package app.dist.ocean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import app.operations.TupleOperations;
import case_studies.Panel;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;
import proxy.tupleware.TuplewareProxy;


public class DistributedOceanModelWorker<T extends ITupleSpace> {

	private Object localGSPath;
	Object masterTSName;
    ArrayList<Object> otherWorkerTSName;
    ArrayList<ITupleSpace> workerGSs;
    Integer workerID;
    int matrixSize;
    Integer numberOfWorkers;
	Class tupleSpaceClass;
	
    
    public DistributedOceanModelWorker(Object _localGSPath, Integer _workerID, Object _masterTSName, ArrayList<Object> _otherWorkerTSName, int matrixSize, int numberOfWorkers, Class tupleSpaceClass)
    {
    	this.localGSPath = _localGSPath;
    	this.masterTSName = _masterTSName;
    	this.otherWorkerTSName = _otherWorkerTSName;
    	this.workerID = _workerID;
    	this.matrixSize = matrixSize;
    	this.tupleSpaceClass = tupleSpaceClass;
    	this.numberOfWorkers = numberOfWorkers;
    	
    }
    
    public void start() throws IOException, InterruptedException {
    	
        System.out.println("Connecting: worker " + workerID);
        
        //GigaSpaceProxy localGS = new GigaSpaceProxy();
        T localGS = (T) getInstanceOfT(tupleSpaceClass);
        localGS.startTupleSpace(localGSPath, numberOfWorkers, true);
        
        Thread.sleep(1000);
    	// trying to connect to server tuple space 5 times
        T masterGS = (T) getInstanceOfT(tupleSpaceClass);
        masterGS.startTupleSpace(masterTSName, numberOfWorkers, false);
        

        // create connection to other workers
        workerGSs = new ArrayList<>();
        Collections.shuffle(otherWorkerTSName);
        for(int i = 0; i < otherWorkerTSName.size(); i++)
        {
        	//GigaSpaceProxy gs = new GigaSpaceProxy(); // GigaspacesDistSearchTest.createGigaspace(otherWorkerTSName.get(i));
        	T workerTS = (T) getInstanceOfT(tupleSpaceClass);
        	workerTS.startTupleSpace(otherWorkerTSName.get(i), numberOfWorkers, false);
        	workerGSs.add(workerTS);
        }

        // signal to master that worker is ready
    	TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("OperationTuple", new Object[]{"oceanModel", "worker_ready", ""}, DistributedOceanModelMaster.operationTemplate), false, true);
    	
    	// get test key
    	Object tupleWithTestKeyObject = TupleOperations.readTuple(masterGS, localGS, localGS.formTuple("OperationTuple", new Object[]{"oceanModel", "master_key", null}, DistributedOceanModelMaster.operationTemplate), false, true);
    	Object[] tupleWithTestKey = localGS.tupleToObjectArray("OperationTuple", tupleWithTestKeyObject);
    	DProfiler.testKey = (String)tupleWithTestKey[2];
    	

    	System.out.println("Ready: worker " + workerID);

        
        // get initial data        
    	Object panelTupleObject = TupleOperations.takeTuple(masterGS, localGS, localGS.formTuple("PanelTuple", new Object[]{"oceanModel", "panel", null}, DistributedOceanModelMaster.panelTemplate), false, true);
    	Object[] panelTuple = localGS.tupleToObjectArray("PanelTuple", panelTupleObject);
    	
     //   PanelTuple pn = masterGS.take(new PanelTuple("panel", null), GigaspacesOceanDistMaster.takeTimeout);
        Panel task = (Panel) panelTuple[2];
        
        System.out.println("Worker " + workerID + ": panel was recieved");

