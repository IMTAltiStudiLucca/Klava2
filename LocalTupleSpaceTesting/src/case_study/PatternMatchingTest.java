package case_study;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import axillary.DataPreparator;
import axillary.DatasetGenerator;
import axillary.OneTestThread;
import axillary.OperationWrapper;
import axillary.SyncObjectClass;
import axillary.WriterOrReaderThread;
import common.TupleLogger;
import klaim.concurrent_structures.TupleSpaceHashtableCS;
import klaim.localspace.TupleSpaceHashtable;
import klaim.localspace.TupleSpaceList;
import klaim.tree.TupleSpaceTree;
import klava.KlavaException;
import klava.Tuple;
import klava.TupleSpace;
import klava.index_space.IndexedTupleSpace;
import profiler.DProfiler;

public class PatternMatchingTest {

	
	
	public static void main(String[] args) throws IOException, KlavaException, InterruptedException {

		System.out.println("start");
		// parameters
		
		int numberTupleToRead = 1000;
		
		int numberOfTuples = 500000;
		int numberOfFields = 5;
		int numberOfTypes = 1;
		float cardinality = 0.1f;
		
		int threadNumber = 2;
		
		int implementationID = 2;
		
		/*
		 * 1 - TupleSpaceVector
		 * 2 - Hashtable
		 * 3 - Indexing
		 * 4 - SeparableVector
		 * 5 - SeparableHashtable
		 * 6 - SeparableIndexed
		 * 7 - SQLite
		 * 8 - Redis
		 * 9 - Tree
		 * 
		 */
		
		boolean isGenerateData = false;
		int numberQueriesPerTupleType = 1;
		
		if(args.length == 8)
		{
			numberOfTuples = Integer.valueOf(args[0]);
			numberOfFields = Integer.valueOf(args[1]);
			numberOfTypes = Integer.valueOf(args[2]);
			cardinality = Float.valueOf(args[3]);
			
			implementationID = Integer.valueOf(args[4]);
			
			isGenerateData = Boolean.valueOf(args[5]);
			numberQueriesPerTupleType = Integer.valueOf(args[6]);
			numberTupleToRead = Integer.valueOf(args[7]);

			System.out.println("numberOfTuples =" + numberOfTuples);
			System.out.println("numberOfFields =" + numberOfFields);
			System.out.println("numberOfTypes =" + numberOfTypes);
			System.out.println("cardinality =" + cardinality);
			System.out.println("implementationID =" + implementationID);
			System.out.println("numberTupleToRead =" + numberTupleToRead);		
			System.out.println("numberQueriesPerTupleType =" + numberQueriesPerTupleType);		
			
		} else if(args.length == 3)
		{
			implementationID = Integer.valueOf(args[0]);
			threadNumber = Integer.valueOf(args[1]);
			numberOfTuples = Integer.valueOf(args[2]);
			
			System.out.println("implementationID =" + implementationID);
			System.out.println("threadNumber =" + threadNumber);
			System.out.println("numberOfTuples =" + numberOfTuples);
		} else
		{
			System.out.println("You use default parameters");
			//return;
		}
		
		
		if(isGenerateData) {
			DatasetGenerator ds = new DatasetGenerator();
			ds.generateData("dataset.dat", numberOfTuples, numberOfFields, numberOfTypes, cardinality);
		} else{
		//	doTest("dataset.dat", numberTupleToRead, implementationID, numberQueriesPerTupleType);
			
			 parallelTest(threadNumber, implementationID);
		//	parallelWRTest(threadNumber, implementationID);
		}
		
		System.out.println("done");
	
	}
	
	
	
	/*
	 * Tests
	 * 1000000 writes
	 * 250 random reads
	 * 
	 * Fields number: 4 8 12
	 * Number of types: 1 5 10
	 * 
	 * Number of templates in case of 1 type: 1 5
	 * 
	 * all tuples are the same
	 * all fields are the same except one
	 * parallel test
	 */
			
