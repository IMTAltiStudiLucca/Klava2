package apps.dist.search.implementations;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.mikado.imc.common.IMCException;

import app.dist.passwordsearch.DistributedSearchMasterThread;
import app.dist.passwordsearch.DistributedSearchWorkerThread;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.topology.ClientNode;
import klava.topology.KlavaNode;
import klava.topology.Net;
import proxy.klaim.KlaimProxy;

public class KlaimImpSearchTest {
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, IMCException, KlavaException {

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

	
	private static void beginScenario(int numberOfElements, int numberOfWorkers) throws IMCException, KlavaException
	{			
		// create server physical locality
		//PhysicalLocality serverPLoc = new PhysicalLocality("tcp-127.0.0.1:6001");
		//KlavaNode serverNode = new KlavaNode(serverPLoc, TupleSpaceVector.class); //new Net(serverPLoc);  TupleSpaceVector
		
		PhysicalLocality serverPLoc = new PhysicalLocality("tcp-127.0.0.1:6001");
		KlavaNode serverNode = new Net(serverPLoc);
		
		Hashtable<String, ArrayList<Object>> settings = new Hashtable<>();
		
		// settings for TupleSpaceSQLiteDB
	    ArrayList<Object> searchTupleTemplate = new ArrayList<Object>(Arrays.asList(String.class, String.class, String.class));
		settings.put("SearchTuple", searchTupleTemplate);
	    
		// settings for hashtable (also for redis)
		//B[] searchTupleTemplate = new Object[]{String.class, String.class, String.class};
	/*	settings.put("SearchTuple_ttt", new Boolean[]{true, true, true});	 
		settings.put("SearchTuple_ttf", new Boolean[]{true, true, false});	 
		settings.put("SearchTuple_tff", new Boolean[]{true, false, false});
		TupleSpaceRedisDB.clearDB();*/

		serverNode.setSettings(settings);
		
		// create all worker nodes
		ArrayList<KlavaNode> workerNodes = new ArrayList<KlavaNode>();
		for(int i = 0; i< numberOfWorkers; i++ )
		{
			//PhysicalLocality workPLoc = new PhysicalLocality("tcp-127.0.0.1:600" + (i+2));
			//KlavaNode workerNode = new KlavaNode(workPLoc, TupleSpaceVector.class); // new ClientNode(serverPLoc);
			KlavaNode workerNode = new ClientNode(serverPLoc);
			//workerNode.setSettings(settings);
			workerNodes.add(workerNode);
		}
		
		for(int i = 0; i< numberOfWorkers; i++ )
		{
			ArrayList<Object> nodesExceptThatNodeList = new ArrayList<Object>();
			for(int n = 0; n < workerNodes.size(); n++)
			{
				if(i != n)
					nodesExceptThatNodeList.add(workerNodes.get(n));
			}
			DistributedSearchWorkerThread<KlaimProxy> workerThread = new DistributedSearchWorkerThread<KlaimProxy>(workerNodes.get(i), i, serverNode, nodesExceptThatNodeList, numberOfElements, numberOfWorkers, KlaimProxy.class);
			workerThread.start();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		DistributedSearchMasterThread<KlaimProxy> mThread = new DistributedSearchMasterThread<KlaimProxy>(serverNode, numberOfElements, numberOfWorkers, KlaimProxy.class);
		mThread.start();
		
        System.out.println("process creation is finished");

	}
	


}




