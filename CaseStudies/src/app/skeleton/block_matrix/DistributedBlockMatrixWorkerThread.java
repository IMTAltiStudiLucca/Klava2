package app.skeleton.block_matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import interfaces.ITupleSpace;


public class DistributedBlockMatrixWorkerThread<T extends ITupleSpace> extends Thread
{
	Object localSpace;
	Integer workerID;
	
	Object masterSpace;
	ArrayList<Object> otherWorkerTSName;
	
	int matrixSize;
	int numberOfWorkers;
	Class tupleSpaceClass;

	public DistributedBlockMatrixWorkerThread(Object localSpace, Integer workerID, Object masterSpace, ArrayList<Object> otherWorkerTSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
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
		new DistributedBlockMatrixWorker<T>(localSpace, workerID, masterSpace, otherWorkerTSName, matrixSize, numberOfWorkers, tupleSpaceClass).start();
	} catch (NoSuchAlgorithmException | IOException | InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
}