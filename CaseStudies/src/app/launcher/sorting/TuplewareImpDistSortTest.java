package app.launcher.sorting;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.skeleton.sorting.v1.DistributedSortMasterThread;
import app.skeleton.sorting.v1.DistributedSortWorkerThread;
import common.DataGeneration;
import proxy.tupleware.TuplewareProxy;


public class TuplewareImpDistSortTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		
		int numberOfElements = 1000000;
		int numberOfWorkers = 5;
    	
		// for network version
		String ipAddress = "127.0.0.1:6001";
		Boolean isMaster = null;
    	
		if(args.length == 2)
		{
			numberOfElements = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
		}
		
		if(args.length == 4)
		{
			numberOfElements = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
			isMaster = Boolean.valueOf(args[2]);
			ipAddress = args[3];
		}
			
		double thresholdPercent = 0.01;
		// load data
        String data = null;
		try {
			data = DataGeneration.readStringFromFile("dataSample_" + numberOfElements + ".dat");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int[] dataArray = DataGeneration.fromStringToIntArray(data);
        int threshold = (int)(Double.valueOf(dataArray.length) * thresholdPercent);
		
		// begin test scenario
        beginScenario(numberOfWorkers, dataArray);
		
		System.out.println("done");
	}

	
	private static void beginScenario(int numberOfWorkers, int[] dataToSort)
	{			
		int portNumber = 6001;
		
		// start master thread
		DistributedSortMasterThread<TuplewareProxy> mThread = new DistributedSortMasterThread<TuplewareProxy>(portNumber, numberOfWorkers, dataToSort, TuplewareProxy.class); 
        mThread.start();
        
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      		
		// start worker Threads
		ArrayList<Object> allWorkers = new ArrayList<Object>();		
		for(int i=0; i < numberOfWorkers; i++)
		{
			DistributedSortWorkerThread<TuplewareProxy> wThread = new DistributedSortWorkerThread<TuplewareProxy>(portNumber + 1 + i, i, 6001, allWorkers, numberOfWorkers, TuplewareProxy.class);       
	        wThread.start();
	        try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        System.out.println("process creation is finished");
	}

}




