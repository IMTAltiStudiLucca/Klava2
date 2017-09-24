package app.launcher.matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.skeleton.matrix.DistributedMatrixMasterThread;
import app.skeleton.matrix.DistributedMatrixWorkerThread;
import proxy.tupleware.TuplewareProxy;


public class TuplewareMatrixTest {

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
		int portNumber = 6001;
		// start tuple space server			
		// start master thread
		DistributedMatrixMasterThread<TuplewareProxy> mThread = new DistributedMatrixMasterThread<TuplewareProxy>(portNumber, matrixSize, numberOfWorkers,  TuplewareProxy.class); 
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
			DistributedMatrixWorkerThread<TuplewareProxy> wThread = new DistributedMatrixWorkerThread<TuplewareProxy>(portNumber + 1 + i, i, 6001, allWorkers, matrixSize, numberOfWorkers, TuplewareProxy.class);       
	        wThread.start();
	        try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
     
        System.out.println("process creation is finished");
	}
	


}




