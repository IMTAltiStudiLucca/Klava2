package app.dist.sorting;

import java.io.IOException;

import interfaces.ITupleSpace;

public class DistributedSortMasterThread<T extends ITupleSpace> extends Thread
{
	private Object masterGSName;
	int numberOfWorkers;
	int[] dataArray;
	Class<?> tupleSpaceClass;

	public DistributedSortMasterThread(Object masterGSName, int numberOfWorkers, int[] dataArray, Class<?> tupleSpaceClass)
	{
		this.masterGSName = masterGSName;
		this.numberOfWorkers = numberOfWorkers;
		this.dataArray = dataArray;
		this.tupleSpaceClass = tupleSpaceClass;
	}
	   
   @Override
    public void run()  {
		try {
			new DistributedSortMaster<T>(masterGSName, numberOfWorkers, dataArray, tupleSpaceClass).start();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}