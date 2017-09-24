package app.skeleton.sorting.v2;

import java.util.ArrayList;
import java.util.Hashtable;

import app.operations.TupleOperations;
import app.skeleton.matrix.DistributedMatrixMaster;
import apps.sorting.QSort;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;


public class DistributedSortWorkerV2<T extends ITupleSpace> {

	private Object localGSPath;
	Object masterTSName;
    ArrayList<Object> otherWorkerTSName;
    Hashtable<String, ITupleSpace> workerGSs;
    Integer workerID;
    int matrixSize;
    Integer numberOfWorkers;
	Class tupleSpaceClass;
	
    
    public DistributedSortWorkerV2(Object localGSPath, Integer workerID, Object masterTSName, ArrayList<Object> otherWorkerTSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
    {
    	this.localGSPath = localGSPath;
    	this.masterTSName = masterTSName;
    	this.otherWorkerTSName = otherWorkerTSName;
    	this.workerID = workerID;
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
	     workerGSs = new Hashtable<>();
	     otherWorkerTSName.add(localGSPath);
	     for(int i = 0; i < otherWorkerTSName.size(); i++)
	     {
	     	//GigaSpaceProxy gs = new GigaSpaceProxy(); // GigaspacesDistSearchTest.createGigaspace(otherWorkerTSName.get(i));
	     	T workerTS = (T) getInstanceOfT(tupleSpaceClass);
	     	workerTS.startTupleSpace(otherWorkerTSName.get(i), numberOfWorkers, false);
	     	String key = String.valueOf(otherWorkerTSName.get(i).hashCode());
	     	workerGSs.put(key, workerTS);
	     }
        
	    System.out.println("Worker started: worker" + workerID);
	     
	     TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("SortingTuple", new Object[]{"sorting", null, "worker_ready"}, 
    			DistributedSortMasterV2.sortingTupleTemplate), false, false);
        System.out.println("Worker " + workerID + " process started.");

    	// get test key
    	Object tupleWithTestKeyObject = TupleOperations.readTuple(masterGS, localGS, localGS.formTuple("SortingTuple", new Object[]{"master_key", null, null}, DistributedSortMasterV2.sortingTupleTemplate), false, true);
    	Object[] tupleWithTestKey = localGS.tupleToObjectArray("SortingTuple", tupleWithTestKeyObject);
    	DProfiler.testKey = (String)tupleWithTestKey[2];
    	
        System.out.println("Test key: " + DProfiler.testKey + " worker" + workerID);
        
        // get a task
        Object nextTaskTupleObject = getNextTask(masterGS, localGS);
        Object[] nextTaskTuple  = null;
        QSort qs = null;
        String status = null;
        if(nextTaskTupleObject!= null) {
        	nextTaskTuple = localGS.tupleToObjectArray("SortingTuple", nextTaskTupleObject);
        	qs = (QSort)nextTaskTuple[1];
        	status = (String)nextTaskTuple[2];
        }
        while(!status.equals("finished")) {
      
            System.out.println("Sorting " + qs.getData().length + " items.");
            TupleLogger.begin("QSortWorker::Sorting");
            QSort.insertionSort(qs.getData());
            TupleLogger.end("QSortWorker::Sorting");
                
            TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("SortingTuple", 
            		new Object[]{"sorting", qs, "sorted"}, DistributedSortMasterV2.sortingTupleTemplate), false, true);   

            // try to find unsorted partition to work on
            nextTaskTupleObject = getNextTask(masterGS, localGS);
	    
            if(nextTaskTupleObject!= null) {
            	nextTaskTuple = localGS.tupleToObjectArray("SortingTuple", nextTaskTupleObject);
            	qs = (QSort)nextTaskTuple[1];
            	status = (String)nextTaskTuple[2];
            }
        }

        TupleLogger.writeAllToFile(DProfiler.testKey);
    	// indicate that process is finished
    	TupleOperations.writeTuple(masterGS, localGS,  localGS.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, DistributedMatrixMaster.operationTemplate), false, false);
        System.out.println("Worker " + workerID + " finished its work");
    } 
    
    static Object getNextTask(ITupleSpace masterGS, ITupleSpace localGS) throws InterruptedException
    {
    	Object template = localGS.formTuple("SortingTuple", new Object[]{"sort_array", null, null}, DistributedSortMasterV2.sortingTupleTemplate);
    	Object resultTuple = TupleOperations.takeTuple(masterGS, localGS, template, false, true); 	
    	return resultTuple;
    	
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