	static void doTest(String fileName, int numberTupleToRead, int implementationID, int numberQueriesPerTupleType) throws IOException, KlavaException, InterruptedException
	{
		System.out.println("start test");
		
		Hashtable<String, List<Object>> typesTable = new Hashtable<>();
		
		// load tuples
		ArrayList<Tuple> tuples = DataPreparator.prepareData("dataset.dat", typesTable);
		
		// pick tuples and create templates
		ArrayList<Tuple> templatesForRead = DataPreparator.prepareQueries(tuples, numberTupleToRead*3, numberQueriesPerTupleType);
		ArrayList<Tuple> templatesForTake = new ArrayList<>();
		for(int i=numberTupleToRead*2-1; i>=numberTupleToRead; i--) 
		{
			templatesForTake.add((Tuple)templatesForRead.get(i));
			templatesForRead.remove(i);
		}
		
		ArrayList<Tuple> templatesForWarmup = new ArrayList<>();
		for(int i=numberTupleToRead*2-1; i>=numberTupleToRead; i--) 
		{
			templatesForWarmup.add((Tuple)templatesForRead.get(i));
			templatesForRead.remove(i);
		}
	
		
		TupleSpace space = chooseImplementation(implementationID, typesTable, templatesForRead);
        
        // write tuples
        
//        System.out.println("warmup phase");
//        for(int i=0; i<tuples.size(); i++)
//        {
//        	space.out(tuples.get(i));
//            if (i%100000 == 0)
//            	System.out.println("warmup write " + i);
//        }
//        
//        for(int i=0; i<templatesForWarmup.size(); i++)
//        {
//        	space.read_nb(templatesForWarmup.get(i));
//  //          System.out.println("warmup reading " + i);
//        }       
//        space.clear();
      

        System.out.println("warmup phase is finished");
        
        TupleLogger.begin("total::local");
        for(int i=0; i<tuples.size(); i++)
        {           
            OperationWrapper.out(space, tuples.get(i), true);     
            
            if (i%100000 == 0)
            	System.out.println("write " + i);
        }
        
        System.out.println("writing is finished");
        
        for(int i=0; i<templatesForRead.size(); i++)
        {
        	boolean result = false;
            OperationWrapper.in_nb(space, templatesForTake.get(i), true);
            
            //Thread.sleep(2);

            result = OperationWrapper.read_nb(space, templatesForRead.get(i), true);
            
            System.out.println(result + " reading and takes " + i);
        }
       

        TupleLogger.end("total::local");
        
	    String testKey = UUID.randomUUID().toString();
	    
	    System.out.println("taking finished");    
	    
	    System.out.println("tuple's work finished");    
	    
	    DProfiler.writeTestKeyToFile(testKey);
	
	    String executionFolder = System.getProperty("user.dir");
	    System.out.println(executionFolder);
	    TupleLogger.writeAllToFile(testKey);
	    TupleLogger.printStatistics(executionFolder, testKey,
	            new String[] { "write::local", "read::local", "take::local","total::local", "checkPermission", "whatToRelease", "read::total" });
 
	}



