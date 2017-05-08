package apps.dist.sorting.implementations;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.mikado.imc.common.IMCException;

import app.dist.sorting.DistributedSortMasterThread;
import app.dist.sorting.DistributedSortWorkerThread;
import common.DataGeneration;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.topology.ClientNode;
import klava.topology.KlavaNode;
import klava.topology.Net;
import proxy.klaim.KlaimProxy;


public class KlaimImpDistSortTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, IMCException, KlavaException {
		
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
//        int threshold = (int)(Double.valueOf(dataArray.length) * thresholdPercent);
		
		// begin test scenario
		beginScenario(numberOfWorkers, dataArray, ipAddress, isMaster);
		
		System.out.println("done");
	}

	
	private static void beginScenario(int numberOfWorkers, int[] dataToSort, String ipAddress, Boolean isMaster) throws IMCException, KlavaException
	{	
		// create server physical locality
		PhysicalLocality serverPLoc = new PhysicalLocality("tcp-127.0.0.1:6001");
		KlavaNode serverNode = new Net(serverPLoc);
		
		//
		//KlavaNode serverNode = new KlavaNode(serverPLoc, TupleSpaceVector.class);
		
		// create all worker nodes
		ArrayList<KlavaNode> workerNodes = new ArrayList<KlavaNode>();
		for(int i = 0; i< numberOfWorkers; i++ )
		{
			//PhysicalLocality workPLoc = new PhysicalLocality("tcp-127.0.0.1:600" + (i+2));
			//KlavaNode workerNode = new KlavaNode(workPLoc, TupleSpaceVector.class);
			KlavaNode workerNode = new ClientNode(serverPLoc);
			workerNodes.add(workerNode);
		}
		
		// start master thread
		DistributedSortMasterThread<KlaimProxy> mThread = new DistributedSortMasterThread<KlaimProxy>(serverNode, numberOfWorkers, dataToSort, KlaimProxy.class); 
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
			DistributedSortWorkerThread<KlaimProxy> wThread = new DistributedSortWorkerThread<KlaimProxy>(workerNodes.get(i), i, serverNode, allWorkers, 0, numberOfWorkers, KlaimProxy.class);       
	        wThread.start();
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        System.out.println("process creation is finished");
	}

}




