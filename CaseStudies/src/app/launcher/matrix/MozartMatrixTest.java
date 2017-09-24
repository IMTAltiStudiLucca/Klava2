package app.launcher.matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.skeleton.matrix.DistributedMatrixMasterThread;
import app.skeleton.matrix.DistributedMatrixWorkerThread;
import proxy.mozartspaces.MozartProxy;


public class MozartMatrixTest {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException 
	{
		int matrixSize = 50;
		int numberOfWorkers = 5;
		
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
		
		int masterPortNumber = 6001;
		// start tuple space server			
		// start master thread
		DistributedMatrixMasterThread<MozartProxy> mThread = new DistributedMatrixMasterThread<MozartProxy>(masterPortNumber, matrixSize, numberOfWorkers,  MozartProxy.class); 
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
			DistributedMatrixWorkerThread<MozartProxy> workerThread = new DistributedMatrixWorkerThread<MozartProxy>(masterPortNumber + 1 + i, i, masterPortNumber, nodesExceptThatNodeList, matrixSize, numberOfWorkers, MozartProxy.class);
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




