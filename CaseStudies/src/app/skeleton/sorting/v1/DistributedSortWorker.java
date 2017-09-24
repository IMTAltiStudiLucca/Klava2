package app.dist.sorting;

import java.util.ArrayList;
import java.util.Collections;

import app.operations.TupleOperations;
import apps.sorting.QSort;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;
import proxy.tupleware.TuplewareProxy;


public class DistributedSortWorker<T extends ITupleSpace> {

	private Object localGSPath;
	Object masterTSName;
    ArrayList<Object> otherWorkerTSName;
    ArrayList<ITupleSpace> workerGSs;
    Integer workerID;
    int matrixSize;
    Integer numberOfWorkers;
	Class tupleSpaceClass;
	
    
    public DistributedSortWorker(Object _localGSPath, Integer _workerID, Object _masterTSName, ArrayList<Object> _otherWorkerTSName, int matrixSize, int numberOfWorkers, Class<?> tupleSpaceClass)
    {
    	this.localGSPath = _localGSPath;
    	this.masterTSName = _masterTSName;
    	this.otherWorkerTSName = _otherWorkerTSName;
    	this.workerID = _workerID;
    	this.matrixSize = matrixSize;
    	this.tupleSpaceClass = tupleSpaceClass;
    	this.numberOfWorkers = numberOfWorkers; 	
    }
    
    public void start() throws InterruptedException {      
        
		System.out.println("Connecting to data grid " + localGSPath);
	     
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
	     	//GigaSpaceProxy gs = new GigaSpaceProxy(); // GigaspacesDistSearchTest.createGigaspace(otherWorkerTSName.get(i));
	     	T workerTS = (T) getInstanceOfT(tupleSpaceClass);
	     	workerTS.startTupleSpace(otherWorkerTSName.get(i), numberOfWorkers, false);
	     	workerGSs.add(workerTS);
	     }
        
	     System.out.println("Worker started: worker" + workerID);
	     
	     TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("SortingTuple", new Object[]{"sorting", null, "worker_ready"}, 
    			DistributedSortMaster.sortingTupleTemplate), false, false);
        System.out.println("Worker " + workerID + " process started.");

    	// get test key
    	Object tupleWithTestKeyObject = TupleOperations.readTuple(masterGS, localGS, localGS.formTuple("SortingTuple", new Object[]{"master_key", null, null}, DistributedSortMaster.sortingTupleTemplate), false, true);
    	Object[] tupleWithTestKey = localGS.tupleToObjectArray("SortingTuple", tupleWithTestKeyObject);
    	DProfiler.testKey = (String)tupleWithTestKey[2];
    	
        System.out.println("Test key: " + DProfiler.testKey + " worker" + workerID);
        
        // get qsort tuple
        Object nextTaskTupleObject = searchLoop(masterGS, localGS, workerGSs);
        Object[] nextTaskTuple = localGS.tupleToObjectArray("SortingTuple", nextTaskTupleObject);

        QSort qs = (QSort)nextTaskTuple[1];
        String status = (String)nextTaskTuple[2];

        while(!status.equals("complete")) {
            // sort OR split
            if(qs.readyToSort()) {
                System.out.println("Sorting " + qs.getData().length + " items.");
                TupleLogger.begin("QSortWorker::Sorting");
                qs.insertionSort(qs.getData());
                TupleLogger.end("QSortWorker::Sorting");
                    
                TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("SortingTuple", 
                		new Object[]{"sorting", qs, "sorted"}, DistributedSortMaster.sortingTupleTemplate), false, true);   

                // try to find unsorted partition to work on
                nextTaskTupleObject = searchLoop(masterGS, localGS, workerGSs);
                nextTaskTuple = localGS.tupleToObjectArray("SortingTuple", nextTaskTupleObject);

                qs = (QSort)nextTaskTuple[1];
                status = (String)nextTaskTuple[2];
            } else {
                TupleLogger.begin("QSortWorker::Partitioning");
                System.out.print("Splitting array of size " +qs.size());
                QSort dup = qs.split();
                System.out.println(" into " + qs.size() + "/" + dup.size());
                TupleLogger.end("QSortWorker::Partitioning");

                System.out.println("Split partition..continuing...");
                
                TupleOperations.writeTuple(localGS, localGS, localGS.formTuple("SortingTuple", 
                		new Object[]{"sort_array", dup, "unsorted"}, DistributedSortMaster.sortingTupleTemplate), true, true);    
            }
        }

        String threadName = Thread.currentThread().getName();
        TupleLogger.writeAllToFile(DProfiler.testKey);
        System.out.println("Worker " + workerID + " finished its work");
       
    } 
    
    static Object searchLoop(ITupleSpace masterGS, ITupleSpace localGS, ArrayList<ITupleSpace> workerGSs) throws InterruptedException
    {
    	boolean firstTime = true;
		TupleLogger.begin("read::l-r");
    	Object result = null;
    	int attemptCounter = 1;
    	while(true)
    	{
    		result = search(masterGS, localGS, workerGSs, firstTime, attemptCounter);
    		attemptCounter++;
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
    
    /*
     * search in the distribution of tuple spaces
     */
    static Object search(ITupleSpace masterGS, ITupleSpace localGS, ArrayList<ITupleSpace> workerGSs, boolean firstTime, int attemptCounter) throws InterruptedException
    {
    	Object template = localGS.formTuple("SortingTuple", new Object[]{"sort_array", null, null}, DistributedSortMaster.sortingTupleTemplate);
    	
    	if(firstTime)
    		TupleLogger.incCounter("nodeVisited");

		// read from local space

		Object resultTuple = null;
    	if (masterGS instanceof TuplewareProxy)
    		resultTuple = TupleOperations.takeIfExistTuple(localGS, localGS, template, true, false);	
    	else
    		resultTuple = TupleOperations.takeIfExistTuple(localGS, localGS, template, true, true);
	
		if (resultTuple != null)
			return resultTuple;
		else
		{
    		for(int i = 0; i< workerGSs.size(); i++)
    		{
    	    	if(firstTime)
    	    		TupleLogger.incCounter("nodeVisited");
	    		// read from remote space
    	        resultTuple = TupleOperations.takeIfExistTuple(workerGSs.get(i), localGS, template, false, true);
    			if(resultTuple != null)
    				return resultTuple;
    			
    			//Thread.sleep(5);
    		}
    	//	if(attemptCounter % 10 == 0) 
    		{
				resultTuple = TupleOperations.takeIfExistTuple(masterGS, localGS, template, false, true);
				if(resultTuple != null)
					return resultTuple;
    		}
		}
		
		return null;
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
