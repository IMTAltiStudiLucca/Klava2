package app.launcher.ocean;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.skeleton.ocean.DistributedOceanModelMasterThread;
import app.skeleton.ocean.DistributedOceanModelWorkerThread;
import proxy.mozartspaces.MozartProxy;


public class MozartOceanModelTest {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException 
	{
		int gridSize = 600;
		int numberOfWorkers = 15;
		
		if(args.length == 2)
		{
			gridSize = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
		}		
		
		beginScenario(gridSize, numberOfWorkers);
		
		System.out.println("done");
	}

	
	private static void beginScenario(int gridSize, int numberOfWorkers)
	{			
		
		int masterPortNumber = 6001;
		// start tuple space server			
		// start master thread
		DistributedOceanModelMasterThread<MozartProxy> mThread = new DistributedOceanModelMasterThread<MozartProxy>(masterPortNumber, gridSize, numberOfWorkers,  MozartProxy.class); 
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
			DistributedOceanModelWorkerThread<MozartProxy> workerThread = new DistributedOceanModelWorkerThread<MozartProxy>(masterPortNumber + 1 + i, i, masterPortNumber, nodesExceptThatNodeList, gridSize, numberOfWorkers, MozartProxy.class);
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




