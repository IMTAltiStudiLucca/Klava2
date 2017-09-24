package app.launcher.matrix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;

import app.skeleton.matrix.DistributedMatrixMaster;
import app.skeleton.matrix.DistributedMatrixWorker;
import proxy.gigaspaces.GigaSpaceProxy;


public class GigaspacesMatrixDocker {

	public static int gigaspacesPort = 4174;
	public static int ipShift = 0;
	
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

	private static void startNetworkClient(int matrixSize, int numberOfWorkers, String ipBase) throws FileNotFoundException, IOException, InterruptedException, NoSuchAlgorithmException
	{			
		String currentIP = getDockerIP();
		System.out.println("currentIP:" + currentIP);
				
		String masterIP = ipBase + "." + (1 + ipShift);
		//String masterIPAddress = masterIP + ":" + String.valueOf(gigaspacesPort);
		String masterSpaceAddress = "jini://" + masterIP + "/*/master";
		
		System.out.println("masterSpaceAddress:" + masterSpaceAddress);
		
		String localSpaceAddress = "jini://" + currentIP + ":" + String.valueOf(gigaspacesPort)+ "/*/client";
				
		// starting space
	//	GigaSpaceProxy spaceGS = new GigaSpaceProxy();
	//	spaceGS.startTupleSpace("/./client");

		//Thread.sleep(5000 + numberOfWorkers*1000);
		
		if(currentIP.equals(masterIP))
		{		
			System.out.println("starting master process");
			// start master thread
			//DistributedMatrixMasterNetThread<GigaSpaceProxy> mThread = new DistributedMatrixMasterNetThread<GigaSpaceProxy>(localSpaceAddress, matrixSize, numberOfWorkers,  GigaSpaceProxy.class); 
	        //mThread.start();
			
			new DistributedMatrixMaster<GigaSpaceProxy>(masterSpaceAddress, matrixSize, numberOfWorkers, GigaSpaceProxy.class).start();
			
		} else
		{
			Thread.sleep(5000);
			
			Integer workerID = Integer.valueOf(currentIP.replaceAll(ipBase + ".", "")) - (1 + 1 + ipShift);
					
			ArrayList<Object> otherWorkerTSName = new ArrayList<Object>();
			for(int i = 0; i < numberOfWorkers; i++)
			{
				if(workerID != i) {
					String otherWorkerIP = ipBase + "." + String.valueOf(i + 1 + 1 + ipShift); //  + ":" + String.valueOf(gigaspacesPort);
					String otherWorkerSpaceAddress = "jini://" + otherWorkerIP + "/*/client";
					otherWorkerTSName.add(otherWorkerSpaceAddress);
				}
			}
		//	DistributedMatrixWorkerNetThread<GigaSpaceProxy> wThread = new DistributedMatrixWorkerNetThread<GigaSpaceProxy>(localSpaceAddress, workerID, masterSpaceAddress, otherWorkerTSName, matrixSize, numberOfWorkers, GigaSpaceProxy.class);       
	    //    wThread.start();
			new DistributedMatrixWorker<GigaSpaceProxy>(localSpaceAddress, workerID, masterSpaceAddress, otherWorkerTSName, matrixSize, numberOfWorkers, GigaSpaceProxy.class).start();
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



