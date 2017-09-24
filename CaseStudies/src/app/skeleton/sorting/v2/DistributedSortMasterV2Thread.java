package app.skeleton.sorting.v2;

import java.io.IOException;

import interfaces.ITupleSpace;


public class DistributedSortMasterV2Thread<T extends ITupleSpace> extends Thread
{
	private Object masterGSName;
	int numberOfWorkers;
	int[] dataArray;
	Class tupleSpaceClass;

	public DistributedSortMasterV2Thread(Object masterGSName, int numberOfWorkers, int[] dataArray, Class tupleSpaceClass)
	{
		this.masterGSName = masterGSName;
		this.numberOfWorkers = numberOfWorkers;
		this.dataArray = dataArray;
		this.tupleSpaceClass = tupleSpaceClass;
	}
	   
   @Override
    public void run()  {
		try {
			new DistributedSortMasterV2<T>(masterGSName, numberOfWorkers, dataArray, tupleSpaceClass).start();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}