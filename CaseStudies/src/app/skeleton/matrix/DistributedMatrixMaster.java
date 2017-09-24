package app.skeleton.matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import app.launcher.matrix.GetDataFromDockers;
import app.operations.TupleOperations;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;

public class DistributedMatrixMaster<T extends ITupleSpace> {

	private Object masterGSName;
	int numberOfWorkers;
	int matrixSize;
	Class tupleSpaceClass;
	
	TupleLogger logger = null;
	        
    public static Object[] matrixTemplate = new Object[]{String.class, String.class, Boolean.class, Integer.class, Integer.class, int[].class, Integer.class};
    public static Object[] operationTemplate =new Object[]{String.class, String.class, String.class};
    
    public DistributedMatrixMaster(Object masterGSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
    {
    	this.masterGSName = masterGSName;
    	this.numberOfWorkers = numberOfWorkers;
    	this.matrixSize = matrixSize;
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

        System.out.println("Master started");
        
        // wait for the readiness of all workers
        int workerCounter = 0;
        while(true)
        {
        	Thread.sleep(10);
        	
        	TupleOperations.takeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_ready", ""}, operationTemplate), true, false);
        	workerCounter++;
        	
        	System.out.println(workerCounter);
        	System.out.println(numberOfWorkers);
        	if(workerCounter == numberOfWorkers)
        		break;     	
        }
        
        // spread the test key
        TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "master_key", DProfiler.testKey}, DistributedMatrixMaster.operationTemplate), true, false);
        
        System.out.println("Master: all worker loaded");

       // DProfiler.begin("Master::TotalRuntime");
 //       String threadName = "all" + Thread.currentThread().getName();
        TupleLogger.begin("Master::TotalRuntime");
        
        // send initial data to the workers
        for(int i = 0; i < matrixA.length; i++)
        {        	
        	Object matrixATuple = masterSpace.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixA", true, i, matrixA.length, matrixA[i], null}, DistributedMatrixMaster.matrixTemplate);
        	TupleOperations.writeTuple(masterSpace, masterSpace, matrixATuple, true, false);
        }
        
        for(int i = 0; i < matrixB.length; i++)
        {        	
        	Object matrixBTuple = masterSpace.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixB", true, i, matrixB.length, matrixB[i], null}, DistributedMatrixMaster.matrixTemplate);
        	TupleOperations.writeTuple(masterSpace, masterSpace, matrixBTuple, true, false);
        }
        System.out.println("Matrices are loaded");
        
        // start multiplication
        Object startMultiplication = masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "start_multiplication", ""}, operationTemplate);
        TupleOperations.writeTuple(masterSpace, masterSpace, startMultiplication, true, false);
        
        
        // wait for the result
        int[][] matrixC = new int[matrixDimension][matrixDimension];

        // get rows of the result matrix
        for(int i = 0; i < matrixC.length; i++) 
        {    	
            Object matrixRowResultTuple = masterSpace.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixC", true, null, null, null, null}, DistributedMatrixMaster.matrixTemplate);

            //masterGigaSpace.take(matrixRowResultTuple, GigaspacesDistPassSearchMaster.takeTimeout);
            Object resultTuple = TupleOperations.takeTuple(masterSpace, masterSpace, matrixRowResultTuple, true, false);

        	System.out.println("Master: row was obtained: " + i);
        }
        System.out.println("Master: tasks were created");
        
        
//        // send to worker - "finish its work"
//        for(int i =0; i< numberOfWorkers; i++)
//        {
//        	TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, operationTemplate), true, false);       	
//        }

        TupleLogger.end("Master::TotalRuntime");
                
   //     DProfiler.printExt();
        DProfiler.writeTestKeyToFile(DProfiler.testKey);
        
        String executionFolder = System.getProperty("user.dir");
        System.out.println(executionFolder);
        TupleLogger.writeAllToFile(DProfiler.testKey);
//        TupleLogger.printStatistics(executionFolder,  DProfiler.testKey, new String[]{"take::remote", "take::local", "write::remote", 
//        		"write::local", "takeE::local", "takeE::remote", "read::l-r", "nodeVisited", "Master::TotalRuntime", "READ_LISTENER"});
        
      //  String executionFolder = System.getProperty("user.dir");
      //  System.out.println(executionFolder);
      //  DProfiler.printStatistics(executionFolder, DProfiler.testKey, new String[]{"take::remote", "take::local", "write::remote", "write::local", "take::local", "nodeVisited", "Master::TotalRuntime"});
          
        int finishedWorkerCount = 0;
        while(true)
		{
        	Object tuple = TupleOperations.takeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, operationTemplate), true, false);
        	finishedWorkerCount++;
        	if(finishedWorkerCount == numberOfWorkers)
        		break;
        	Thread.sleep(100);
		}
        System.err.println("Master finished its work");
         
    	GetDataFromDockers.sendLogData(masterSpace);
    	System.out.println("waiting");
    	
        masterSpace.stopTupleSpace();
        
        
        Thread.sleep(5000);
      //  System.exit(0);

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
  

}
