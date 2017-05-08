package profiler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import common.Statistics;
import common.TupleLogger;

public class TestLogger 
{
	public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException, URISyntaxException
	{
	//	writeToLogTest();
		
		//String executionFolder = System.getProperty("user.dir");
		//DProfiler.printStatistics(executionFolder, "b7873bb8-3e19-4a19-a486-23fe6fb42d6b", new String[]{"take::remote", "take::local", "write::remote", "take::local", "nodeVisited", "Master::TotalRuntime"});
		
		
		Path executionFolder = Paths.get(TestLogger.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
		System.out.println(executionFolder.toString());
		
	//	combineResultsWithTestFile(executionFolder.toString(), new String[]{"write::local", "read::local", "take::local", "total::local"});
		
		combineResultsWithTestFile(executionFolder.toString(), new String[]{"take::local", "take::remote", "write::local", "write::remote", "takeE::local",
				"takeE::remote", "read::l-r", "inner-takeE::remote", "inner-takeE::local", "nodeVisited", "Master::TotalRuntime"});
		
	//	String[] operationKeyArray = new String[]{"op_write", "op_read"};
	//	getAllDataFromLogsTest("C:\\oneFolder\\OneDrive\\OneFolder\\IMT\\FP_Comp\\JavaPr\\MatrixOperation", "test1", operationKeyArray);
		//getAllDataFromLogsTest("C:\\oneFolder\\OneDrive\\OneFolder\\IMT\\FP_Comp\\JavaPr\\MatrixOperation", "test1", "op_write");
	}
	
	
	
	private static void combineResultsWithTestFile(String folderName, String[] operationKeyArray) throws FileNotFoundException, IOException	
	{
		String testFileName = Paths.get(folderName, "testkeys.tklf").toString();
		// load all keys
		ArrayList<String> testKeyList = new ArrayList<String>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(testFileName))) {
		    String line;
		    while ((line = br.readLine()) != null) 
		    {
		    	if(!line.startsWith("#"))
		    	{
		    		line = line.trim();
		    		if(line.length() > 0)
		    			testKeyList.add(line);
		    	}		    	
		    }
		}
		
		String[] testKeyArray = new String[testKeyList.size()];
		testKeyArray = testKeyList.toArray(testKeyArray);
		
		TupleLogger.printStatistics(folderName, testKeyArray, operationKeyArray);		
	}

	
	private static void getAllDataFromLogsTest(String folderName, String testKey, String[] operationKeyArray)	
	{
		for(int i =0; i < operationKeyArray.length; i++)
		{
			getAllDataFromLogsTest(folderName, testKey, operationKeyArray[i]);
		}
		
	}
	
	private static void getAllDataFromLogsTest(String folderName, String testKey, String operationKey)
	{
		try {
			ArrayList<Double> data = TupleLogger.collectDataFromLogs(folderName, testKey, operationKey);
			Double[] rawData = TupleLogger.convertListToArray(data);
			rawData = TupleLogger.devideArrayElements(rawData, 1000d);
					
			//Statistics st = new Statistics((Double[])data.toArray());
			
			System.out.println("Operation '" + operationKey + "'");			
			
			Double average =  Statistics.getMean(rawData);
			System.out.println("count = " + rawData.length + ", average = " + average);
			
			Double deviation =  Statistics.getStdDev(rawData);
			System.out.println("count = " + rawData.length + ", deviation = " + deviation);
			
			Double median =  Statistics.getMedian(rawData);
			System.out.println("count = " + rawData.length + ", median = " + median);		
			
			Double max =  Statistics.getMax(rawData);
			System.out.println("count = " + rawData.length + ", max = " + max);	
			
			Double min =  Statistics.getMin(rawData);
			System.out.println("count = " + rawData.length + ", min = " + min);	
			
			System.out.println();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	

}
