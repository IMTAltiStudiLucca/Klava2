package app.dist.ocean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import app.operations.TupleOperations;
import case_studies.Panel;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;

public class DistributedOceanModelMaster<T extends ITupleSpace> {

	private Object masterGSName;
	int numberOfWorkers;
	int gridSize;
	Class tupleSpaceClass;
	
	TupleLogger logger = null;
	        
    public static Object[] panelTemplate = new Object[]{String.class, String.class, Panel.class};
    public static Object[] operationTemplate =new Object[]{String.class, String.class, String.class};
    public static Object[] updateOceanModelTemplate =new Object[]{String.class, String.class, Integer.class, Integer.class, Vector.class};
    public DistributedOceanModelMaster(Object masterGSName, int gridSize, int numberOfWorkers, Class<?> tupleSpaceClass)
    {
    	this.masterGSName = masterGSName;
    	this.numberOfWorkers = numberOfWorkers;
    	this.gridSize = gridSize;
    	this.tupleSpaceClass = tupleSpaceClass;
    	
    	SLICE_WIDTH = gridSize / numberOfWorkers;
    }
    
    private final int SLICE_WIDTH;

    // Model-related constants
    private final int im        = 10;
//    private final int jm        = 10;
    private final int dx        = 300;
    private final int dy        = 300;
    private final int dt        = 25;
    private final int tout      = 500000;
    private final int tend      = 1225; //2200; //1000000;
    private final double g      = 9.8;
    private final int rho       = 1025;
    private final double rho_a  = 1.2;
//    private final int uf        = 2;
    protected final double cd   = 0.000025;
    protected final double cd_a = 0.0013;
    protected final double wx   = 5.0;
    protected final double wy   = 5.0;

    protected final double facgx = g*dt/((double) dx);
    protected final double facgy = g*dt/((double) dy);
    protected final double facbf = cd * ((double) dt);
    protected final double facwx = wx*Math.sqrt(Math.pow(wx,2.0)+Math.pow(wy,2.0))*cd_a*rho_a*dt/((double) rho);
    protected final double facwy = wy*Math.sqrt(Math.pow(wx,2.0)+Math.pow(wy,2.0))*cd_a*rho_a*dt/((double) rho);
    protected final double facex = ((double) dt)/((double) dx);
    protected final double facey = ((double) dt)/((double) dy);
    protected final double hmax  = 0.8/(g*Math.pow(dt,2.0)*(1.0/Math.pow(dx,2.0)+1.0/Math.pow(dy,2.0)));
    protected final int itmax    = (int) Math.round(tend/dt)+2;

    private Panel[] panels;

    private String serverName;

    public void createPanels() {
        System.out.println("createPanels");
        
        panels = new Panel[numberOfWorkers];

        for(int i = 0; i < panels.length; i++) {
            System.out.println("Master: create panels results " + i);
            
            panels[i] = new Panel(SLICE_WIDTH, gridSize, i);
            panels[i].numPanels = panels.length;
            panels[i].itmax = itmax;
            panels[i].facgx = facgx;
            panels[i].facgy = facgy;
            panels[i].facbf = facbf;
            panels[i].facwx = facwx;
            panels[i].facwy = facwy;
            panels[i].facex = facex;
            panels[i].facey = facey;
            panels[i].hmax  = hmax;
            panels[i].itmax = itmax;

            panels[i].SHARE_LEFT = true;
            panels[i].SHARE_RIGHT = true;

            if(i == 0) {
                panels[i].SHARE_LEFT = false;
            }

            if(i == panels.length-1) {
                panels[i].SHARE_RIGHT = false;
            }
        }
    }

    public void start() throws InterruptedException, FileNotFoundException, IOException {
    	
        System.out.println("Connecting to data grid " + serverName);

        // start local tuple space   
        T masterSpace = (T) getInstanceOfT(tupleSpaceClass);
        masterSpace.startTupleSpace(masterGSName, numberOfWorkers, true);      

		
        System.out.println("Master: started");

        this.createPanels();
        
        // wait for the readiness of all workers
        int workerCounter = 0;
        while(true)
        {
        	Thread.sleep(10);
        	
        	TupleOperations.takeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"oceanModel", "worker_ready", ""}, operationTemplate), true, false);
        	workerCounter++;
        	if(workerCounter == numberOfWorkers)
        		break;     	
        }
        
        Thread.sleep(2000);
        
        // spread the test key
        TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"oceanModel", "master_key", DProfiler.testKey}, operationTemplate), true, false);
        System.out.println("Master: all worker loaded");
        
        TupleLogger.begin("Master::TotalRuntime");
        
        
        for(int i = 0; i < numberOfWorkers; i++) {
        	TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("PanelTuple", new Object[]{"oceanModel", "panel", panels[i]}, panelTemplate), true, false);
        	//gigaSpace.write(new PanelTuple("panel", panels[i]));
        }
        
        // Collect final results
        panels = new Panel[numberOfWorkers];
        System.gc();

        System.out.println("Master: collecting results");
        for(int i = 0; i < panels.length; i++) {
        	Object panelTupleObject = TupleOperations.takeTuple(masterSpace, masterSpace, masterSpace.formTuple("PanelTuple", new Object[]{"oceanModel", "ready_panel", null}, panelTemplate), true, false);
        	Object[] panelTuple = masterSpace.tupleToObjectArray("PanelTuple", panelTupleObject);
        	
        //	PanelTuple pt = gigaSpace.take(new PanelTuple("ready_panel", null), GigaspacesOceanDistMaster.takeTimeout);
            panels[i] = (Panel)panelTuple[2];
            
            System.out.println("Master: collecting results " + i);
        }
        System.out.println("Master: All results received.");    
       
        
        TupleLogger.end("Master::TotalRuntime");

        
        // add logging data
        DProfiler.writeTestKeyToFile(DProfiler.testKey);
        
        String executionFolder = System.getProperty("user.dir");
        System.out.println(executionFolder);
        TupleLogger.writeAllToFile(DProfiler.testKey);
