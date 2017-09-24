package app.skeleton.sorting.v1;

import java.util.ArrayList;

import interfaces.ITupleSpace;


public class DistributedSortWorkerThread<T extends ITupleSpace> extends Thread
{
	Object localSpace;
	Integer workerID;
	
	Object masterSpace;
	ArrayList<Object> otherWorkerTSName;
	
	int numberOfWorkers;
	Class<?> tupleSpaceClass;

	public DistributedSortWorkerThread(Object localSpace, Integer workerID, Object masterSpace, ArrayList<Object> otherWorkerTSName, int numberOfWorkers, Class<?> tupleSpaceClass)
	{
		this.localSpace = localSpace;
		this.workerID = workerID;
		this.masterSpace = masterSpace;
		this.otherWorkerTSName = otherWorkerTSName;
		this.numberOfWorkers = numberOfWorkers;
		this.tupleSpaceClass = tupleSpaceClass;
	}
	   
   @Override
    public void run()  {
	   try {
		new DistributedSortWorker<T>(localSpace, workerID, masterSpace, otherWorkerTSName, numberOfWorkers, tupleSpaceClass).start();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
}