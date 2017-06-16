package case_study;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import axillary.OperationWrapper;
import common.TupleLogger;
import klaim.concurrent_structures.TupleSpaceHashtableCS;
import klaim.localspace.TupleSpaceHashtable;
import klaim.localspace.TupleSpaceHashtableSC;
import klaim.localspace.TupleSpaceList;
import klaim.tree.TupleSpaceTree;
import klava.KlavaException;
import klava.Tuple;
import klava.TupleSpace;
import klava.index_space.IndexedTupleSpace;
import profiler.DProfiler;

public class SieveEratosthenesThread extends Thread {

	public static void main(String[] args) throws IOException, KlavaException, InterruptedException {

		int numThreads = 1;
		int numElements = 500000;
		int implementationID = 2;
		
		if(args.length == 3)
		{
			implementationID = Integer.valueOf(args[0]);
			numElements = Integer.valueOf(args[1]);
			numThreads = Integer.valueOf(args[2]);
		}
		
		System.out.println("implementationID =" + implementationID);
		System.out.println("numElements =" + numElements);	
		System.out.println("threadNumber =" + numThreads);
		
		TupleSpace ts = chooseImplementation(implementationID);
		setHashTableSettings(ts);
		

		for(int i=1; i<=numElements; i++) {
			OperationWrapper.out(ts, new Tuple("str_int_str", new Object[]{"initial_array", i, "not_used"}), true);
		}
		
	    for(int i=0; i< 1000000;i++) {
	    	float index = (float)i + 0.5f;
			Tuple noisyTuple = new Tuple("str_int_str", new Object[]{"initial_array-", 12, "lala"});
			OperationWrapper.out(ts, noisyTuple, false);
			if(i%10000 == 0)
				System.out.println("adding noisyTuple " + i);
	    }
	    
	    String testKey = UUID.randomUUID().toString();
	    
	    TupleLogger.begin("queue::total");
	    
		ArrayList<Thread> threads = new ArrayList<>();
		//int nParts = numElements/numThreads;
		for(int i=0; i<numThreads; i++){
			
			int firstElement =  i*numElements/numThreads;
			int lastElement = (i+1)*numElements/numThreads - 1;
			SieveEratosthenesThread thread = new SieveEratosthenesThread(ts, i, testKey, firstElement, lastElement, numThreads, numElements);
			threads.add(thread);
			thread.start();
		}
		
		for(int i=0; i<threads.size(); i++)
			threads.get(i).join();
		
	    TupleLogger.end("queue::total");
		
	    DProfiler.writeTestKeyToFile(testKey);
		
	    String executionFolder = System.getProperty("user.dir");
	    TupleLogger.writeAllToFile(testKey);
	    TupleLogger.printStatistics(executionFolder, testKey,
	            new String[] { "out::local", "read::local", "read_nb::local", "in::local", "in_nb::local"});
	    
		System.out.println("Pogram finished");
	}

	private static void setHashTableSettings(TupleSpace ts) {
		Hashtable<String, List<Object>>  settings = new Hashtable<>();
		settings.put("str_int_str", Arrays.asList(new Boolean[]{true, true, true}, null));
		settings.put("str_int_int", Arrays.asList(new Boolean[]{true, true, false}, null));
		settings.put("str_int", Arrays.asList(new Boolean[]{true, true}, null));
		ts.setSettings(settings);
	}
	
	
	int threadID;
	TupleSpace space;
	String testKey;
	private int firstElement;
	private int lastElement;
	private int numThreads;
	private int numElements;
	
	public SieveEratosthenesThread(TupleSpace space, int threadID, String testKey, int firstElement, int lastElement, int numThreads, int numElements) {
		
		this.space = space;
		this.threadID = threadID;
		this.testKey = testKey;
		
		this.firstElement = firstElement;
		this.lastElement = lastElement;
		this.numThreads = numThreads;
		this.numElements = numElements;
	}
	
