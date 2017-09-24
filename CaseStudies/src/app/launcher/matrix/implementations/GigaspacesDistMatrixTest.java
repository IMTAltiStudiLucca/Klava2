package apps.dist.matrix.implementations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.dist.matrix.DistributedMatrixMasterThread;
import app.dist.matrix.DistributedMatrixWorkerThread;
import apps.dist.sorting.implementations.GigaspacesImpDistSortTest;
import proxy.gigaspaces.GigaSpaceProxy;


public class GigaspacesDistMatrixTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		
		int matrixSize = 100;
		int numberOfWorkers = 1;
		
		
		if(args.length == 2)
		{
			matrixSize = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
		} 
		
		beginScenario(matrixSize, numberOfWorkers);
		
		System.out.println("done");
	}
	
	private static void beginScenario(int matrixSize, int numberOfWorkers)
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
			
			//String masterServerPathForWorkers = "jini://127.0.0.1/*/master";
			DistributedMatrixWorkerThread<GigaSpaceProxy> wThread = new DistributedMatrixWorkerThread<GigaSpaceProxy>(allWorkers.get(i), i, masterServerPath, otherWorkerTSName, matrixSize, numberOfWorkers, GigaSpaceProxy.class);       
	        wThread.start();
		}

		// start master thread
		DistributedMatrixMasterThread<GigaSpaceProxy> mThread = new DistributedMatrixMasterThread<GigaSpaceProxy>(masterServerPath, matrixSize, numberOfWorkers,  GigaSpaceProxy.class); 
        mThread.start();
       
        System.out.println("process creation is finished");
	}
	


}