	public static TupleSpace chooseImplementation(int implementationID,
			Hashtable<String, List<Object>> typesTable, ArrayList<Tuple> templatesForRead) {
		
		TupleSpace space = null;
		
		if(implementationID == 1)
        	space = new TupleSpaceList();
        else if(implementationID == 2) {
        	space = new TupleSpaceHashtable();
    		
        	Hashtable<String, List<Object>> hashtableSettings = DataPreparator.extractSettingsForHashtable(templatesForRead);   		    
        	space.setSettings(hashtableSettings);    		
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
        	Hashtable<String, List<Object>> hashtableSettings = DataPreparator.extractSettingsForHashtable(templatesForRead);   		    
        	space.setSettings(hashtableSettings);  
        } else if(implementationID == 10) {
          	//space = new TupleSpaceListSC();
        } else if(implementationID == 11) {
        	space = new TupleSpaceHashtableCS();
        	Hashtable<String, List<Object>> hashtableSettings = DataPreparator.extractSettingsForHashtable(templatesForRead);   		    
        	space.setSettings(hashtableSettings);   
        }
		return space;
	}
	
	
	static void parallelTest(int threadNumber, int implementationID) throws IOException, InterruptedException
	{
		System.out.println("start test");
		
		
		Hashtable<String, List<Object>> typesTable = new Hashtable<>();
		
		int numberTupleToRead = 1000;
		//if(implementationID == 1)
		//	numberTupleToRead = 150;
		
		// load tuples
		ArrayList<Tuple> tuples = DataPreparator.prepareData("dataset.dat", typesTable);
		
		// pick tuples and create templates
		ArrayList<Tuple> allTemplatesForRead = DataPreparator.prepareQueries(tuples, numberTupleToRead*(2*threadNumber + 1), 31);
		
		// initialize tuple space
        TupleSpace space = chooseImplementation(implementationID, typesTable, allTemplatesForRead);
        
		// prepare templates
		ArrayList<ArrayList<Tuple>> templatesToReadList = new ArrayList<>(threadNumber);
		ArrayList<ArrayList<Tuple>> templatesToTakeList = new ArrayList<>(threadNumber);

		for(int i=0; i<threadNumber; i++)
		{
			int tupleCount = 0;
			ArrayList<Tuple> templatesToRead = new ArrayList<>();
			ArrayList<Tuple> templatesToTake = new ArrayList<>();
			while(tupleCount != numberTupleToRead*2)
			{
				int lastIndex = allTemplatesForRead.size()-1;
				templatesToRead.add((Tuple)allTemplatesForRead.get(lastIndex));
				allTemplatesForRead.remove(lastIndex);
				
				templatesToTake.add((Tuple)allTemplatesForRead.get(lastIndex-1));
				allTemplatesForRead.remove(lastIndex-1);
				
				
				tupleCount += 2;
			}
			templatesToReadList.add(templatesToRead);
			templatesToTakeList.add(templatesToTake);
		}
		
		ArrayList<ArrayList<Tuple>> tuplesToWriteList = new ArrayList<>(threadNumber);
		int numberOfTuplesToWritePerThread = tuples.size() / threadNumber;
		for(int i=0; i<threadNumber; i++)
		{
			ArrayList<Tuple> tuplesToWrite = new ArrayList<>();
			for(int j=0; j<numberOfTuplesToWritePerThread;j++)
			{
				int tupleIndex = i*numberOfTuplesToWritePerThread + j;
				tuplesToWrite.add(tuples.get(tupleIndex));
			}
			tuplesToWriteList.add(tuplesToWrite);
		}
		
		ArrayList<Tuple> templatesForWarmup = new ArrayList<>();
		for(int i=numberTupleToRead-1; i>=0; i--) 
		{
			templatesForWarmup.add((Tuple)allTemplatesForRead.get(i));
			allTemplatesForRead.remove(i);
		}
		       
		// warm up phase
//        System.out.println("warmup phase");
//        for(int i=0; i<tuples.size(); i++)
//        {
//        	space.out(tuples.get(i));
//            if (i%100000 == 0)
//            	System.out.println("warmup write " + i);
//        }
//        
//        for(int i=0; i<templatesForWarmup.size(); i++)
//        {
//        	space.read_nb(templatesForWarmup.get(i));
//        }       
//        space.clear();
//        System.out.println("warmup phase finished");
		
	    String testKey = UUID.randomUUID().toString();
	    
	    SyncObjectClass syncObj = new SyncObjectClass();
		for(int i=0; i<threadNumber; i++)
		{
			OneTestThread thread = new OneTestThread(threadNumber, space, tuplesToWriteList.get(i), templatesToReadList.get(i), templatesToTakeList.get(i), testKey, syncObj);
			thread.start();		
		}
		
		while(true)
		{
			if(syncObj.get() == threadNumber*3)
				break;
			Thread.sleep(5);
		}
		
	    DProfiler.writeTestKeyToFile(testKey);	
	    
//	    String executionFolder = System.getProperty("user.dir");
//	    TupleLogger.printStatistics(executionFolder, testKey,
//	            new String[] { "write::local", "read::local", "take::local","total::local", "checkPermission", "whatToRelease", "read::total", "checkIfFeasibleToExecute"});
	    
        System.out.println("test finished");
	}
	
	
	// chto eto  ?????????????
	static void parallelWRTest(int threadNumber, int implementationID) throws IOException, InterruptedException
	{
		System.out.println("start test");
		
		threadNumber = 2;
		
		Hashtable<String, List<Object>> typesTable = new Hashtable<>();
		
		int numberTupleToRead = 250;
		
		// load tuples
		ArrayList<Tuple> tuples = DataPreparator.prepareData("dataset.dat", typesTable);
		
		// pick tuples and create templates
		ArrayList<Tuple> allTemplatesForRead = DataPreparator.prepareQueries(tuples, numberTupleToRead*(2*threadNumber + 1), 1);
		
		// initialize tuple space
        TupleSpace space = chooseImplementation(implementationID, typesTable, allTemplatesForRead);
        
		// prepare templates
		ArrayList<ArrayList<Tuple>> templatesToReadList = new ArrayList<>(threadNumber);
		ArrayList<ArrayList<Tuple>> templatesToTakeList = new ArrayList<>(threadNumber);

		for(int i=0; i<threadNumber; i++)
		{
			int tupleCount = 0;
			ArrayList<Tuple> templatesToRead = new ArrayList<>();
			ArrayList<Tuple> templatesToTake = new ArrayList<>();
			while(tupleCount != numberTupleToRead*2)
			{
				int lastIndex = allTemplatesForRead.size()-1;
				templatesToRead.add((Tuple)allTemplatesForRead.get(lastIndex));
				allTemplatesForRead.remove(lastIndex);
				
				templatesToTake.add((Tuple)allTemplatesForRead.get(lastIndex-1));
				allTemplatesForRead.remove(lastIndex-1);
				
				
				tupleCount += 2;
			}
			templatesToReadList.add(templatesToRead);
			templatesToTakeList.add(templatesToTake);
		}
		
		ArrayList<ArrayList<Tuple>> tuplesToWriteList = new ArrayList<>(threadNumber);
		int numberOfTuplesToWritePerThread = tuples.size() / threadNumber;
		for(int i=0; i<threadNumber; i++)
		{
			ArrayList<Tuple> tuplesToWrite = new ArrayList<>();
			for(int j=0; j<numberOfTuplesToWritePerThread;j++)
			{
				int tupleIndex = i*numberOfTuplesToWritePerThread + j;
				tuplesToWrite.add(tuples.get(tupleIndex));
			}
			tuplesToWriteList.add(tuplesToWrite);
		}
		
		ArrayList<Tuple> templatesForWarmup = new ArrayList<>();
		for(int i=numberTupleToRead-1; i>=0; i--) 
		{
			templatesForWarmup.add((Tuple)allTemplatesForRead.get(i));
			allTemplatesForRead.remove(i);
		}

	    String testKey = UUID.randomUUID().toString();
	    
	    SyncObjectClass syncObj = new SyncObjectClass();
		WriterOrReaderThread readerthread = new WriterOrReaderThread(true, space, tuples, threadNumber, syncObj, testKey);
		readerthread.start();
		
		WriterOrReaderThread writerThread = new WriterOrReaderThread(false, space, templatesToReadList.get(0), threadNumber, syncObj, testKey);
		writerThread.start();	
		
		while(true)
		{
			if(syncObj.get() == threadNumber*2)
				break;
			Thread.sleep(5);
		}
		
	    DProfiler.writeTestKeyToFile(testKey);	
	    
	    String executionFolder = System.getProperty("user.dir");
	    TupleLogger.printStatistics(executionFolder, testKey,
	            new String[] { "write::local", "read::local", "take::local","total::local", "checkPermission", "whatToRelease", "read::total", "checkIfFeasibleToExecute"});
	    
        System.out.println("test finished");
	}

	

}
