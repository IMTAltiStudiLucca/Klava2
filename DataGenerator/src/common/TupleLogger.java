package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import profiler.StatisticStructure;


public class TupleLogger 
{
	public static String fileExtension = ".tlf";
	
	// for each thread (process, worker) and for each operation there is single list
	public static Hashtable<String, Hashtable<String, ArrayList<TupleLogStructure>>> map;
	
	static {	 
		map = new Hashtable<String, Hashtable<String, ArrayList<TupleLogStructure>>>();	
	}
	
	public static void begin(String operationKey)
	{	
        String threadName = Thread.currentThread().getName();
        
		TupleLogStructure log = new TupleLogStructure(0d, operationKey);
		if(map.containsKey(threadName))
		{
			Hashtable<String, ArrayList<TupleLogStructure>> operationList = map.get(threadName);
			
			
			if(operationList.containsKey(operationKey))
				operationList.get(operationKey).add(log);
			else {
				operationList.put(operationKey, new ArrayList<TupleLogStructure>());
				operationList.get(operationKey).add(log);
			}
			Double startTime = (double)System.nanoTime();
			log.time = startTime;
		}
		else
		{
			Hashtable<String, ArrayList<TupleLogStructure>> operationList = new Hashtable<String, ArrayList<TupleLogStructure>>();
			ArrayList<TupleLogStructure> array = new ArrayList<TupleLogStructure>();
			array.add(log);
			operationList.put(operationKey, array);
			map.put(threadName, operationList);
			
			Double startTime = (double)System.nanoTime();
			log.time = startTime;
		}
	}
	
	public static void end(String operationKey)
	{
		long endTime = System.nanoTime();
		
		String threadName = Thread.currentThread().getName();
		if(map.containsKey(threadName))
		{
			Hashtable<String, ArrayList<TupleLogStructure>> operationList = map.get(threadName);
			ArrayList<TupleLogStructure> array = operationList.get(operationKey);
			TupleLogStructure log = array.get(array.size() - 1);
			if(log.computed)
			{
				System.out.println("TupleLogStructure logger error: end()");
			} else {
				log.time = (endTime - log.time)/1000000;
				log.computed = true;
			}
		}
	}
	
	public static void incCounter(String operationKey)
	{
        String threadName = Thread.currentThread().getName();
             
		if(map.containsKey(threadName))
		{
			Hashtable<String, ArrayList<TupleLogStructure>> operationList = map.get(threadName);

			if(operationList.containsKey(operationKey))
				operationList.get(operationKey).get(0).time++;
			else {
				operationList.put(operationKey, new ArrayList<TupleLogStructure>());
				
				TupleLogStructure log = new TupleLogStructure(1d, operationKey);
				operationList.get(operationKey).add(log);
			}
		}
		else
		{
			Hashtable<String, ArrayList<TupleLogStructure>> operationList = new Hashtable<String, ArrayList<TupleLogStructure>>();
			operationList.put(operationKey, new ArrayList<TupleLogStructure>());

			TupleLogStructure log = new TupleLogStructure(1d, operationKey);
			operationList.get(operationKey).add(log);
			
			map.put(threadName, operationList);
		}
	}
	
	public static ArrayList<TupleLogStructure> getDataList(String threadName)
	{
		if(map.containsKey(threadName)){
			ArrayList<TupleLogStructure> wholeArray = new ArrayList<TupleLogStructure>();
			Hashtable<String, ArrayList<TupleLogStructure>> operationList = map.get(threadName);
			for (String operationKey : operationList.keySet()) {
				ArrayList<TupleLogStructure> array = operationList.get(operationKey);
				wholeArray.addAll(array);
			}
			return wholeArray;
		}
		else 
			return null;
	}
		
