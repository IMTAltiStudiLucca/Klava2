package app.dist.matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import interfaces.ITupleSpace;


public class DistributedMatrixMasterThread<T extends ITupleSpace> extends Thread
{
	private Object masterGSName;
	int matrixSize;
	int numberOfWorkers;
	Class<?> tupleSpaceClass;

	public DistributedMatrixMasterThread(Object masterGSName, int matrixSize, int numberOfWorkers, Class<?> tupleSpaceClass)
	{
		this.masterGSName = masterGSName;
		this.matrixSize = matrixSize;
		this.numberOfWorkers = numberOfWorkers;
		this.tupleSpaceClass = tupleSpaceClass;
	}
	   
   @Override
    public void run()  {
		try {
			new DistributedMatrixMaster<T>(masterGSName, matrixSize, numberOfWorkers, tupleSpaceClass).start();
		} catch (NoSuchAlgorithmException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}