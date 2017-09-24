package apps.dist.sorting.implementations;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.dist.sorting.DistributedSortMasterThread;
import app.dist.sorting.DistributedSortWorkerThread;
import common.DataGeneration;
import proxy.mozartspaces.MozartProxy;


public class MozartImpDistSortTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		
		int numberOfElements = 10000;
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
        beginScenario(numberOfWorkers, dataArray, ipAddress, isMaster);
		
		System.out.println("done");
	}

	
	private static void beginScenario(int numberOfWorkers, int[] dataToSort, String ipAddress, Boolean isMaster)
	{			
		int masterPortNumber = 6001;
		
		// start master thread
		DistributedSortMasterThread<MozartProxy> mThread = new DistributedSortMasterThread<MozartProxy>(masterPortNumber, numberOfWorkers, dataToSort, MozartProxy.class); 
        mThread.start();
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// start worker Threads	
		for(int i = 0; i< numberOfWorkers; i++ )
		{
			ArrayList<Object> nodesExceptThatNodeList = new ArrayList<Object>();
			for(int n = 0; n < numberOfWorkers; n++)
			{
				if(i != n)
					nodesExceptThatNodeList.add(masterPortNumber + 1 + n);
			}
			DistributedSortWorkerThread<MozartProxy> workerThread = new DistributedSortWorkerThread<MozartProxy>(masterPortNumber + 1 + i, i, masterPortNumber, nodesExceptThatNodeList, 0, numberOfWorkers, MozartProxy.class);
			workerThread.start();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        System.out.println("process creation is finished");
	}

}




