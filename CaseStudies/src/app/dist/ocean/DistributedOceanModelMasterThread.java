package app.dist.ocean;

import java.io.IOException;

import interfaces.ITupleSpace;


public class DistributedOceanModelMasterThread<T extends ITupleSpace> extends Thread
{
	private Object masterGSName;
	int matrixSize;
	int numberOfWorkers;
	Class tupleSpaceClass;

	public DistributedOceanModelMasterThread(Object masterGSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
	{
		this.masterGSName = masterGSName;
		this.matrixSize = matrixSize;
		this.numberOfWorkers = numberOfWorkers;
		this.tupleSpaceClass = tupleSpaceClass;
	}
	   
   @Override
    public void run()  {
		try {
			new DistributedOceanModelMaster<T>(masterGSName, matrixSize, numberOfWorkers, tupleSpaceClass).start();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}