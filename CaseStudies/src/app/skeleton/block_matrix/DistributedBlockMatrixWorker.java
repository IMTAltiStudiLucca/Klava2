package app.skeleton.block_matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import app.operations.TupleOperations;
import common.CustomPair;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;


public class DistributedBlockMatrixWorker<T extends ITupleSpace> {

	private Object localGSPath;
	Object masterTSName;
    ArrayList<Object> otherWorkerTSName;
    ArrayList<ITupleSpace> workerGSs;
    Integer workerID;
    int matrixSize;
    Integer numberOfWorkers;
	Class tupleSpaceClass;
	
    
    public DistributedBlockMatrixWorker(Object _localGSPath, Integer _workerID, Object _masterTSName, ArrayList<Object> _otherWorkerTSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
    {
    	this.localGSPath = _localGSPath;
    	this.masterTSName = _masterTSName;
    	this.otherWorkerTSName = _otherWorkerTSName;
    	this.workerID = _workerID;
    	this.matrixSize = matrixSize;
    	this.tupleSpaceClass = tupleSpaceClass;
    	this.numberOfWorkers = numberOfWorkers;
    	
    }
    
    public void start() throws NoSuchAlgorithmException, IOException, InterruptedException 
    {
        System.out.println("Connecting: worker " + workerID);
        T localGS = (T) getInstanceOfT(tupleSpaceClass);
        localGS.startTupleSpace(localGSPath, numberOfWorkers, true);
        
        Thread.sleep(3000);
    	// trying to connect to server tuple space 5 times
        T masterGS = (T) getInstanceOfT(tupleSpaceClass);
        masterGS.startTupleSpace(masterTSName, numberOfWorkers, false);
        
        // create connection to other workers
        workerGSs = new ArrayList<>();
        Collections.shuffle(otherWorkerTSName);
        for(int i = 0; i < otherWorkerTSName.size(); i++)
        {
        	T workerTS = (T) getInstanceOfT(tupleSpaceClass);
        	workerTS.startTupleSpace(otherWorkerTSName.get(i), numberOfWorkers, false);
        	workerGSs.add(workerTS);
        }

        // signal to master that worker is ready
    	TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("OperationTuple", new Object[]{"matrix", "worker_ready", ""}, DistributedBlockMatrixMaster.operationTemplate), false, true);
	
    	// get test key
    	Object tupleWithTestKeyObject = TupleOperations.readTuple(masterGS, localGS, localGS.formTuple("OperationTuple", new Object[]{"matrix", "master_key", null}, DistributedBlockMatrixMaster.operationTemplate), false, true);
    	Object[] tupleWithTestKey = localGS.tupleToObjectArray("OperationTuple", tupleWithTestKeyObject);
    	DProfiler.testKey = (String)tupleWithTestKey[2];
    	   	
    	System.out.println("Ready: worker " + workerID);
    	
    	
    	while(true) {
    		
    		Object taskTemplate = masterGS.formTuple("BlockMatrixRowTuple", new Object[]{"task", null, null, null, null}, DistributedBlockMatrixMaster.blockMatrixTemplate);
    		Object taskObject = TupleOperations.takeTuple(masterGS, localGS, taskTemplate, false, false);
    		Object[] taskTuple = localGS.tupleToObjectArray("BlockMatrixRowTuple", taskObject);
    		
    		
    		Integer i = (Integer)taskTuple[1];
    		Integer j = (Integer)taskTuple[2];
    		Integer k = (Integer)taskTuple[3];
    		   		
    		// finish or not
    		if(i == -1)
    			break;
    		
    		// take first operand
    		Object firstOperandTemplate = masterGS.formTuple("BlockMatrixRowTuple", new Object[]{"matrixA", i, k, null, null}, DistributedBlockMatrixMaster.blockMatrixTemplate);
    		Object firstOperandObject = TupleOperations.readTuple(masterGS, localGS, firstOperandTemplate, false, true);
    		Object[] firstOperandTuple = localGS.tupleToObjectArray("BlockMatrixRowTuple", firstOperandObject);	
    		
    		// take second operand
    		Object secondOperandTemplate = masterGS.formTuple("BlockMatrixRowTuple", new Object[]{"matrixB", k, j, null, null}, DistributedBlockMatrixMaster.blockMatrixTemplate);
    		Object secondOperandObject = TupleOperations.readTuple(masterGS, localGS, secondOperandTemplate, false, true);
    		Object[] secondOperandTuple = localGS.tupleToObjectArray("BlockMatrixRowTuple", secondOperandObject);	
    		
    		int[][] aBlock = (int[][])firstOperandTuple[4];
    		int[][] bBlock = (int[][])secondOperandTuple[4];
    		
    		int[][] tmp = multiplyTwoMatrix(aBlock, bBlock);
    		
    		// get a temporal block of result matrix C
    		Object cBlockTemplate = masterGS.formTuple("BlockMatrixRowTuple", new Object[]{"matrixC", i, j, null, null}, DistributedBlockMatrixMaster.blockMatrixTemplate);
    		Object cBlockObject = TupleOperations.takeTuple(masterGS, localGS, cBlockTemplate, false, true);
    		Object[] cBlockTuple = localGS.tupleToObjectArray("BlockMatrixRowTuple", cBlockObject);	
    		
    		Integer cnt = (Integer)cBlockTuple[3];
    		int[][] cBlock = (int[][])cBlockTuple[4];
    		cBlock = sumTwoMatrix(cBlock, tmp);
    		
    		// send to the master
        	TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("BlockMatrixRowTuple", new Object[]{"matrixC", i, j, ++cnt, cBlock}, 
        			DistributedBlockMatrixMaster.blockMatrixTemplate), false, true);
        	
        	//System.out.println("Update matrix C: iteration #" + cnt);
    	}
    	
     	
        String threadName = Thread.currentThread().getName();
        TupleLogger.writeAllToFile(DProfiler.testKey);
    	        
