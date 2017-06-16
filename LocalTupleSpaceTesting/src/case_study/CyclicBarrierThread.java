package case_study;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import axillary.OperationWrapper;
import common.TupleLogger;
import klava.KlavaException;
import klava.Tuple;
import klava.TupleSpace;
import profiler.DProfiler;

public class CyclicBarrierThread extends Thread {

	int threadID;
	int operationNumber;
	TupleSpace space;
	String testKey;
	int numThreads;
	
	public CyclicBarrierThread(int threadID, TupleSpace space, int operationNumber, String testKey, int numThreads) {
		this.threadID = threadID;
		this.operationNumber = operationNumber;
		this.space = space;
		this.testKey = testKey;
		this.numThreads = numThreads;
	}
	
	@Override
	public void run() {
		try {
			barrier();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    TupleLogger.writeAllToFile(testKey);
	}


	private void barrier() throws InterruptedException {
		Random rand = new Random();
		String barrierName = null;
		for(int i=0; i<operationNumber; i++) {

			barrierName = "barrier_" + String.valueOf(i);

			if(threadID == 0) {

				if(i>2) {
					Tuple removeBarrierTuple = new Tuple("str_int", new Object[]{"barrier_" + String.valueOf(i-2), Integer.class});
					OperationWrapper.in(space, removeBarrierTuple, false);
				}
				System.out.println("create barrier " + barrierName);
				init(space, barrierName, numThreads);
			}
		
			int sleepTime = rand.nextInt(5);
		//	Thread.sleep(sleepTime);
			await(space, barrierName, threadID);
		
		}
		System.out.println("Thread #" + String.valueOf(threadID) + " finished");
	}
	
	
	public static void init(TupleSpace space, String barrierName, Integer numThreads) {
		Tuple updateCounterTuple = new Tuple("str_int", new Object[]{barrierName, numThreads});
		space.out(updateCounterTuple);
	}
	
	public static void await(TupleSpace space, String barrierName, Integer threadID) throws InterruptedException {
		
		Tuple counterTemplate = new Tuple("str_int", new Object[]{barrierName, Integer.class});
		OperationWrapper.in(space, counterTemplate, true);
		
		Integer counter = (Integer)counterTemplate.getItem(1) - 1;
		Tuple updateCounterTuple = new Tuple("str_int", new Object[]{barrierName, counter});
		OperationWrapper.out(space, updateCounterTuple, true);
			
		Tuple waitForZeroTemplate = new Tuple("str_int", new Object[]{barrierName, new Integer(0)});
		OperationWrapper.read(space, waitForZeroTemplate, true);
		return;
	}
	

	// main
	public static void main(String[] args) throws IOException, KlavaException, InterruptedException {

		int numThreads = 10;
		int operationNumber = 100000;
		int implementationID = 12;
		
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
		settings.put("str_int", Arrays.asList(new Boolean[]{true, false}, new Boolean[]{true, true}));
		space.setSettings(settings);
		
		
		operationNumber = operationNumber/numThreads;
		cyclicBarrierScenario(numThreads, space, operationNumber);
	}
	
	
	public static void cyclicBarrierScenario(int numThreads, TupleSpace space, int operationNumber) throws FileNotFoundException, IOException, InterruptedException {
		
	    String testKey = UUID.randomUUID().toString();
	    ArrayList<Thread> threads = new ArrayList<>();
	    
//	    for(int i=0; i< 50000000;i++) {
//	    	float index = (float)i + 0.5f;
//			Tuple noisyTuple = new Tuple("str_int", new Object[]{"barrier_-" + index, -1});
//			OperationWrapper.out(space, noisyTuple, false);
//			if(i%10000 == 0)
//				System.out.println("adding noisyTuple " + i);
//	    }

	    TupleLogger.begin("queue::total");
		for(int i=0; i<numThreads; i++) {
			
			CyclicBarrierThread barrierThread = new CyclicBarrierThread(i, space, operationNumber, testKey, numThreads);
			threads.add(barrierThread);
			barrierThread.start();
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
	//            new String[] { "queue::add", "queue::peek", "queue::total", "out::local", "read::local", "read_nb::local", "in::local", "in_nb::local", "inc"});
 
	}
}
