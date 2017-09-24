package app.skeleton.passwordsearch;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import interfaces.ITupleSpace;


public class DistributedSearchMasterThread<T extends ITupleSpace> extends Thread
{
	private Object masterGSName;
	int numberOfElements;
	int numberOfWorkers;
	Class<?> tupleSpaceClass;

	public DistributedSearchMasterThread(Object masterGSName, int numberOfElements, int numberOfWorkers, Class<?> tupleSpaceClass)
	{
		this.masterGSName = masterGSName;
		this.numberOfElements = numberOfElements;
		this.numberOfWorkers = numberOfWorkers;
		this.tupleSpaceClass = tupleSpaceClass;
	}
	   
   @Override
    public void run()  {
		try {
			new DistributedSearchMaster<T>(masterGSName, numberOfElements, numberOfWorkers, tupleSpaceClass).passwordSearchMaster();
		} catch (NoSuchAlgorithmException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}