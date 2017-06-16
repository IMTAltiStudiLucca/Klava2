package case_study;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import axillary.OperationWrapper;
import common.TupleLogger;
import klava.KlavaException;
import klava.Tuple;
import klava.TupleSpace;
import profiler.DProfiler;

public class DistributedQueueThread extends Thread {

	boolean isWriter;
	int threadID;
	int operationNumber;
	TupleSpace space;
	String testKey;
	
	public DistributedQueueThread(boolean isWriter, int threadID, TupleSpace space, int operationNumber, String testKey) {
		this.isWriter = isWriter;
		this.threadID = threadID;
		this.operationNumber = operationNumber;
		this.space = space;
		this.testKey = testKey;
	}
	
	@Override
	public void run() {
		if (threadID ==0)
			init(space);
		if(isWriter)
			writer();
		else
			reader();
		
	    TupleLogger.writeAllToFile(testKey);
	}


	private void writer() {
		for(int i=0; i<operationNumber; i++) {
			try {
				String item = String.valueOf(threadID) + " " + String.valueOf(i);
		        TupleLogger.begin("queue::add");
				add(space, item);
				TupleLogger.end("queue::add");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Thread-writer #" + String.valueOf(threadID) + " finished");
	}
	
	private void reader() {
		for(int i=0; i<operationNumber; i++) {
			try {
				TupleLogger.begin("queue::peek");
				String item = peekFromHead(space);
				TupleLogger.begin("queue::peek");
				System.out.println(threadID + " Item: " + item);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Thread-reader #" + String.valueOf(threadID) + " finished");
	}
	
	
	
	public static void init(TupleSpace space) {
		
		Tuple initHeadTuple = new Tuple("str_str_int", new Object[]{"queue", "head", 0});
		space.out(initHeadTuple);
		Tuple initTailTuple = new Tuple("str_str_int", new Object[]{"queue", "tail", 0});
		space.out(initTailTuple);
	}
	
	public static void add(TupleSpace space, String item) throws InterruptedException {
		Tuple getCounterTemplate = new Tuple("str_str_int", new Object[]{"queue", "tail", Integer.class});
		OperationWrapper.in(space, getCounterTemplate, true);
		
		Integer counter = (Integer)getCounterTemplate.getItem(2);

		Tuple dataTuple = new Tuple("str_str_int_str", new Object[]{"queue", "item", counter++, item});
		OperationWrapper.out(space, dataTuple, true);
		
		Tuple updateTailTuple = new Tuple("str_str_int", new Object[]{"queue", "tail", counter});
		OperationWrapper.out(space, updateTailTuple, true);

		return;
	}
	
	public static String peekFromHead(TupleSpace space) throws InterruptedException {
		
		Tuple getCounterTemplate = new Tuple("str_str_int", new Object[]{"queue", "head", Integer.class});
		OperationWrapper.in(space, getCounterTemplate, true);
		
		Integer counter = (Integer)getCounterTemplate.getItem(2);
		Tuple getDatarTemplate = new Tuple("str_str_int_str", new Object[]{"queue", "item", counter, String.class});
		OperationWrapper.in(space, getDatarTemplate, true);
		String item = (String)getDatarTemplate.getItem(3);
		
		Tuple updateHeadTuple = new Tuple("str_str_int", new Object[]{"queue", "head", ++counter});
		OperationWrapper.out(space, updateHeadTuple, true);

		return item;
	}
	
	// main
	public static void main(String[] args) throws IOException, KlavaException, InterruptedException {

		int numThreads = 2;
		int operationNumber = 100000;
		int implementationID = 11;
		
		if(args.length == 3)
		{
			implementationID = Integer.valueOf(args[0]);
			operationNumber = Integer.valueOf(args[1]);
			numThreads = Integer.valueOf(args[2]);
		
		}
		
		System.out.println("implementationID =" + implementationID);
		System.out.println("operationNumber =" + operationNumber);	
		System.out.println("threadNumber =" + numThreads);
		
		TupleSpace space = SieveEratosthenesThread.chooseImplementation(implementationID);
		Hashtable<String, List<Object>>  settings = new Hashtable<>();
		settings.put("str_str_int", Arrays.asList(new Boolean[]{true, true, false}, null));
		settings.put("str_str_int_str", Arrays.asList(new Boolean[]{true, true, true, false}, null));
		space.setSettings(settings);
		
		
		operationNumber = operationNumber/numThreads;
		distributedQueueScenario(numThreads, space, operationNumber);
	}
	public static void distributedQueueScenario(int numThreads, TupleSpace space, int operationNumber) throws FileNotFoundException, IOException, InterruptedException {
		
	    String testKey = UUID.randomUUID().toString();
	    ArrayList<Thread> threads = new ArrayList<>();
	    
	    TupleLogger.begin("queue::total");
		for(int i=0; i<numThreads; i++) {
			
			if(i%2 == 0) {			
				DistributedQueueThread writerThread = new DistributedQueueThread(true, i, space, operationNumber, testKey);
				threads.add(writerThread);
				writerThread.start();
				
			} else {
				DistributedQueueThread readerThread = new DistributedQueueThread(false, i, space, operationNumber, testKey);
				threads.add(readerThread);
				readerThread.start();
			}
		} 

		// wait for the finish
		for(int i=0; i<threads.size(); i++)
			threads.get(i).join();
		
		TupleLogger.end("queue::total");
		
	    System.out.println("Test finished");    
	    DProfiler.writeTestKeyToFile(testKey);
	    String executionFolder = System.getProperty("user.dir");
	    TupleLogger.writeAllToFile(testKey);
	//    TupleLogger.printStatistics(executionFolder, testKey,
	//            new String[] { "queue::add", "queue::peek", "queue::total", "out::local", "read::local", "read_nb::local", "in::local", "in_nb::local"});
 
	}
}