    	System.out.println("Worker " + workerID + ": finished its work");
    	
    	// wait for the end
    	TupleOperations.writeTuple(masterGS, localGS,  localGS.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, DistributedBlockMatrixMaster.operationTemplate), false, false);
    	    	
    }
    
    
    Object searchLoop(ITupleSpace localGS, ArrayList<ITupleSpace> workerGSs, Integer rowID) throws InterruptedException
    {
		TupleLogger.begin("read::l-r");
    	Object result = null;
    	while(true)
    	{
    		result = search(localGS, workerGSs, rowID);
    		if(result != null)
    		{
    			TupleLogger.end("read::l-r");
    			return result;
    		}
    		try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    /*
     * search in the distribution of tuple spaces
     */
    Object search(ITupleSpace localGS, ArrayList<ITupleSpace> workerGSs, Integer rowID) throws InterruptedException
    {
    	Object template = localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixB", true, rowID, null, null, null}, DistributedBlockMatrixMaster.blockMatrixTemplate);
    	
		TupleLogger.incCounter("nodeVisited");

		// read from local space
		Object resultTuple = TupleOperations.readIfExistTuple(localGS, localGS, template, true, true);
	
		if (resultTuple != null)
			return resultTuple;
		else
		{
    		for(int i = 0; i< workerGSs.size(); i++)
    		{
    			TupleLogger.incCounter("nodeVisited");
	    		// read from remote space
             //  System.out.print("worker" + workerID + " in_not_block: start-");
    	        resultTuple = TupleOperations.readIfExistTuple(workerGSs.get(i), localGS, template, false, true);
             //   System.out.println("end");
    	        
    			if(resultTuple != null)
    				return resultTuple;
    			
    			//Thread.sleep(5);
    		}
		}
		
		return null;
    }

    
    ArrayList<Integer> getSequenceOfRow(int startingNumber, int maxNumber)
    {
    	ArrayList<Integer> sequence = new ArrayList<Integer>();
    	
    	for(int i = startingNumber; i<maxNumber; i++)
    		sequence.add(i);
    	
    	for(int i = 0; i < startingNumber; i++)
    		sequence.add(i);

    	return sequence;
    }
    
	public static int[] multiplyByTheNumberOfArray(int[] children, int number)
	{
		int array[] = new int[children.length];
		for( int i = 0; i < children.length; i++)
		{
			array[i] = children[i] * number;
		}
		return array;
    }
	
	public static int[][] multiplyTwoMatrix(int[][] firstMatrix, int[][] secondMatrix)
	{
		/* Create another 2d array to store the result using the original arrays' lengths on row and column respectively. */
		int [][] result = new int[firstMatrix.length][secondMatrix[0].length];

		/* Loop through each and get product, then sum up and store the value */
		for (int i = 0; i < firstMatrix.length; i++) { 
		    for (int j = 0; j < secondMatrix[0].length; j++) { 
		        for (int k = 0; k < firstMatrix[0].length; k++) { 
		            result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
		        }
		    }
		}
		return result;
    }
	
	public static int[][] sumTwoMatrix(int[][] firstMatrix, int[][] secondMatrix)
	{
		/* Create another 2d array to store the result using the original arrays' lengths on row and column respectively. */
		int [][] result = new int[firstMatrix.length][firstMatrix[0].length];

		/* Loop through each and get product, then sum up and store the value */
		for (int i = 0; i < firstMatrix.length; i++) { 
		    for (int j = 0; j < firstMatrix[0].length; j++) { 
		    	result[i][j] = firstMatrix[i][j] + secondMatrix[i][j];
		    }
		}
		return result;
    }
	
	public static int[] sumArray(int[] firstArray, int[] secondArray)
	{
		int array[] = new int[firstArray.length];
		for( int i = 0; i < firstArray.length; i++)
		{
			array[i] = firstArray[i] * secondArray[i];
		}
		return array;
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
