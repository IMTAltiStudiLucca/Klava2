package app.launcher.search.implementations;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import app.skeleton.passwordsearch.DistributedSearchMasterThread;
import app.skeleton.passwordsearch.DistributedSearchWorkerThread;
import common.DataGeneration;
import proxy.gigaspaces.GigaSpaceProxy;


public class GigaspacesImpDistSearchTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		
		int numberOfElements = 1000;
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
			datasetGeneration(numberOfElements);
			
			// devide whole data set into several parts
			divideArray(fileName, numberOfWorkers);
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
		/*// start tuple space server		
		String serverPath = "/./";
		String masterServerPath = serverPath + "master";
		
		GigaSpaceProxy masterTS = new GigaSpaceProxy();
		masterTS.startTupleSpace(masterServerPath);
      		
		ArrayList<String> allWorkers = new ArrayList<String>();
		for(int k = 0; k < numberOfWorkers; k++)
		{
			String workerTSName = serverPath + "worker" + k;
			allWorkers.add(serverPath + "worker" + k);
			GigaSpaceProxy workerTS = new GigaSpaceProxy();
			workerTS.startTupleSpace(workerTSName);
		}*/
		
		String basicAddress = "/./";
		String masterAddress = basicAddress + "master";
		
		ArrayList<String> allWorkers = new ArrayList<String>();
		for(int k = 0; k < numberOfWorkers; k++)
		{
			allWorkers.add(basicAddress + "worker" + k);
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
			DistributedSearchWorkerThread<GigaSpaceProxy> wThread = new DistributedSearchWorkerThread<GigaSpaceProxy>(allWorkers.get(i), i, masterAddress, otherWorkerTSName, numberOfElements, numberOfWorkers, GigaSpaceProxy.class);       
	        wThread.start();
		}

		// start master thread
		DistributedSearchMasterThread<GigaSpaceProxy> mThread = new DistributedSearchMasterThread<GigaSpaceProxy>(masterAddress, numberOfElements, numberOfWorkers,  GigaSpaceProxy.class); 
        mThread.start();
       
        
        System.out.println("process creation is finished");
	}


	public static void datasetGeneration(int _elementsNumber) throws NoSuchAlgorithmException {
		// generate data set
		MessageDigest mdEnc = MessageDigest.getInstance("MD5"); 
		
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < _elementsNumber; i++)
		{
			String md5 = GigaspacesImpDistSearchTest.integerToHashString(mdEnc, i);
			sb.append(md5 + "," + i + "\n");

		}
		DataGeneration.writeStringToFile("hashSet.dat", sb.toString());
	}
	
	public static void divideArray(String fileName, int numberOfParts) throws IOException {

		String[] originalDataArray = GigaspacesImpDistSearchTest.getStringArray(fileName);
		
		int numberOfRecords = originalDataArray.length / numberOfParts;

		for(int p = 0; p < numberOfParts; p++)
		{
			int upperBorder = (p != numberOfParts -1) ? numberOfRecords * (p + 1) : originalDataArray.length;
			StringBuffer fileStringBuffer = new StringBuffer();
			for(int i = numberOfRecords * p; i < upperBorder; i++)
			{
				fileStringBuffer.append(originalDataArray[i] + "\n");
			}
			DataGeneration.writeStringToFile("hashSet" + p + ".dat", fileStringBuffer.toString());
		}

	}
	
	

	public static String[] getStringArray(String fileName) throws IOException {
		String str = DataGeneration.readLineStringFromFile(fileName);
		String[] originalDataArray = str.split("\n");
		return originalDataArray;
	}

	public static String integerToHashString(MessageDigest mdEnc, int number) {
		String str = String.valueOf(number);
		mdEnc.update(str.getBytes(), 0, str.length());
	//	String ms = mdEnc.toString();
		//String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
		byte[] hash = mdEnc.digest();
		StringBuffer md5 = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
		    if ((0xff & hash[i]) < 0x10) {
		    	md5.append("0"
		                + Integer.toHexString((0xFF & hash[i])));
		    } else {
		    	md5.append(Integer.toHexString(0xFF & hash[i]));
		    }
		}
		String md5Value = md5.toString();
		return md5Value;
	}
	


}




