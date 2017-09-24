package app.launcher.ocean.implementations;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.skeleton.ocean.DistributedOceanModelMasterThread;
import app.skeleton.ocean.DistributedOceanModelWorkerThread;
import proxy.tupleware.TuplewareProxy;


public class TuplewareOceanModelTest {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException 
	{
		
		int gridSize = 600;
		int numberOfWorkers = 5;
		
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
		int portNumber = 6001;
		// start tuple space server			
		// start master thread
		DistributedOceanModelMasterThread<TuplewareProxy> mThread = new DistributedOceanModelMasterThread<TuplewareProxy>(portNumber, gridSize, numberOfWorkers,  TuplewareProxy.class); 
        mThread.start();
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      		
		// start worker Threads
		ArrayList<Object> allWorkers = new ArrayList<Object>();		
		for(int i=0; i < numberOfWorkers; i++)
		{
			DistributedOceanModelWorkerThread<TuplewareProxy> wThread = new DistributedOceanModelWorkerThread<TuplewareProxy>(portNumber + 1 + i, i, 6001, allWorkers, gridSize, numberOfWorkers, TuplewareProxy.class);       
	        wThread.start();
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
     
        System.out.println("process creation is finished");
	}
	


}