//        TupleLogger.printStatistics(executionFolder,  DProfiler.testKey, new String[]{"take::remote", "take::local", "write::remote", 
//        		"write::local", "takeE::local", "takeE::remote", "read::l-r", "nodeVisited", "Master::TotalRuntime"});
        
        System.out.println("Master: finished.");   
        Thread.sleep(10000);
        System.exit(0);
    }

    
 /*  	public void start() throws NoSuchAlgorithmException, InterruptedException, IOException {
    	 		
   				
   		int matrixDimension = matrixSize;
   		// generate initial matrices
   		int[][] matrixA = generateMatrix(matrixDimension);
   		int[][] matrixB = generateMatrix(matrixDimension);

        System.out.println("Connecting to data grid " + masterGSName);

        // start local tuple space   
        T masterSpace = getInstanceOfT(tupleSpaceClass);
        masterSpace.startTupleSpace(masterGSName, numberOfWorkers, true);      
        
        // wait for the readiness of all workers
        int workerCounter = 0;
        while(true)
        {
        	Thread.sleep(10);
        	
        	TupleOperations.takeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_ready", ""}, operationTemplate), true, false);
        	workerCounter++;
        	if(workerCounter == numberOfWorkers)
        		break;     	
        }
        
        // spread the test key
        TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "master_key", DProfiler.testKey}, DistributedMatrixMaster.operationTemplate), true, false);
        
        System.out.println("Master: all worker loaded");

       // DProfiler.begin("Master::TotalRuntime");
 //       String threadName = "all" + Thread.currentThread().getName();
        TupleLogger.begin("Master::TotalRuntime");
        
        // send initial data to the workers
        for(int i = 0; i < matrixA.length; i++)
        {        	
        	Object matrixATuple = masterSpace.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixA", true, i, matrixA.length, matrixA[i], null}, DistributedMatrixMaster.matrixTemplate);
        	TupleOperations.writeTuple(masterSpace, masterSpace, matrixATuple, true, false);
        }
        
        for(int i = 0; i < matrixB.length; i++)
        {        	
        	Object matrixBTuple = masterSpace.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixB", true, i, matrixB.length, matrixB[i], null}, DistributedMatrixMaster.matrixTemplate);
        	TupleOperations.writeTuple(masterSpace, masterSpace, matrixBTuple, true, false);
        }
        System.out.println("Matrices are loaded");
        
        // start multiplication
        Object startMultiplication = masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "start_multiplication", ""}, operationTemplate);
        TupleOperations.writeTuple(masterSpace, masterSpace, startMultiplication, true, false);
        
        
        // wait for the result
        int[][] matrixC = new int[matrixDimension][matrixDimension];

        // get rows of the result matrix
        for(int i = 0; i < matrixC.length; i++) 
        {    	
            Object matrixRowResultTuple = masterSpace.formTuple("MatrixRowTuple", new Object[]{"matrix", "matrixC", true, null, null, null, null}, DistributedMatrixMaster.matrixTemplate);

            //masterGigaSpace.take(matrixRowResultTuple, GigaspacesDistPassSearchMaster.takeTimeout);
            Object resultTuple = TupleOperations.takeTuple(masterSpace, masterSpace, matrixRowResultTuple, true, false);

        	System.out.println("Master: row was obtained: " + i);
        }
        System.out.println("Master: tasks were created");
        
        
        // send to worker - "finish its work"
        for(int i =0; i< numberOfWorkers; i++)
        {
        	TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, operationTemplate), true, false);       	
        }
        //DProfiler.end("Master::TotalRuntime");

        TupleLogger.end("Master::TotalRuntime");
                
   //     DProfiler.printExt();
        DProfiler.writeTestKeyToFile(DProfiler.testKey);
        
        String executionFolder = System.getProperty("user.dir");
        System.out.println(executionFolder);
        TupleLogger.writeAllToFile(DProfiler.testKey);
        TupleLogger.printStatistics(executionFolder,  DProfiler.testKey, new String[]{"take::remote", "take::local", "write::remote", 
        		"write::local", "takeE::local", "takeE::remote", "read::l-r", "nodeVisited", "Master::TotalRuntime"});
               
        //while(masterGigaSpace.readIfExists(new OperationTuple("worker_finish_work", "")) != null)
        while(true)
		{
        	Object tuple = TupleOperations.readIfExistTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, operationTemplate), true, false);

        	if(tuple == null)
        		break;
        	Thread.sleep(250);
		}
        System.out.println("Master finished its work");
         
        masterSpace.stopTupleSpace(masterSpace);

    } 	*/
   	
  
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
