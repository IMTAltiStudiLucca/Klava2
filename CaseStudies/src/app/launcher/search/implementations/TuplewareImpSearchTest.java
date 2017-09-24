package app.launcher.search.implementations;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.skeleton.passwordsearch.DistributedSearchMasterThread;
import app.skeleton.passwordsearch.DistributedSearchWorkerThread;
import proxy.tupleware.TuplewareProxy;


public class TuplewareImpSearchTest {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException 
	{
		int numberOfElements = 100000;
		int numberOfWorkers = 1;
		int mode = 1;
    	
		if(args.length == 3)
		{
			numberOfElements = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
			mode = Integer.valueOf(args[2]);
		}
		
		String fileName = "hashSet.dat";
		
		if(mode == 0) 
		{
			// create whole data set
			GigaspacesImpDistSearchTest.datasetGeneration(numberOfElements);
			
			// devide whole data set into several parts
			GigaspacesImpDistSearchTest.divideArray(fileName, numberOfWorkers);
		} 
		else 
		{
			// begin test scenario
			beginScenario(numberOfElements, numberOfWorkers);
		}
		
		System.out.println("done");
	}

	
	private static void beginScenario(int numberOfElements, int numberOfWorkers)
	{			
		int portNumber = 6001;
		// start tuple space server			
		// start master thread
		DistributedSearchMasterThread<TuplewareProxy> mThread = new DistributedSearchMasterThread<TuplewareProxy>(portNumber, numberOfElements, numberOfWorkers,  TuplewareProxy.class); 
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
			DistributedSearchWorkerThread<TuplewareProxy> wThread = new DistributedSearchWorkerThread<TuplewareProxy>(portNumber + 1 + i, i, 6001, allWorkers, numberOfElements, numberOfWorkers, TuplewareProxy.class);       
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