	public static void writeAllToFile(String testKey)
	{
		String threadName = Thread.currentThread().getName();
		try{
			//URL location = TupleLogger.class.getProtectionDomain().getCodeSource().getLocation();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("tl" + threadName + ".tlf", true));  //location.getFile() +
			writer.write("#starting new test");
			writer.newLine();
			writer.write("#everything in nanoseconds");
			writer.newLine();
			writer.write("testkey=" + testKey);
			writer.newLine();
			ArrayList<TupleLogStructure> items = getDataList(threadName);
			for(int i = 0; i < items.size(); i++)
			{
				String str = GeneralMethods.concat(items.get(i).labelName, "=", items.get(i).time.toString());
				writer.write(str);
				writer.newLine();
			}
			writer.newLine();
			writer.close();
		} catch(Exception exc) {
			
		}
	}
	
	public static void writeTestKeyToFile(String testKey)
	{
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter("testkeys.tklf", true));
			writer.write("#test performed at " + GeneralMethods.getCurrrentDatetimeAsString());
			writer.newLine();
			writer.write(testKey);
			writer.newLine();
			writer.newLine();
			writer.close();
		} catch(Exception exc) {
			
		}
	}
	
	// in microseconds
	public static double getAverage(String labelName, ArrayList<TupleLogStructure> list)
	{
		Double[] rawData = getData(labelName, list);
		//Statistics st = new Statistics(rawData);
		return Statistics.getMean(rawData);
	}

	// in microseconds
	public static double getStandartDeviation(String labelName, ArrayList<TupleLogStructure> list)
	{
		Double[] rawData = getData(labelName, list);
		//Statistics st = new Statistics(rawData);
		return Statistics.getStdDev(rawData);
	}
	
	public static double getMedian(String labelName, ArrayList<TupleLogStructure> list)
	{
		Double[] rawData = getData(labelName, list);
		//Statistics st = new Statistics(rawData);
		return Statistics.getMedian(rawData);
	}
	
	public static double getMax(String labelName, ArrayList<TupleLogStructure> list)
	{
		Double[] rawData = getData(labelName, list);
		//Statistics st = new Statistics(rawData);
		return Statistics.getMax(rawData);
	}
	
	public static double getMin(String labelName, ArrayList<TupleLogStructure> list)
	{
		Double[] rawData = getData(labelName, list);
		//Statistics st = new Statistics(rawData);
		return Statistics.getMin(rawData);
	}
	
	
	
	private static Double[] getData(String labelName, ArrayList<TupleLogStructure> list) {
		ArrayList<Double> dataArray = new ArrayList<>();
		for(int i = 0; i < list.size(); i++)
		{
			if(list.get(i).labelName.equals(labelName))
			{
				dataArray.add((double) (list.get(i).time));
			}
		}
		Double[] rawData = convertListToArray(dataArray);
		return rawData;
	}
	
	
	public static Double[] convertListToArray(ArrayList<Double> list)
	{
		Double[] array = new Double[list.size()];
		for(int i =0; i<list.size(); i++)
		{
			array[i] = list.get(i);
		}		
		return array;
	}
	
	public static Double[] devideArrayElements(Double[] data, Double divider)
	{
		for(int i=0; i < data.length; i++)
		{
			data[i] = data[i]/divider;
		}		
		return data;
	}
	
	
	
	public static ArrayList<Double> collectDataFromLogs(String folderName, String testKey, String operationKey) throws FileNotFoundException, IOException
	{
		// data array to obtain
    	ArrayList<Double> allDataByOperationKey = new ArrayList<Double>();
    	
		File dir = new File(folderName);
    	File[] files =  dir.listFiles(new FilenameFilter() { 
    	         public boolean accept(File dir, String filename)
    	              { return filename.endsWith(fileExtension); }
    	});
    	
    	for(int i=0; i < files.length; i++)
    	{    		
    		ArrayList<Double> data = getDataArraByTestKey(files[i].getPath(), testKey, operationKey);
    		data = correctData(data);
    		allDataByOperationKey.addAll(data);
    	}  	
		return allDataByOperationKey;
	}
		
	public static Hashtable<String, ArrayList<Double>> collectDataFromLogs(String folderName, String testKey, String[] operationKeyArray) throws FileNotFoundException, IOException
	{
		Hashtable<String, ArrayList<Double>> allData = new Hashtable<String, ArrayList<Double>>();
		for(int i =0; i <operationKeyArray.length; i++)
		{
			ArrayList<Double> dataByOperationKey = collectDataFromLogs(folderName, testKey, operationKeyArray[i]);
			allData.put(operationKeyArray[i], dataByOperationKey);
		}
		return allData;
	}
	
