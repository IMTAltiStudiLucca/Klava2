package app.launcher.ocean;

import java.io.IOException;
import java.util.ArrayList;

import app.skeleton.ocean.DistributedOceanModelMasterThread;
import app.skeleton.ocean.DistributedOceanModelWorkerThread;
import proxy.gigaspaces.GigaSpaceProxy;


public class GigaspacesDistOceanModelTest {

	
	public static void main(String[] args) throws IOException {
		
		int gridSize = 800;
		int numberOfWorkers = 1;
		
		// for network version
		String roleName = "worker1";
		
		if(args.length == 2)
		{
			gridSize = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
		} 
		else if(args.length == 3)
		{
			gridSize = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
			roleName = args[2];
		}
		
		beginScenario(gridSize, numberOfWorkers);
		
		System.out.println("done");
	}

	
	private static void beginScenario(int gridSize, int numberOfWorkers)
	{			
		// start tuple space server		
		String serverPath = "/./";
		String masterServerPath = serverPath + "master";
		
		GigaSpaceProxy masterGS = new GigaSpaceProxy();
		masterGS.startTupleSpace(masterServerPath);
      		
		ArrayList<String> allWorkers = new ArrayList<String>();
		for(int k = 0; k < numberOfWorkers; k++)
		{
			String workerGSName = serverPath + "worker" + k;
			allWorkers.add(serverPath + "worker" + k);
			GigaSpaceProxy workerGS = new GigaSpaceProxy();
			workerGS.startTupleSpace(workerGSName);
		}

		// start worker Threads
		for(int i=0; i < numberOfWorkers; i++)
		{
			ArrayList<Object> otherWorkerTSName = new ArrayList<Object>();
			for(int k = 0; k < numberOfWorkers; k++)
			{
				if(i != k)
					otherWorkerTSName.add(allWorkers.get(k));
			}
			DistributedOceanModelWorkerThread<GigaSpaceProxy> wThread = new DistributedOceanModelWorkerThread<GigaSpaceProxy>(allWorkers.get(i), i, masterServerPath, otherWorkerTSName, gridSize, numberOfWorkers, GigaSpaceProxy.class);       
	        wThread.start();
		}

		// start master thread
		DistributedOceanModelMasterThread<GigaSpaceProxy> mThread = new DistributedOceanModelMasterThread<GigaSpaceProxy>(masterServerPath, gridSize, numberOfWorkers,  GigaSpaceProxy.class); 
        mThread.start();
       
        
        System.out.println("process creation is finished");
	}
	


}




