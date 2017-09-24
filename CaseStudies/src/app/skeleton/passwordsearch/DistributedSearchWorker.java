package app.dist.passwordsearch;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import app.operations.TupleOperations;
import common.DataGeneration;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;
import proxy.tupleware.TuplewareProxy;


public class DistributedSearchWorker<T extends ITupleSpace> {

	private Object localTSAddress;
	Object masterTSAddress;
    ArrayList<Object> otherWorkerTSName;
    ArrayList<ITupleSpace> workerTSs;
    Integer workerID;
    int matrixSize;
    Integer numberOfWorkers;
	Class tupleSpaceClass;
	
	public final static String completeStatus = "complete";
	
    
    public DistributedSearchWorker(Object localTSAddress, Integer workerID, Object masterTSAddress, ArrayList<Object> otherWorkerTSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
    {
    	this.localTSAddress = localTSAddress;
    	this.masterTSAddress = masterTSAddress;
    	this.otherWorkerTSName = otherWorkerTSName;
    	this.workerID = workerID;
    	this.matrixSize = matrixSize;
    	this.tupleSpaceClass = tupleSpaceClass;
    	this.numberOfWorkers = numberOfWorkers;
    	
    }
    
    /***
     * description of the worker process
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InterruptedException
     */
    public void passwordSearchWorker() throws NoSuchAlgorithmException, IOException, InterruptedException 
    {
        System.out.println("Connecting to data grid " + localTSAddress);
        
        // initialize a local tuple space
        T localTS = (T) getInstanceOfT(tupleSpaceClass);
        localTS.startTupleSpace(localTSAddress, numberOfWorkers, true);
        
        Thread.sleep(3000);
        // connect to the master and to the other workers
    	T masterTS = initializeMasterAndWorkers();
    	
    	TupleOperations.writeTuple(masterTS, localTS, localTS.formTuple("SearchTuple_ttt", new Object[]{"search", "worker", "worker_ready"}, DistributedSearchMaster.searchTupleTemplate), false, true);  	

    	// load data to the local tuple space
    	loadDataTable(localTS);
        
    	System.out.println("Worker " + workerID + ": data loaded");
    	
    	// notify master that worker is ready to receive tasks
    	TupleOperations.writeTuple(masterTS, localTS, localTS.formTuple("SearchTuple_ttt", new Object[]{"search", "worker", "data_loaded"}, DistributedSearchMaster.searchTupleTemplate), false, true);
    	
    	// get test key of the execution
    	Object tupleWithTestKeyObject = TupleOperations.readTuple(masterTS, localTS, localTS.formTuple("SearchTuple_ttf", new Object[]{"search", "master_key", null}, DistributedSearchMaster.searchTupleTemplate), false, true);
    	Object[] tupleWithTestKey = localTS.tupleToObjectArray("SearchTuple", tupleWithTestKeyObject);
    	DProfiler.testKey = (String)tupleWithTestKey[2];
    	
    	
    	System.out.println("Worker " + workerID + ": start to process tasks");
    	
        // get a task from the master
		Object[] searchTaskTuple = searchNextTask(localTS, masterTS);
    	
        String hashedValue = (String)searchTaskTuple[1];
        String status = (String)searchTaskTuple[2];
        
        while(!DistributedSearchWorker.completeStatus.equals(status))
        {
        	// search for the tuple with given hashed value
        	Object foundTupleObject = searchLoop(localTS, workerTSs, hashedValue);
        	Object[] foundTuple = localTS.tupleToObjectArray("SearchTuple", foundTupleObject);
        	
        	System.out.println("Worker " + workerID + ": found the password");
    		
        	// send found password to the master   		
        	TupleOperations.writeTuple(masterTS, localTS, localTS.formTuple("SearchTuple_tff", new Object[]{"foundValue", (String)foundTuple[1], (String)foundTuple[2]}, DistributedSearchMaster.searchTupleTemplate), false, true);
    		
    		// search and retrieve the next task
        	searchTaskTuple = searchNextTask(localTS, masterTS);
            hashedValue = (String)searchTaskTuple[1];
            status = (String)searchTaskTuple[2];
        }
               
		// write data of the test
        String threadName = Thread.currentThread().getName();
        TupleLogger.writeAllToFile(DProfiler.testKey);
        System.out.println("Worker " + workerID + " finished its work");
        localTS.stopTupleSpace();
    }

    /***
     * initialize connections with master and workers
     * @return
     */
	private T initializeMasterAndWorkers() {
		// trying to connect to server tuple space 5 times
        T masterTS = (T) getInstanceOfT(tupleSpaceClass);
        masterTS.startTupleSpace(masterTSAddress, numberOfWorkers, false);
        
        // create connection to other workers
        workerTSs = new ArrayList<>();
        Collections.shuffle(otherWorkerTSName);
        for(int i = 0; i < otherWorkerTSName.size(); i++)
        {
        	T workerTS = (T) getInstanceOfT(tupleSpaceClass);
        	workerTS.startTupleSpace(otherWorkerTSName.get(i), numberOfWorkers, false);
        	workerTSs.add(workerTS);
        }
		return masterTS;
	}
	
	/***
	 * load data with passwords from the file
	 * @param localTS local tuple space
	 * @throws IOException
	 */
	private void loadDataTable(T localTS) throws IOException {
		String[] dataArray = DistributedSearchWorker.getStringArray("hashSet" + workerID + ".dat");
    	for(int i =0; i< dataArray.length; i++)
    	{
    		String[] elements = dataArray[i].split(",");
    		// write data to the local tuple space
        	TupleOperations.writeTuple(localTS, localTS, localTS.formTuple("SearchTuple_ttf", new Object[]{"hashSet", elements[0], elements[1]}, DistributedSearchMaster.searchTupleTemplate), true, true);
    	}
	}
	
	/***
	 * get next task from the master
	 * @param localTS  local tuple space
	 * @param masterTS master tuple space
	 * @return
	 */
	private Object[] searchNextTask(T localTS, T masterTS) {
		Object searchTaskTupleTemplate = localTS.formTuple("SearchTuple_tff", new Object[]{"search_task", null, null}, DistributedSearchMaster.searchTupleTemplate);
    	Object searchTaskTupleObject = TupleOperations.takeTuple(masterTS, localTS, searchTaskTupleTemplate, false, true);
    	Object[] searchTaskTuple = localTS.tupleToObjectArray("SearchTuple", searchTaskTupleObject);
		return searchTaskTuple;
	}

	/***
	 * continuously search a password
     * @param localTS     local tuple space
     * @param workerTSs   tuple spaces of other workers
     * @param hashedValue hashed value for the search
	 * @return
	 * @throws InterruptedException
	 */
    static Object searchLoop(ITupleSpace localTS, ArrayList<ITupleSpace> workerTSs, String hashedValue) throws InterruptedException
    {
    	boolean firstTime = true;
		TupleLogger.begin("read::l-r");
    	Object result = null;
    	while(true)
    	{
    		result = search(localTS, workerTSs, hashedValue, firstTime);
    		firstTime = false;
    		if(result != null)
    		{
    			TupleLogger.end("read::l-r");
    			return result;
    		}
    		try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /***
     * search in the distribution of tuple spaces
     * @param localTS     local tuple space
     * @param workerTSs   tuple spaces of other workers
     * @param hashedValue hashed value for the search
     * @param firstTime   
     * @return
     * @throws InterruptedException
     */
    static Object search(ITupleSpace localTS, ArrayList<ITupleSpace> workerTSs, String hashedValue, boolean firstTime) throws InterruptedException
    {
    	Object template = localTS.formTuple("SearchTuple_ttf", new Object[]{"hashSet", hashedValue, null}, DistributedSearchMaster.searchTupleTemplate);    	
    	if(firstTime)
    		TupleLogger.incCounter("nodeVisited");

		// read from local space
		Object resultTuple = null;
		if (localTS instanceof TuplewareProxy)
			resultTuple = TupleOperations.readIfExistTuple(localTS, localTS, template, true, false);
		else
			resultTuple = TupleOperations.readIfExistTuple(localTS, localTS, template, true, true);	
	
		if (resultTuple != null)
			return resultTuple;
		else
		{
			// search in tuple spaces of the other workers
    		for(int i = 0; i< workerTSs.size(); i++)
    		{
    	    	if(firstTime)
    	    		TupleLogger.incCounter("nodeVisited");
	    		// read from remote space
    	        resultTuple = TupleOperations.readIfExistTuple(workerTSs.get(i), localTS, template, false, true);
    			if(resultTuple != null)
    				return resultTuple;
    		}
		}		
		return null;
    }

	/***
	 * create an object using class name
	 * @param aClass class name
	 * @return
	 */	
    public T getInstanceOfT(Class<T> aClass)
    {
       try {
			return aClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
       return null;
    }  
    
	public static String[] getStringArray(String fileName) throws IOException {
		String str = DataGeneration.readLineStringFromFile(fileName);
		String[] originalDataArray = str.split("\n");
		return originalDataArray;
	}
}
