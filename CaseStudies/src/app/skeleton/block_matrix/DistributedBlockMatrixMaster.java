package app.skeleton.block_matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import app.operations.TupleOperations;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;

public class DistributedBlockMatrixMaster<T extends ITupleSpace> {

	private Object masterGSName;
	int numberOfWorkers;
	int matrixSize;
	int blockSize;
	Class tupleSpaceClass;
	
	TupleLogger logger = null;
	        
    public static Object[] blockMatrixTemplate = new Object[]{String.class, Integer.class, Integer.class, Integer.class, int[][].class};
    
    public static Object[] operationTemplate =new Object[]{String.class, String.class, String.class};
    //public static Object[] taskTemplate =new Object[]{String.class, Integer.class, Integer.class, Integer.class};
    
    public DistributedBlockMatrixMaster(Object masterGSName, int matrixSize, int numberOfWorkers, int blockSize, Class tupleSpaceClass)
    {
    	this.masterGSName = masterGSName;
    	this.numberOfWorkers = numberOfWorkers;
    	this.matrixSize = matrixSize;
    	this.blockSize = blockSize;
    	this.tupleSpaceClass = tupleSpaceClass;
    }
    
   	public void start() throws NoSuchAlgorithmException, InterruptedException, IOException {
    	 		
   		int matrixDimension = matrixSize;
   		// generate initial matrices
   		int[][] matrixA = generateMatrix(matrixDimension);
   		int[][] matrixB = generateMatrix(matrixDimension);
        System.out.println("Connecting to data grid " + masterGSName);
        // start local tuple space   
        T masterSpace = (T) getInstanceOfT(tupleSpaceClass);
        masterSpace.startTupleSpace(masterGSName, numberOfWorkers, true);  
        
        // wait for the readiness of all workers
        int workerCounter = 0;
        while(true)
        {
        	Thread.sleep(10);
        	
        	TupleOperations.takeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_ready", ""}, operationTemplate), true, false);
        	workerCounter++;
        	if(workerCounter == numberOfWorkers)
        		break;     	
        }
   		
        // spread the test key
        TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "master_key", DProfiler.testKey}, DistributedBlockMatrixMaster.operationTemplate), true, false);
        
        System.out.println("Master: all worker loaded");
        TupleLogger.begin("Master::TotalRuntime");
        
        
        int[][] matrixC = initMatrixC(matrixSize);
        
        int blockBorder = matrixSize/(blockSize);
        for(int i=0; i<blockBorder; i++) 
        	for(int j=0; j<blockBorder; j++) {
        		Object matrixATuple = masterSpace.formTuple("BlockMatrixRowTuple", new Object[]{"matrixA", i, j, blockSize, getBlock(matrixA, i, j, blockSize)}, DistributedBlockMatrixMaster.blockMatrixTemplate);
        		TupleOperations.writeTuple(masterSpace, masterSpace, matrixATuple, true, true);
        		
        		Object matrixBTuple = masterSpace.formTuple("BlockMatrixRowTuple", new Object[]{"matrixB", i, j, blockSize, getBlock(matrixB, i, j, blockSize)}, DistributedBlockMatrixMaster.blockMatrixTemplate);
        		TupleOperations.writeTuple(masterSpace, masterSpace, matrixBTuple, true, true);
        		
        		Object matrixCTuple = masterSpace.formTuple("BlockMatrixRowTuple", new Object[]{"matrixC", i, j, 0, getBlock(matrixC, i, j, blockSize)}, DistributedBlockMatrixMaster.blockMatrixTemplate);
        		TupleOperations.writeTuple(masterSpace, masterSpace, matrixCTuple, true, true);
        		
        		for(int k=0; k<blockBorder; k++) 
        		{
        			//int k = i*blockBorder + j;
            		Object task = masterSpace.formTuple("BlockMatrixRowTuple", new Object[]{"task", i, j, k, null}, DistributedBlockMatrixMaster.blockMatrixTemplate);
            		TupleOperations.writeTuple(masterSpace, masterSpace, task, true, true);
            		//System.out.println("Create a task #" + k);
        		}
        	}
        
        // wait for completed blocks
        for(int i=0; i<blockBorder*blockBorder; i++) {
    		Object task = masterSpace.formTuple("BlockMatrixRowTuple", new Object[]{"matrixC", null, null, blockBorder, null}, DistributedBlockMatrixMaster.blockMatrixTemplate);
    		Object matrixCRowObject = TupleOperations.takeTuple(masterSpace, masterSpace, task, true, false);
    		
    		Object[] matrixCRowTuple = masterSpace.tupleToObjectArray("BlockMatrixRowTuple", matrixCRowObject);	
    		System.out.println("Collecting results: " + i);
    		//System.out.println("Collecting results: " + (int[][])matrixCRowTuple[4]);
    	}
        
        // a poison tuple
		for(int k=0; k<numberOfWorkers; k++) {
    		Object task = masterSpace.formTuple("BlockMatrixRowTuple", new Object[]{"task", -1, -1, -1, null}, DistributedBlockMatrixMaster.blockMatrixTemplate);
    		TupleOperations.writeTuple(masterSpace, masterSpace, task, true, false);
		}

        TupleLogger.end("Master::TotalRuntime");
           
        int finishedWorkerCount = 0;
        while(true)
		{
        	Object tuple = TupleOperations.takeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, operationTemplate), true, false);
        	finishedWorkerCount++;
        	if(finishedWorkerCount == numberOfWorkers)
        		break;
        	Thread.sleep(100);
		}
        
        System.out.println("Master finished");
        
        DProfiler.writeTestKeyToFile(DProfiler.testKey);
        
        String executionFolder = System.getProperty("user.dir");
        System.out.println(executionFolder);
        TupleLogger.writeAllToFile(DProfiler.testKey);
        TupleLogger.printStatistics(executionFolder,  DProfiler.testKey, new String[]{"take::remote", "take::local", "write::remote", 
        		"write::local", "takeE::local", "takeE::remote", "read::l-r", "nodeVisited", "Master::TotalRuntime", "READ_LISTENER"});
        System.err.println("Master finished its work");
         
        masterSpace.stopTupleSpace();
        Thread.sleep(5000);
        System.exit(0);

    } 	
   	
  
   	
   	
   	int[][] generateMatrix(int dimension)
   	{
   		int[][] matrix = new int[dimension][dimension];
		Random rn = new Random();	
		
		for(int i = 0; i < dimension; i++)
			for(int j = 0; j < dimension; j++)
				matrix[i][j] = rn.nextInt(10);		
   		return matrix;
   	}
   	

    public T getInstanceOfT(Class<T> aClass)
    {
       try {
			return aClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       return null;
    }  
    
    
    public int[][] initMatrixC(int matrixSize) {
    	int[][] matrixC = new int[matrixSize][matrixSize];
    	for(int i=0; i<matrixSize; i++)
    		for(int j=0; j<matrixSize; j++)
    			matrixC[i][j] = 0;
    	return matrixC;
    }
    
    // get a block using index of the block
    public int[][] getBlock(int[][] initialMatrix, int indexI, int indexJ, int blockSize) {
    	
    	int[][] block = new int[blockSize][blockSize];
    	for(int i=0; i<blockSize; i++)
    		for(int j=0; j<blockSize; j++) {
    			int iPos = indexI*blockSize + i;
    			int jPos = indexJ*blockSize + j;
    			block[i][j] = initialMatrix[iPos][jPos];
    		}
    	return block;
    }
  
    public int[][] getBlock6x6(int matrixIndex) {
    	if(matrixIndex ==1) {
    		int[][] matrix = new int[][]{
    			  { 1, 2, 3, 4, 5, 6},
    			  { 4, 5, 6, 7, 8, 9 },
    			  { 1, 2, 3, 4, 5, 6 },
    			  { 4, 5, 6, 7, 8, 9 },
    			  { 1, 2, 3, 4, 5, 6 },
    			  { 4, 5, 6, 7, 8, 9 }
    			};
    		return matrix;
    	} else {
    		int[][] matrix = new int[][]{
  			  { 1, 3, 5, 7, 9, 11 },
  			  { 7, 6, 5, 4, 3, 2 },
  			  { 1, 3, 5, 7, 9, 11 },
  			  { 7, 6, 5, 4, 3, 2 },
  			  { 1, 3, 5, 7, 9, 11 },
  			  { 7, 6, 5, 4, 3, 2 }
  			  
  			};
  		return matrix;
    	}
    	
    }
}