       // Profiler.end("Sequential");

        
        while(task.kuv < task.itmax) {
            
            task.process();
            System.out.println("Worker " + workerID + ": processing finished " + task.kuv);

            if(numberOfWorkers == 1) {
                continue;
            }

            Vector<Vector> bvals = task.getBoundaryValuesGen();
            for(Vector t : bvals)
            {
            //	UpdateDataTuple updateTuple = new UpdateDataTuple((String) t.get(0), (Integer) t.get(1), (Integer) t.get(2), (Vector) t.get(3));
            	
            //    String serData = DataGeneration.serializeObject(t.get(3));
            //    byte[] bytes = serData.getBytes();
            //    System.out.println("data for update, bytes " + bytes.length);

            //	localGS.write(updateTuple);    
            	TupleOperations.writeTuple(localGS, localGS, localGS.formTuple("UpdateOceanModelTuple", 
            			new Object[]{"oceanModel", (String) t.get(0), (Integer) t.get(1), (Integer) t.get(2), (Vector) t.get(3)}, DistributedOceanModelMaster.updateOceanModelTemplate), true, true);
            }

            Vector<Vector> templates = task.getBoundaryTemplatesGen();
            Vector<Vector> intermediate = new Vector<Vector>(templates.size() - 1);

            for(Vector template : templates) {
             //   UpdateDataTuple getDataTemplate = new UpdateDataTuple((String) template.get(0), (Integer) template.get(1), (Integer) template.get(2), (Vector) template.get(3));
                
                Object borderUpdatedDataTupleObject = searchLoop(localGS, workerGSs, template);
                Object[] borderUpdatedDataTuple = localGS.tupleToObjectArray("UpdateOceanModelTuple", borderUpdatedDataTupleObject);

                intermediate.addAll((Vector<Vector>) borderUpdatedDataTuple[4]);
            }

            System.out.println("Worker " + workerID + ": get some data " + task.kuv);
            

            task.updateBoundariesGen(intermediate);

            intermediate = null;
        }

     //   task.cleanUp();

        //return final result
       // Profiler.begin("Sequential");
       // masterGS.write(new PanelTuple("panel_", new Panel()));
        //Profiler.end("Sequential");
        TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("PanelTuple", new Object[]{"oceanModel", "ready_panel", task}, DistributedOceanModelMaster.panelTemplate), false, true);

        String threadName = Thread.currentThread().getName();
        TupleLogger.writeAllToFile(DProfiler.testKey);
        System.out.println("Worker " + workerID + "finished");
        
        Thread.sleep(10000);
    }
     
    
    static Object searchLoop(ITupleSpace localGS, ArrayList<ITupleSpace> workerGSs, Vector templateData) throws InterruptedException
    {
    	boolean firstTime = true;
		TupleLogger.begin("read::l-r");
    	Object result = null;
    	while(true)
    	{
    		result = search(localGS, workerGSs, templateData, firstTime);
    		firstTime = false;
    		if(result != null)
    		{
    			TupleLogger.end("read::l-r");
    			return result;
    		}
    		try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    /*
     * search in the distribution of tuple spaces
     */
    static Object search(ITupleSpace localGS, ArrayList<ITupleSpace> workerGSs, Vector templateData, boolean firstTime) throws InterruptedException
    {
    	//Object template = localGS.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixB", true, rowID, null, null, null}, DistributedOceanModelMaster.panelTemplate);
    	Object template = localGS.formTuple("UpdateOceanModelTuple",  
    			new Object[]{"oceanModel", (String) templateData.get(0), (Integer) templateData.get(1), (Integer) templateData.get(2), (Vector) templateData.get(3)}, DistributedOceanModelMaster.updateOceanModelTemplate);
        
    	if (firstTime)
    		TupleLogger.incCounter("nodeVisited");

		// read from local space
		Object resultTuple = null;
		if(localGS instanceof TuplewareProxy)
			resultTuple = TupleOperations.readIfExistTuple(localGS, localGS, template, true, false);
		else
			resultTuple = TupleOperations.readIfExistTuple(localGS, localGS, template, true, true);
		
		if (resultTuple != null)
			return resultTuple;
		else
		{
    		for(int i = 0; i< workerGSs.size(); i++)
    		{
    	    	if (firstTime)
    	    		TupleLogger.incCounter("nodeVisited");
	    		// read from remote space
    	        resultTuple = TupleOperations.readIfExistTuple(workerGSs.get(i), localGS, template, false, true);
    			if(resultTuple != null)
    				return resultTuple;
    			
    			//Thread.sleep(5);
    		}
		}
		
		return null;
    }

       
    public T getInstanceOfT(Class<T> aClass)
    {
       try {
			return aClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       return null;
    }  
}
