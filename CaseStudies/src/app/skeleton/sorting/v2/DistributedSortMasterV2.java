package app.skeleton.sorting.v2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import app.operations.TupleOperations;
import app.skeleton.matrix.DistributedMatrixMaster;
import app.skeleton.sorting.v1.MegreSort;
import apps.sorting.QSort;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;

public class DistributedSortMasterV2<T extends ITupleSpace> {

	private Object masterGSName;
	Class tupleSpaceClass;
	TupleLogger logger = null;

	// specific data
	private int[] values;
	private int threshold;
	int numberOfWorkers;
	ArrayList<Object> workerTSName;

	public final static String completeStatus = "complete";

	public static Object[] sortingTupleTemplate = new Object[] { String.class, QSort.class, String.class };

	public DistributedSortMasterV2(Object masterGSName, int numberOfWorkers, int[] values, Class tupleSpaceClass) {
		this.masterGSName = masterGSName;
		this.numberOfWorkers = numberOfWorkers;
		this.values = values;
		// this.workerTSName = workerTSName;
		this.tupleSpaceClass = tupleSpaceClass;
	}

	public void start() throws FileNotFoundException, IOException, InterruptedException {
		System.out.println("Connecting to data grid " + masterGSName);
		// start local tuple space
		T masterSpace = (T) getInstanceOfT(tupleSpaceClass);
		masterSpace.startTupleSpace(masterGSName, numberOfWorkers, true);

		threshold = (int) (values.length * 0.005d);
		QSort qs = new QSort(values, threshold);
		System.out.println("Done.");

		System.out.println("Master started");
		int workerCounter = 0;
		while (true) {
			Thread.sleep(50);
			TupleOperations.takeTuple(masterSpace,
							masterSpace, masterSpace.formTuple("SortingTuple",
									new Object[] { "sorting", null, "worker_ready" }, sortingTupleTemplate), true, false);
			workerCounter++;
			if (workerCounter == numberOfWorkers)
				break;
		}
		System.out.println("Master: all worker are connected");
		
		TupleLogger.begin("Master::TotalRuntime");

		// spread the test key
		TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("SortingTuple",
				new Object[] { "master_key", null, DProfiler.testKey }, DistributedSortMasterV2.sortingTupleTemplate),
				true, false);

		// choose on of the workers and send to it all data
		System.out.print("Adding data to tuplespace...");

		Vector<int[]> unsortedParts = new Vector<>();
		int partNumber = values.length/threshold;
		for(int i=0; i<partNumber; i++) {
			int[] part = new int[threshold];
			for(int k=0; k<threshold; k++)
			{
				part[k] = qs.getData()[i+threshold + k];
			}
			unsortedParts.add(part);
		}
		
		for(int i=0; i<unsortedParts.size(); i++) {
			int[] part = unsortedParts.get(i);
			QSort qsPart = new QSort(part, threshold);
			TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("SortingTuple",
				new Object[] { "sort_array", qsPart, "unsorted" }, DistributedSortMasterV2.sortingTupleTemplate), true, true);
		}
		// collect sorted sections of array
		int n = 0;
		Vector<int[]> sortedPartitions = new Vector<int[]>();
		System.out.println("Waiting for sorted parts...");
		while (n < values.length) {

			Object foundTupleTupleTemplate = masterSpace.formTuple("SortingTuple",
					new Object[] { "sorting", null, "sorted" }, DistributedSortMasterV2.sortingTupleTemplate);
			Object foundTupleTupleObject = TupleOperations.takeTuple(masterSpace, masterSpace, foundTupleTupleTemplate,
					true, false);
			Object[] foundTuple = masterSpace.tupleToObjectArray("SortingTuple", foundTupleTupleObject);

			QSort q = (QSort) foundTuple[1];
			sortedPartitions.addElement(q.getData());
			n += q.getData().length;
			System.out.println("collected " + n + " elements.");
		}
		System.out.println("Done. " + sortedPartitions.size() + " partitions collected.");

		// reconstruct array
		values = MegreSort.reconstructArray(sortedPartitions, values.length);

		// stop all workers
		for (int i = 0; i < numberOfWorkers; i++) {
			TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("SortingTuple",
					new Object[] { "sort_array", null, "finished"}, DistributedSortMasterV2.sortingTupleTemplate), true,
					false);
		}

		TupleLogger.end("Master::TotalRuntime");
		DProfiler.writeTestKeyToFile(DProfiler.testKey);

		String executionFolder = System.getProperty("user.dir");
		System.out.println(executionFolder);
		TupleLogger.writeAllToFile(DProfiler.testKey);
		TupleLogger.printStatistics(executionFolder, DProfiler.testKey,
				new String[] { "take::remote", "take::local", "write::remote", "write::local", "takeE::local",
						"takeE::remote", "read::l-r", "nodeVisited", "Master::TotalRuntime" });
		
        int finishedWorkerCount = 0;
        while(true) {
        	Object tuple = TupleOperations.takeTuple(masterSpace, masterSpace, masterSpace.formTuple("OperationTuple", new Object[]{"matrix", "worker_finish_work", ""}, DistributedMatrixMaster.operationTemplate), true, false);
        	finishedWorkerCount++;
        	if(finishedWorkerCount == numberOfWorkers)
        		break;
        	Thread.sleep(100);
		}
        System.err.println("MasterV2 finished its work");
         
        masterSpace.stopTupleSpace();
        System.exit(0);
	}

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
