package app.skeleton.passwordsearch;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import interfaces.ITupleSpace;


public class DistributedSearchWorkerThread<T extends ITupleSpace> extends Thread
{
	Object localSpace;
	Integer workerID;
	
	Object masterSpace;
	ArrayList<Object> otherWorkerTSName;
	
	int matrixSize;
	int numberOfWorkers;
	Class<?> tupleSpaceClass;

	public DistributedSearchWorkerThread(Object localSpace, Integer workerID, Object masterSpace, ArrayList<Object> otherWorkerTSName, int matrixSize, int numberOfWorkers, Class<?> tupleSpaceClass)
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
		new DistributedSearchWorker<T>(localSpace, workerID, masterSpace, otherWorkerTSName, matrixSize, numberOfWorkers, tupleSpaceClass).passwordSearchWorker();
	} catch (NoSuchAlgorithmException | IOException | InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
}