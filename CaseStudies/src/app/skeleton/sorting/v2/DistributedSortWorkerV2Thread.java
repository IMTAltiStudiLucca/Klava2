package app.skeleton.sorting.v2;

import java.util.ArrayList;

import interfaces.ITupleSpace;


public class DistributedSortWorkerV2Thread<T extends ITupleSpace> extends Thread
{
	Object localSpace;
	Integer workerID;
	
	Object masterSpace;
	ArrayList<Object> otherWorkerTSName;
	
	int matrixSize;
	int numberOfWorkers;
	Class tupleSpaceClass;

	public DistributedSortWorkerV2Thread(Object localSpace, Integer workerID, Object masterSpace, ArrayList<Object> otherWorkerTSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
	{
		this.localSpace = localSpace;
		this.workerID = workerID;
		this.masterSpace = masterSpace;
		this.otherWorkerTSName = otherWorkerTSName;
		this.matrixSize = matrixSize;
		this.numberOfWorkers = numberOfWorkers;
		this.tupleSpaceClass = tupleSpaceClass;
	}
	   
   @Override
    public void run()  {
	   try {
		new DistributedSortWorkerV2<T>(localSpace, workerID, masterSpace, otherWorkerTSName, matrixSize, numberOfWorkers, tupleSpaceClass).start();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
}