	@Override
	public void run() {
		
		try {
			process();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    TupleLogger.writeAllToFile(testKey);
	}

	
	private void process() throws InterruptedException{
		if(threadID == 0) {
			
			int minPrime = 1;
			for(int i = minPrime + 1; i<Math.sqrt(numElements); i++) {
				Tuple checkNextPrimeTemplate = new Tuple("str_int_str", new Object[]{"initial_array", i, "not_used"});
				if(space.read_nb(checkNextPrimeTemplate)) {
					minPrime = i;
					System.out.println("Prime " + minPrime);
					for(int p=0; p<numThreads; p++) {
						Tuple addNewPrimeTuple = new Tuple("str_int_int", new Object[]{"newPrime", p, minPrime});
						OperationWrapper.out(space, addNewPrimeTuple, true);
						
						System.out.println("NEW PRIME");
					}
					Thread.sleep(10);
					removeMultipleOfPrime(space, threadID, firstElement, lastElement);
					
					for(int p=0; p<numThreads; p++) {
						Tuple primeProcessedTemplate = new Tuple("str_int", new Object[]{"prime_processed", p});
						OperationWrapper.in(space, primeProcessedTemplate, true);
						
						//System.out.println(threadID + ": got prime_processed");
						Thread.sleep(10);
					}						
				}
			}
			
			// stop all processes
			for(int p=0; p<numThreads; p++) {
				Tuple addNewPrimeTuple = new Tuple("str_int_int", new Object[]{"newPrime", p, 0});
				OperationWrapper.out(space, addNewPrimeTuple, true);
			}
			
		//	printPrimeNumbers();
			
		} else {
			while(true) {
				boolean result = removeMultipleOfPrime(space, threadID, firstElement, lastElement);
				if(!result)
					break;
			}
		}	
	}
	
	private static boolean removeMultipleOfPrime(TupleSpace space, int threadID, int firstElement, int lastElement) throws InterruptedException {
		Tuple getNewPrimeTuple = new Tuple("str_int_int", new Object[]{"newPrime", threadID, Integer.class});
		OperationWrapper.in(space, getNewPrimeTuple, true);

		int newPrime = (int)getNewPrimeTuple.getItem(2);
		
		//System.out.println(threadID + " !new prime " + newPrime);
		
		// if not the end
		if(newPrime != 0) {

			// find a closest multiple of the prime
			int lowerBorder = firstElement % newPrime == 0 ? firstElement : (firstElement/newPrime)*newPrime + newPrime;

			// remove all multiple of the number
			for(int i=lowerBorder; i<=lastElement; i+=newPrime) {
				if(i == newPrime)
					continue;
				Tuple findUnusedTuple = new Tuple("str_int_str", new Object[]{"initial_array", i, "not_used"});
				if(OperationWrapper.in_nb(space, findUnusedTuple, true)) {
					
					Tuple markAsNotPrimeTuple = new Tuple("str_int_str", new Object[]{"initial_array", i, "not_prime"});
					space.out(markAsNotPrimeTuple);
					System.out.println(threadID + " - removed: " + i);
				}
				
			}
		} else
			return false;
		
		Tuple primeProcessedTuple = new Tuple("str_int", new Object[]{"prime_processed", threadID});
		space.out(primeProcessedTuple);
		System.out.println("Thread" + threadID + ": sent processed");
		return true;
	}
	
	private void printPrimeNumbers() throws InterruptedException {
		System.out.println("All prime numbers");
		for(int i = 2; i<numElements; i++) {
			Tuple checkNextPrimeTemplate = new Tuple("str_int_str", new Object[]{"initial_array", i, "not_used"});
			if(OperationWrapper.read_nb(space, checkNextPrimeTemplate, true)) {
				System.out.print(i + " ");
			}
		}
		System.out.print("\n");
	}
	

	
	public static TupleSpace chooseImplementation(int implementationID) {
		
		TupleSpace space = null;
		
		if(implementationID == 1)
        	space = new TupleSpaceList();
        else if(implementationID == 2) {
        	space = new TupleSpaceHashtable();	
        }
        else if(implementationID == 3)
        	space = new IndexedTupleSpace();
        else if(implementationID == 4) {}
        else if(implementationID == 5) {}
        else if(implementationID == 6) {}
        else if(implementationID == 7) {}
        else if(implementationID == 8) {
        } else if(implementationID == 9) {
        	space = new TupleSpaceTree();      	
        } else if(implementationID == 10) {
          	//space = new TupleSpaceListSC();
        } else if(implementationID == 11) {
        	space = new TupleSpaceHashtableCS();
        } else if(implementationID == 12) {
        	space = new TupleSpaceHashtableSC();	
        } 
		return space;
	}
	
	
	
	
	
}


