package app.launcher.ocean;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.mikado.imc.common.IMCException;

import app.skeleton.ocean.DistributedOceanModelMasterThread;
import app.skeleton.ocean.DistributedOceanModelWorkerThread;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.topology.KlavaNode;
import proxy.klaim.KlaimProxy;


public class KlaimOceanModelTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

		int gridSize = 200;
		int numberOfWorkers = 5;
		
		if(args.length == 2)
		{
			gridSize = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
		}		
		
		try {
			beginScenario(gridSize, numberOfWorkers);
		} catch (IMCException | KlavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("done");
	}

	
	private static void beginScenario(int gridSize, int numberOfWorkers) throws IMCException, KlavaException
	{			
		// create server physical locality
		PhysicalLocality serverPLoc = new PhysicalLocality("tcp-127.0.0.1:6001");
		KlavaNode serverNode = new KlavaNode(serverPLoc);
		
		// create all worker nodes
		ArrayList<KlavaNode> workerNodes = new ArrayList<KlavaNode>();
		for(int i = 0; i< numberOfWorkers; i++ )
		{
			PhysicalLocality workPLoc = new PhysicalLocality("tcp-127.0.0.1:600" + (i+2));
			KlavaNode workerNode = new KlavaNode(workPLoc); 
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
			DistributedOceanModelWorkerThread<KlaimProxy> workerThread = new DistributedOceanModelWorkerThread<KlaimProxy>(workerNodes.get(i), i, serverNode, nodesExceptThatNodeList, gridSize, numberOfWorkers, KlaimProxy.class);
			workerThread.start();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		DistributedOceanModelMasterThread<KlaimProxy> mThread = new DistributedOceanModelMasterThread<KlaimProxy>(serverNode, gridSize, numberOfWorkers, KlaimProxy.class);
		mThread.start();
		
        System.out.println("process creation is finished");

	}
	


}




