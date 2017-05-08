package app.dist.passwordsearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

import app.operations.TupleOperations;
import common.GeneralMethods;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;

public class DistributedSearchMaster<T extends ITupleSpace> {

	private Object masterTSAddress;
	int numberOfWorkers;
	int numberOfElements;
	Class tupleSpaceClass;
	
	TupleLogger logger = null;
	
    public final static String completeStatus = "complete"; 
	        
    public static Object[] searchTupleTemplate = new Object[]{String.class, String.class, String.class};
    
    public DistributedSearchMaster(Object masterTSAddress, int numberOfElements, int numberOfWorkers, Class tupleSpaceClass) {
    	this.masterTSAddress = masterTSAddress;
    	this.numberOfWorkers = numberOfWorkers;
    	this.numberOfElements = numberOfElements;
    	this.tupleSpaceClass = tupleSpaceClass;
    }
  
    /***
     * description of the master process
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws IOException
     */
  	public void passwordSearchMaster() throws NoSuchAlgorithmException, InterruptedException, FileNotFoundException, IOException {
    	
        System.out.println("Connecting to data grid " + masterTSAddress);

        // initialize a local tuple space   
        T masterTS = (T) getInstanceOfT(tupleSpaceClass);
        masterTS.startTupleSpace(masterTSAddress, numberOfWorkers, true); 
        
       
        // wait when all workers will be available
		waitForAllWorkers(masterTS);
		
		//System.out.println("something!!!");
        
        TupleLogger.begin("Master::TotalRuntime");

        // wait when all workers will load tables with data
        waitForDataLoad(masterTS);
        System.out.println("Master: all worker loaded data");

        // spread the current test key
        TupleOperations.writeTuple(masterTS, masterTS, masterTS.formTuple("SearchTuple_ttf", new Object[]{"search", "master_key", DProfiler.testKey}, DistributedSearchMaster.searchTupleTemplate), true, false);
       
        // create tasks and write it into local tuple space of the master
        int numberOfTasks = 100;
        taskCreation(masterTS, numberOfTasks);        

        // wait and get accomplished tasks
        ArrayList<Object[]> foundTuples = getAccomplishedTasks(masterTS, numberOfTasks);

        // send to worker - "finish its work"
        for(int i =0; i< numberOfWorkers; i++) {
        	TupleOperations.writeTuple(masterTS, masterTS, masterTS.formTuple("SearchTuple_tff", new Object[]{"search_task", "", completeStatus}, searchTupleTemplate), true, false);
        }
        TupleLogger.end("Master::TotalRuntime");

        // print results
        for(int i = 0; i < foundTuples.size(); i++) {
        	System.out.println(foundTuples.get(i)[1] + " - " + foundTuples.get(i)[2]);
        }        
        
		// write data of the test
        DProfiler.writeTestKeyToFile(DProfiler.testKey);
        
        
        Thread.sleep(2000);
        // add logging data
        
        String executionFolder = System.getProperty("user.dir");
        System.out.println(executionFolder);
        TupleLogger.writeAllToFile(DProfiler.testKey);
//        TupleLogger.printStatistics(executionFolder,  DProfiler.testKey, new String[]{"take::remote", "take::local", "write::remote", 
//        		"write::local", "takeE::local", "takeE::remote", "read::l-r", "nodeVisited", "Master::TotalRuntime", "match"});

        System.out.println("Master finished its work");
        
        Thread.sleep(10000);
        masterTS.stopTupleSpace();
        
        System.out.println("Master-terminate");
        System.exit(0);
    }

	/***
	 * wait when all workers are available
	 * @param masterTS the master tuple space 
	 * @throws InterruptedException
	 */
	private void waitForAllWorkers(T masterTS) throws InterruptedException {
		int workerCounter = 0;
        while (true) {
        	Thread.sleep(10);
        	TupleOperations.takeTuple(masterTS, masterTS, masterTS.formTuple("SearchTuple_ttt", new Object[]{"search", "worker", "worker_ready"}, searchTupleTemplate), true, false);
        	workerCounter++;
        	if(workerCounter == numberOfWorkers)
        		break;     
        }
        System.out.println("Master: all worker loaded data");
	}	
	
	/***
	 * wait when all workers load tables with data
	 * @param masterTS the master tuple space 
	 * @throws InterruptedException
	 */
	private void waitForDataLoad(T masterTS) throws InterruptedException {
		int workerCounter;
		workerCounter = 0;
        while (true) {
        	Thread.sleep(10);
        	TupleOperations.takeTuple(masterTS, masterTS, masterTS.formTuple("SearchTuple_ttt", new Object[]{"search", "worker", "data_loaded"}, searchTupleTemplate), true, false);
        	workerCounter++;
        	if(workerCounter == numberOfWorkers)
        		break;     
        }
	}
	
	/***
	 * create tasks and write it into local tuple space of the master
	 * @param masterTS      the master tuple space 
	 * @param numberOfTasks the number of tasks
	 * @throws NoSuchAlgorithmException
	 */
	private void taskCreation(T masterTS, int numberOfTasks) throws NoSuchAlgorithmException {
		
		if (numberOfElements < numberOfTasks)
		{
			System.err.println("numberOfTasks is less than numberOfElements: tasks are not unique");
		}
		Random r = new Random();
		MessageDigest mdEnc = MessageDigest.getInstance("MD5");
        ArrayList<String> tasks = new ArrayList<String>();
        while (tasks.size() <= numberOfTasks) {
        	int nextNumber = r.nextInt(numberOfElements);
        	String hashedValue = GeneralMethods.integerToHashString(mdEnc, nextNumber);   
        	if(!tasks.contains(hashedValue))
        		tasks.add(hashedValue);
        }
        
        // put all tasks into tupleSpace
        for(int i = 0; i < tasks.size(); i++) {    	
        	TupleOperations.writeTuple(masterTS, masterTS, 
        			masterTS.formTuple("SearchTuple_tff", new Object[]{"search_task", tasks.get(i), "not_processed"}, searchTupleTemplate), true, false);
        }
        System.out.println("Master: tasks were created");
	}
	
  	/***
  	 * get accomplished tasks
  	 * @param masterTS       the master tuple space
  	 * @param numberOfTasks  the number of tasks
  	 * @return
  	 */
	private ArrayList<Object[]> getAccomplishedTasks(T masterTS, int numberOfTasks) {
		
		Integer counter= 0;
		ArrayList<Object[]> foundTuples = new ArrayList<Object[]>();
        while (foundTuples.size() != numberOfTasks) {        	
        	Object foundTupleTemplate = masterTS.formTuple("SearchTuple_tff", new Object[]{"foundValue", null, null}, searchTupleTemplate);
        	Object foundTupleObject = TupleOperations.takeTuple(masterTS, masterTS, foundTupleTemplate, true, false);
        	Object[] foundTuple = masterTS.tupleToObjectArray("SearchTuple", foundTupleObject);
        	foundTuples.add(foundTuple);
        	
        	counter++;
        	System.out.println("foundValue count" + counter );
//        	try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        }
		return foundTuples;
	}

	/***
	 * create an object using class name
	 * @param aClass class name
	 * @return
	 */
    public T getInstanceOfT(Class<T> aClass) {
       try {
			return aClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       return null;
    }   
    
}
