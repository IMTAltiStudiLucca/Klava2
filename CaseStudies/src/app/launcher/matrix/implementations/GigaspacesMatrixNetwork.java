package app.launcher.matrix.implementations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;

import app.launcher.sorting.implementations.GigaspacesImpDistSortTest;
import app.skeleton.matrix.DistributedMatrixMasterThread;
import app.skeleton.matrix.DistributedMatrixWorkerThread;
import proxy.gigaspaces.GigaSpaceProxy;


public class GigaspacesMatrixNetwork {

	public static int gigaspacesPort = 4174;
	public static int ipShift = 1;
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InterruptedException {
		
		int matrixSize = 10;
		int numberOfWorkers = 1;
		String ipBase = "172.17.0";
				
		if(args.length == 3)
		{
			matrixSize = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
			ipBase = args[2];
		} else
		{
			System.err.println("Parameters are not correct");
		}
		
		startNetworkClient(matrixSize, numberOfWorkers, ipBase);
	   // beginScenarioNetVersion(matrixSize, numberOfWorkers, roleName);
		
		System.out.println("done");
	}

	private static void startNetworkClient(int matrixSize, int numberOfWorkers, String ipBase) throws FileNotFoundException, IOException, InterruptedException
	{			
		String currentIP = getDockerIP();
		System.out.println("currentIP:" + currentIP);
				
		String masterIP = ipBase + "." + (1 + ipShift);
		String masterIPAddress = masterIP + ":" + String.valueOf(gigaspacesPort);
		String masterSpaceAddress = "jini://" + masterIPAddress + "/*/client";
		
		System.out.println("masterSpaceAddress:" + masterSpaceAddress);
		
		String localSpaceAddress = "jini://" + currentIP + ":" + String.valueOf(gigaspacesPort)+ "/*/client";
				
		// starting space
		GigaSpaceProxy spaceGS = new GigaSpaceProxy();
		spaceGS.startTupleSpace("/./client");

		Thread.sleep(5000 + numberOfWorkers*1000);
		
		if(currentIP.equals(masterIP))
		{		
			// start master thread
			DistributedMatrixMasterThread<GigaSpaceProxy> mThread = new DistributedMatrixMasterThread<GigaSpaceProxy>(localSpaceAddress, matrixSize, numberOfWorkers,  GigaSpaceProxy.class); 
	        mThread.start();
		} else
		{
			Integer workerID = Integer.valueOf(currentIP.replaceAll(ipBase + ".", "")) - (1 + 1 + ipShift);
//			String workerSpaceAddress = "jini://" + workerIP + "/*/client";
					
			ArrayList<Object> otherWorkerTSName = new ArrayList<Object>();
			for(int i = 0; i < numberOfWorkers; i++)
			{
				if(workerID != i) {
					String otherWorkerIP = ipBase + "." + String.valueOf(i + 1 + 1 + ipShift) + ":" + String.valueOf(gigaspacesPort);
					String otherWorkerSpaceAddress = "jini://" + otherWorkerIP + "/*/client";
					otherWorkerTSName.add(otherWorkerSpaceAddress);
				}
			}
			DistributedMatrixWorkerThread<GigaSpaceProxy> wThread = new DistributedMatrixWorkerThread<GigaSpaceProxy>(localSpaceAddress, workerID, masterSpaceAddress, otherWorkerTSName, matrixSize, numberOfWorkers, GigaSpaceProxy.class);       
	        wThread.start();
		}
        
        System.out.println("process creation is finished");
	}
	
	static String getDockerIP()
	{
	//    System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
	    Enumeration<NetworkInterface> n = null;
		try {
			n = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    for (; n.hasMoreElements();)
	    {
	    	boolean isDocker = false;
	        NetworkInterface e = n.nextElement();

	        Enumeration<InetAddress> a = e.getInetAddresses();
	        for (; a.hasMoreElements();)
	        {
	            InetAddress addr = a.nextElement();
	            System.out.println("  " + addr.getHostAddress());
	            
	            return addr.getHostAddress();
//	            if(isDocker)
//	            	return addr.getHostAddress();
//	            if(addr.getHostAddress().contains("docker"))
//	            	isDocker = true;
	        }
	    }
	    return "";
	}

}




