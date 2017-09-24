package app.launcher.matrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import app.skeleton.matrix.DistributedMatrixMaster;
import app.skeleton.matrix.DistributedMatrixWorker;
import proxy.gigaspaces.GigaSpaceProxy;


public class GigaspacesMatrixNetwork {

	public static int gigaspacesPort = 4174;
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InterruptedException {
		
		int matrixSize = 10;
		int numberOfWorkers = 1;
				
		if(args.length == 2)
		{
			matrixSize = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
		} else
		{
			System.err.println("Parameters are not correct");
		}
		
		startNetworkClient(matrixSize, numberOfWorkers);
	   // beginScenarioNetVersion(matrixSize, numberOfWorkers, roleName);
		
		System.out.println("done");
	}

	private static void startNetworkClient(int matrixSize, int numberOfWorkers) throws FileNotFoundException, IOException, InterruptedException, NoSuchAlgorithmException
	{			
		String currentIP = getIPByInterfaceName("enp0s3"); //enp0s3
		System.out.println("currentIP:" + currentIP);
		
		Hashtable<String, String> nodeTable = loadNodeTable();
				
		String masterIP = nodeTable.get("master");
		//String masterIPAddress = masterIP + ":" + String.valueOf(gigaspacesPort);
		String masterSpaceAddress = "jini://" + masterIP + "/*/master";
		
		System.out.println("masterSpaceAddress:" + masterSpaceAddress);
		

						
		if(currentIP.equals(masterIP)) {		
			System.out.println("starting master process");		
			new DistributedMatrixMaster<GigaSpaceProxy>(masterSpaceAddress, matrixSize, numberOfWorkers, GigaSpaceProxy.class).start();
			
		} else {
			Thread.sleep(5000);
						
			Integer workerID = getWorkerID(nodeTable, currentIP);
			
			String localSpaceAddress = "jini://" + currentIP + ":" + String.valueOf(gigaspacesPort)+ "/*/client" + String.valueOf(workerID);
			System.out.println("workerID = " + String.valueOf(workerID));
			ArrayList<Object> otherWorkerTSName = new ArrayList<Object>();
			for(int i = 0; i < numberOfWorkers; i++)
			{
				if(workerID != i) {
					String otherWorkerIP = nodeTable.get("worker" + String.valueOf(i));
					String otherWorkerSpaceAddress = "jini://" + otherWorkerIP + "/*/client" + String.valueOf(i);
					otherWorkerTSName.add(otherWorkerSpaceAddress);
				}
			}
			new DistributedMatrixWorker<GigaSpaceProxy>(localSpaceAddress, workerID, masterSpaceAddress, otherWorkerTSName, matrixSize, numberOfWorkers, GigaSpaceProxy.class).start();
		}
        
        System.out.println("process creation is finished");
	}
	
	static String getIPByInterfaceName(String interfaceName)
	{
	    Enumeration<NetworkInterface> n = null;
		try {
			n = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    for (; n.hasMoreElements();)
	    {
	        NetworkInterface e = n.nextElement();

	        if(e.getName().equals(interfaceName)) {
		        Enumeration<InetAddress> a = e.getInetAddresses();
		        for (; a.hasMoreElements();)
		        {
		            InetAddress addr = a.nextElement();
		            if(addr instanceof Inet4Address) {
		            
		            	System.out.println("IP v4  " + addr.getHostAddress()); 
		            	return addr.getHostAddress();
		            }
		        }
	        }
	    }
	    return null;
	}
	
	// table - nodeName:ip
	static Hashtable<String, String> loadNodeTable() throws FileNotFoundException, IOException
	{
		String fileName = "nodes.conf";
		Hashtable<String, String> table = new Hashtable<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       String[] parts = line.split(" ");
		       String nodeName = parts[0].trim();
		       String ip = parts[1].trim();
		       table.put(nodeName, ip);
		    }
		}
	    return table;
	}
	
	static Integer getWorkerID(Hashtable<String, String> nodeTable, String ipToSearch) {
		Integer workerID = null;
		Iterator<String> workerNamesIter = nodeTable.keySet().iterator();
		while(workerNamesIter.hasNext()) {
			String workerName = workerNamesIter.next();
			String ip = nodeTable.get(workerName);
			if(ip.equals(ipToSearch)) {
				String idStr = workerName.replace("worker", "");
				workerID = Integer.valueOf(idStr);
				break;
			}
		}
		return workerID;
	}

}



