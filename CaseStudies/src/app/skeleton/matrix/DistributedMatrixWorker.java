package app.dist.matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import app.operations.TupleOperations;
import apps.dist.matrix.implementations.GetDataFromDockers;
import common.CustomPair;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;


public class DistributedMatrixWorker<T extends ITupleSpace> {

	private Object localGSPath;
	Object masterTSName;
    ArrayList<Object> otherWorkerTSName;
    ArrayList<ITupleSpace> workerGSs;
    Integer workerID;
    int matrixSize;
    Integer numberOfWorkers;
	Class tupleSpaceClass;
	
    
    public DistributedMatrixWorker(Object _localGSPath, Integer _workerID, Object _masterTSName, ArrayList<Object> _otherWorkerTSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
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
    	//String threadName = "worker" + workerID;

        System.out.println("Connecting: worker " + workerID);
        System.out.println("numberOfWorkers: " + numberOfWorkers);
        System.out.println("matrixSize: " + matrixSize);
        System.out.println("masterTSName: " + masterTSName);
         
        //GigaSpaceProxy localGS = new GigaSpaceProxy();
        T localGS = (T) getInstanceOfT(tupleSpaceClass);
        localGS.startTupleSpace(localGSPath, numberOfWorkers, true);
        
        System.out.println("localGSPath:" + localGSPath);
        
        
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

        System.out.println("Connected to all spaces");
        
        //Thread.sleep(1000*workerID);
        // signal to master that worker is ready
    	TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("OperationTuple", new Object[]{"matrix", "worker_ready", ""}, DistributedMatrixMaster.operationTemplate), false, true);
	
        System.out.println("worker_ready is sent");
        
    	// get test key
    	Object tupleWithTestKeyObject = TupleOperations.readTuple(masterGS, localGS, localGS.formTuple("OperationTuple", new Object[]{"matrix", "master_key", null}, DistributedMatrixMaster.operationTemplate), false, true);
    	Object[] tupleWithTestKey = localGS.tupleToObjectArray("OperationTuple", tupleWithTestKeyObject);
    	DProfiler.testKey = (String)tupleWithTestKey[2];
    	    	
    	System.out.println("Ready: worker " + workerID);
    	 
    	// determine the matrix to own compute the number of row per each local tuple space
    	// uniformly
    	int workerPerMatrixA = numberOfWorkers;
    	int workerPerMatrixB = numberOfWorkers; // numberOfWorkers   1

		// for matrix A
		int numberOfRowsMatrixA = matrixSize/workerPerMatrixA;
		int remainder = matrixSize % workerPerMatrixA;
		if(workerID < remainder)
			numberOfRowsMatrixA++;	
    	    	
    	// for matrix B
		int numberOfRowsMatrixB = 0;
    	if (workerID < workerPerMatrixB)
    	{    		
    		numberOfRowsMatrixB = matrixSize/workerPerMatrixB;
    		remainder = matrixSize % workerPerMatrixB;
    		if(workerID < remainder)
    			numberOfRowsMatrixB++;	
    	}
    	
    	System.out.println("numberOfRowsMatrixB = " + numberOfRowsMatrixB);
    	
    	
    	for(int i =0; i < numberOfRowsMatrixB; i++)
    	{  		
    		Object rowTuple = TupleOperations.takeTuple(masterGS, localGS,
    				localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixB", true, null, null, null, null},
    						DistributedMatrixMaster.matrixTemplate), false, true);
    		