/*	public static void collectDataFromLogs(String folderName, String[] testKeyArray, String[] operationKeyArray) throws FileNotFoundException, IOException
	{
		Hashtable<String, ArrayList<Double>> allData = new Hashtable<String, ArrayList<Double>>();
		for(int op =0; op <operationKeyArray.length; op++)
		{
			String operationKey = operationKeyArray[op];
			ArrayList<Double> dataByOperationKey = new ArrayList<Double>();
			for(int t =0; t <testKeyArray.length; t++)
			{
				ArrayList<Double> dataByOperationKeyAndTest = collectDataFromLogs(folderName, testKeyArray[t], operationKeyArray[op]);
				dataByOperationKey.addAll(dataByOperationKeyAndTest);
			}
			allData.put(operationKey, dataByOperationKey);
		}
		return;
	}
	
	public static void printStatistics(String folderName, String testKey, String[] operationKeyArray) throws FileNotFoundException, IOException
	{
		Hashtable<String, ArrayList<Double>> allData = collectDataFromLogs(folderName, testKey, operationKeyArray);
		for (String operationKey : allData.keySet()) {
			ArrayList<Double> data = allData.get(operationKey);
			Double[] rawData = convertListToArray(data);
			
			System.out.println("#Operation " + operationKey);
			Double average =  Statistics.getMean(rawData);
			System.out.println("count = " + rawData.length + ", average = " + average);
			
			Double deviation =  Statistics.getStdDev(rawData);
			System.out.println("count = " + rawData.length + ", deviation = " + deviation);
			
//			Double median =  Statistics.getMedian(rawData);
//			System.out.println("count = " + rawData.length + ", median = " + median);		
			
			Double max =  Statistics.getMax(rawData);
			System.out.println("count = " + rawData.length + ", max = " + max);	
			
//			Double min =  Statistics.getMin(rawData);
//			System.out.println("count = " + rawData.length + ", min = " + min);	
			System.out.println();
			
		}
	}*/

	public static Hashtable<String, StatisticStructure> processCollectedData(String folderName, String testKey, String[] operationKeyArray) throws FileNotFoundException, IOException
	{
		Hashtable<String, StatisticStructure> result = new Hashtable<String, StatisticStructure>();
				
		Hashtable<String, ArrayList<Double>> allData = collectDataFromLogs(folderName, testKey, operationKeyArray);
		for (String operationKey : allData.keySet()) 
		{
			ArrayList<Double> data = allData.get(operationKey);
			Double[] rawData = convertListToArray(data);
			
			Double average =  Statistics.getMean(rawData);		
			Double deviation =  Statistics.getStdDev(rawData);
						
			StatisticStructure resultPerOperation = new StatisticStructure();
			resultPerOperation.averageValue = average;
			resultPerOperation.numberOfOperations = data.size();
			resultPerOperation.standardDeviation = deviation;
			result.put(operationKey, resultPerOperation);
		}
		return result;
	}
	
	public static void printStatistics(String folderName, String testKey, String[] operationKeyArray) throws FileNotFoundException, IOException
	{
		Hashtable<String, StatisticStructure> allData = processCollectedData(folderName, testKey, operationKeyArray);
		for (String operationKey : allData.keySet()) {
			StatisticStructure data = allData.get(operationKey);
			
			System.out.println("#Operation " + operationKey);
			System.out.println("count = " + data.numberOfOperations + ", average = " + data.averageValue);
			
			System.out.println("count = " + data.numberOfOperations + ", deviation = " + data.standardDeviation);	
			System.out.println();
			
		}
	}
	
	private static ArrayList<Double> getDataArraByTestKey(String filePath, String testKey, String operationKey) throws FileNotFoundException, IOException
	{
		boolean isTestKeyFound = false;
		ArrayList<Double> data = new ArrayList<Double>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if(line.startsWith("testkey"))
		    	{
		    		if(line.equals("testkey=" + testKey))
		    			isTestKeyFound = true;
		    		else
		    			if(!isTestKeyFound)
		    				continue;
		    			else
		    				break;
		    	}
		    	if(isTestKeyFound)
		    	{
		    		if(line.startsWith(operationKey))
		    		{
		    			String strTime = line.replace(operationKey + "=", "");
		    			data.add(Double.valueOf(strTime));
		    		}
		    	}
		    	
		    }
		}
		return data;
	}
	
	/*
	 * important, remove picks in data
	 */
	public static ArrayList<Double> correctData(ArrayList<Double> initialData)
	{
		
		for(int i =0; i< (int)(initialData.size()*0.2) ; i++)
			initialData.remove(0);
		double coefficientMax = 3d;
		
		// count average of ten values nearby. If value varies more than coefficientMax times - substitute on average value
		for(int i =0; i < initialData.size(); i++)
		{
			double average = getAverageOfElements(initialData, i, 30);
			double difference = average*coefficientMax - initialData.get(i).doubleValue();
			if(difference < 0d)
				initialData.set(i, average);
		}
		
		return initialData;
	}
	
	public static double getAverageOfElements(ArrayList<Double> data, int elementPosition, int distance)
	{
		double average = 0.0;
		int addedNumbers = 0;
		for(int i=elementPosition - distance; i < elementPosition + distance ; i++)
		{
			if(i < 0)
				continue;
			if(i >= data.size())
				continue;
			average += data.get(i);
			addedNumbers++;
		}
		average = average/addedNumbers;
		return average;
	}

	public static Hashtable<String, StatisticStructure> collectDataFromLogs(String folderName, String[] testKeyArray, String[] operationKeyArray) throws FileNotFoundException, IOException
	{
		Hashtable<String, StatisticStructure> averageData = new Hashtable<String, StatisticStructure>();
    	// initialization
		for(int op = 0; op < operationKeyArray.length; op++)
			averageData.put(operationKeyArray[op], new StatisticStructure());
    	
    	int counterFoundData = 0;
		for(int t = 0; t<testKeyArray.length; t++)
		{
			Hashtable<String, StatisticStructure> data = processCollectedData(folderName, testKeyArray[t], operationKeyArray);
			
			for(String operationKey: data.keySet())
			{
				StatisticStructure dataByOperationKey = data.get(operationKey);
	    		if(data != null)
	    		{
		    		averageData.get(operationKey).numberOfOperations += dataByOperationKey.numberOfOperations;
		    		averageData.get(operationKey).averageValue += dataByOperationKey.averageValue;
		    		averageData.get(operationKey).standardDeviation += dataByOperationKey.standardDeviation;
	    		}	
			}
    		counterFoundData++;
		}

		if(counterFoundData > 0)
		{
			for(String operationKey: averageData.keySet())
			{
				averageData.get(operationKey).numberOfOperations = averageData.get(operationKey).numberOfOperations/counterFoundData;
				averageData.get(operationKey).averageValue = averageData.get(operationKey).averageValue/counterFoundData;
				averageData.get(operationKey).standardDeviation = averageData.get(operationKey).standardDeviation/counterFoundData;
			}
		}	
	
		return averageData;
	}
	
	public static void printStatistics(String folderName, String[] testKeyArray, String[] operationKeyArray) throws FileNotFoundException, IOException
	{
		Hashtable<String, StatisticStructure> allData = collectDataFromLogs(folderName, testKeyArray, operationKeyArray);
		
		System.out.println("\n\nResults");
		for (String operationKey : allData.keySet()) {
			StatisticStructure data = allData.get(operationKey);

			System.out.println("#Operation " + operationKey);
			System.out.println("count = " + data.numberOfOperations + ", average = " + data.averageValue);
			
			System.out.println("count = " + data.numberOfOperations + ", deviation = " + data.standardDeviation);
			
			System.out.println();
			
		}
	} 
	
	
}
