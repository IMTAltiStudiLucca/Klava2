package apps.dist.sorting.implementations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

import app.dist.sorting.DistributedSortMasterThread;
import app.dist.sorting.DistributedSortWorkerThread;
import common.DataGeneration;
import proxy.gigaspaces.GigaSpaceProxy;


public class GigaspacesImpDistSortTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		
		int numberOfElements = 10000;
		int numberOfWorkers = 5;
		
		// for network version
		String roleName = "worker1";
    	
		if(args.length == 2)
		{
			numberOfElements = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
		}
		
		if(args.length == 3)
		{
			numberOfElements = Integer.valueOf(args[0]);
			numberOfWorkers = Integer.valueOf(args[1]);
			roleName = args[2];
		}
		
		double thresholdPercent = 0.01;
		// load data
        String data = null;
        int[] dataArray = null;
        int threshold = 100;
        
       // if(roleName.contains("master"))
        {
			try {
				data = DataGeneration.readStringFromFile("dataSample_" + numberOfElements + ".dat");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			dataArray = DataGeneration.fromStringToIntArray(data);
	        threshold = (int)(Double.valueOf(dataArray.length) * thresholdPercent);
        }
		
		// begin test scenario
       // beginScenarioNetVersion(numberOfWorkers, dataArray, roleName);
        beginScenario(numberOfWorkers, dataArray);
		
		System.out.println("done");
	}

	
	private static void beginScenarioNetVersion(int numberOfWorkers, int[] dataToSort, String roleName) throws FileNotFoundException, IOException
	{			
		// start tuple space server		    
		// e.g. : jini://10.180.53.36:4174/*/
		
		String infoFileName = "net.info";
		
		// starting space
		GigaSpaceProxy spaceGS = new GigaSpaceProxy();
		spaceGS.startTupleSpace("/./" + roleName);
		
		System.out.println("waiting for the typing ...");
		waitForThePressingKey(1);
		//System.in.read();
		
		String masterServerIP = getIP("net.info", "master");
		String masterSpaceAddress = "jini://" + masterServerIP + "/*/master";
		
		if(roleName.equals("master"))
		{		
			// start master thread
			DistributedSortMasterThread<GigaSpaceProxy> mThread = new DistributedSortMasterThread<GigaSpaceProxy>(masterSpaceAddress, numberOfWorkers, dataToSort, GigaSpaceProxy.class); 
	        mThread.start();
		} else
		{
			int workerID = Integer.valueOf(roleName.replace("worker", "").trim());
			String workerIP = getIP("net.info", roleName);
			String workerSpaceAddress = "jini://" + workerIP + "/*/" + roleName;
					
			ArrayList<Object> otherWorkerTSName = new ArrayList<Object>();
			for(int i = 0; i < numberOfWorkers; i++)
			{
				if(workerID != i) {
					String otherWorkerName = "worker" + i;
					String otherWorkerIP = getIP(infoFileName, otherWorkerName);	
					String otherWorkerSpaceAddress = "jini://" + otherWorkerIP + "/*/" + otherWorkerName;
					otherWorkerTSName.add(otherWorkerSpaceAddress);
				}
			}
			DistributedSortWorkerThread<GigaSpaceProxy> wThread = new DistributedSortWorkerThread<GigaSpaceProxy>(workerSpaceAddress, workerID, masterSpaceAddress, otherWorkerTSName, 0, numberOfWorkers, GigaSpaceProxy.class);       
	        wThread.start();
		}

        
        System.out.println("process creation is finished");
	}
	
	private static void beginScenario(int numberOfWorkers, int[] dataToSort)
	{			
		// start tuple space server		
		String serverPath = "/./"; //"/./";   "jini://*/*/"     jini://10.180.53.36:4174/*/
		String masterServerPath = "/./" + "master";
		
		GigaSpaceProxy masterGS = new GigaSpaceProxy();
		masterGS.startTupleSpace(masterServerPath);
      		
		ArrayList<Object> allWorkers = new ArrayList<Object>();
		for(int k = 0; k < numberOfWorkers; k++)
		{
			String workerGSName = serverPath + "worker" + k;
			allWorkers.add(serverPath + "worker" + k);
			GigaSpaceProxy workerGS = new GigaSpaceProxy();
			workerGS.startTupleSpace(workerGSName);
		}

		// start worker Threads
		for(int i=0; i < numberOfWorkers; i++)
		{
			ArrayList<Object> otherWorkerTSName = new ArrayList<Object>();
			for(int k = 0; k < numberOfWorkers; k++)
			{
				if(i != k)
					otherWorkerTSName.add(allWorkers.get(k));
			}
			DistributedSortWorkerThread<GigaSpaceProxy> wThread = new DistributedSortWorkerThread<GigaSpaceProxy>(allWorkers.get(i), i, masterServerPath, otherWorkerTSName, 0, numberOfWorkers, GigaSpaceProxy.class);       
	        wThread.start();
		}

		// start master thread
		DistributedSortMasterThread<GigaSpaceProxy> mThread = new DistributedSortMasterThread<GigaSpaceProxy>(masterServerPath, numberOfWorkers, dataToSort, GigaSpaceProxy.class); 
        mThread.start();
       
        
        System.out.println("process creation is finished");
	}
	
	
	//+ func for vozvrat workers
	
	public static String getIP(String fileName, String roleName) throws FileNotFoundException, IOException
	{
		String IP = null;

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if(line.startsWith(roleName))
		    	{
		    		line = line.replace(roleName, "");
		    		line = line.trim();
		    		IP = line;
		    		return IP;
		    				
		    	}		    	
		    }
		}
		return null;
	}
	
	public static void waitForThePressingKey(int number) throws IOException
	{
		for(;;)
		{
			Scanner scanner =  new Scanner(System.in);
			int inputValue = scanner.nextInt();
			if(inputValue == number)
				break;
		}
	}


}




