package app.launcher.matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.mikado.imc.common.IMCException;

import app.skeleton.matrix.DistributedMatrixMasterThread;
import app.skeleton.matrix.DistributedMatrixWorkerThread;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.topology.KlavaNode;
import proxy.klaim.KlaimProxy;

public class KlaimMatrixTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

		int matrixSize = 50;
		int numberOfWorkers = 5;
		
		if(args.length == 2)
		{
			matrixSize = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
		}		
		
		try {
			beginScenario(matrixSize, numberOfWorkers);
		} catch (IMCException | KlavaException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("done");
	}

	
	private static void beginScenario(int matrixSize, int numberOfWorkers) throws IMCException, KlavaException, InterruptedException
	{					
		PhysicalLocality serverPLoc = new PhysicalLocality("tcp-127.0.0.1:6001");
		KlavaNode serverNode = new KlavaNode(serverPLoc);
		
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
			DistributedMatrixWorkerThread<KlaimProxy> workerThread = new DistributedMatrixWorkerThread<KlaimProxy>(workerNodes.get(i), i, serverNode, nodesExceptThatNodeList, matrixSize, numberOfWorkers, KlaimProxy.class);
			workerThread.start();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		DistributedMatrixMasterThread<KlaimProxy> mThread = new DistributedMatrixMasterThread<KlaimProxy>(serverNode, matrixSize, numberOfWorkers, KlaimProxy.class);
		mThread.start();

	}
	


}