    		TupleOperations.writeTuple(localGS, localGS, rowTuple, true, true);
    //		Thread.sleep(10);
    	}
    	
    	
    	System.out.println("Matrix B loaded: worker " + workerID);
    	
    	
    	ArrayList<CustomPair<Integer, int[]>> rows = new ArrayList<CustomPair<Integer, int[]>>();
    	// get data from the master and put it into local tuple space
    	for(int i =0; i < numberOfRowsMatrixA; i++)
    	{  		
    		Object rowTupleObject = TupleOperations.takeTuple(masterGS, localGS,
    				localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixA", true, null, null, null, null}, 
    						DistributedMatrixMaster.matrixTemplate), false, true);
    		Object[] rowTuple = localGS.tupleToObjectArray("MatrixRowTuple", rowTupleObject);
    				
    		CustomPair<Integer, int[]> pair = new CustomPair<Integer, int[]>((Integer)rowTuple[3], (int[])rowTuple[5]);
			rows.add(pair);
		//	Thread.sleep(10);
    	}
    	
		// read from local space
    	Object templateTTT = localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixB", true, 1, null, null, null}, DistributedMatrixMaster.matrixTemplate);
    	Object resultTupleTTT = TupleOperations.readIfExistTuple(localGS, localGS, templateTTT, true, true);
    	if(resultTupleTTT == null)
        	System.out.println("resultTupleTTT null");	
    	
    	System.out.println("Matrix A loaded: worker " + workerID);
	
    	int resultRowCounter = 0;
    	
       	// multiplication
    	// only processes with data of MatrixA can initiate the multiplication

		// look at each row
    	for(int i = 0; i < rows.size(); i++)
    	{
    		System.out.println("iterations:" + i);
    		int rowID = rows.get(i).getKey();
    		int[] rowOperandOne = rows.get(i).getValue();
    		
    		// check all rows of matrix B
    		ArrayList<Integer> rowsToInteractList = getSequenceOfRow(rowID, matrixSize);
    		for(int k = 0; k < rowsToInteractList.size(); k++)
    		{
    			// take k-row from matrix B
    			//System.out.println("search start: worker " + workerID);
	    		Object rowOperandTwoTupleObject = searchLoop(localGS, workerGSs, rowsToInteractList.get(k));
	    		//System.out.println("search end: worker " + workerID);
	    		
	    	//	System.out.println("found " + workerID);
	    		Object[] rowOperandTwoTuple = localGS.tupleToObjectArray("MatrixRowTuple", rowOperandTwoTupleObject);
	    		
	    		int[] rowOperandTwo = (int[])rowOperandTwoTuple[5];
	    		int[] rowMatrixC = multiplyByTheNumberOfArray(rowOperandTwo, rowOperandOne[rowID]);
	    		
	    		// check matrix C (existence of partial result)
	    		// always stored in the same place
	    		Object rowMatrixCTupleObject = TupleOperations.takeIfExistTuple(localGS, localGS,
	    				localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixC", false, rowID, rowMatrixC.length, null, null},
	    						DistributedMatrixMaster.matrixTemplate), true, true);	    		
	    		if(rowMatrixCTupleObject == null)
	    		{
	    			TupleOperations.writeTuple(localGS, localGS,
	    					localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixC", false, rowID, rowMatrixC.length, rowMatrixC, Integer.valueOf(1)},
	    							DistributedMatrixMaster.matrixTemplate), true, true);
	    			
	    			//System.err.println("new row C i=" + i);
	    		} else
	    		{
	    			Object[] rowMatrixCTuple = localGS.tupleToObjectArray("MatrixRowTuple", rowMatrixCTupleObject);
	    			
	    			int[] previousPartialData = (int[])rowMatrixCTuple[5];
	    			int[] updateRowData = sumArray(previousPartialData, rowMatrixC);
	    			
	    			boolean isRowComplete = (Integer)rowMatrixCTuple[6] + 1 == matrixSize ? true : false;
	    			
	    			//System.out.println(rowMatrixCTuple[6]);
	    			
	    			if(isRowComplete)
	    			{
	    				// write to the master result
	    				TupleOperations.writeTuple(masterGS, localGS, 
	    						localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixC", isRowComplete, rowID, rowMatrixC.length, updateRowData, (int)rowMatrixCTuple[6] + 1},
	    								DistributedMatrixMaster.matrixTemplate), false, true);
	    				
	    				//System.out.println("new result row is sent to master i=" + i + ", obtained resultRowCounter=" + resultRowCounter);
	    				resultRowCounter++;
	    			}
	    			else
	    				TupleOperations.writeTuple(localGS, localGS,
	    						localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixC", isRowComplete, rowID, rowMatrixC.length, updateRowData, (int)rowMatrixCTuple[6] + 1},
	    								DistributedMatrixMaster.matrixTemplate), true, true);
	    		}
    		}
    //		System.out.println("Processing: worker " + workerID);
    //		System.err.println("Processing: worker " + workerID);
    	}
    	
    	//System.out.println("FINISHED: worker " + workerID);
    	System.err.println("FINISHED: worker " + workerID);
    	
        String threadName = Thread.currentThread().getName();
        TupleLogger.writeAllToFile(DProfiler.testKey);
    	        
    	// wait for the end
    	TupleOperations.writeTuple(masterGS, localGS,  localGS.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, DistributedMatrixMaster.operationTemplate), false, false);

    	System.out.println("Worker " + workerID + ": finished its work");
    	  	
    	//localGS.stopTupleSpace();
    	
    	GetDataFromDockers.sendLogData(localGS);
    	System.out.println("waiting");
    	while(true)
    	{
    		Thread.sleep(100000);
    	}

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
    	Object template = localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixB", true, rowID, null, null, null}, DistributedMatrixMaster.matrixTemplate);
    	
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
