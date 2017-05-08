package app.dist.sorting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import app.operations.TupleOperations;
import apps.sorting.QSort;
import common.TupleLogger;
import interfaces.ITupleSpace;
import profiler.DProfiler;

public class DistributedSortMaster<T extends ITupleSpace> {

	private Object masterGSName;
	Class tupleSpaceClass;
	TupleLogger logger = null;

	// specific data
	private int[] values;
	private int threshold;
	int numberOfWorkers;
	ArrayList<Object> workerTSName;



	public static Object[] sortingTupleTemplate = new Object[] { String.class, QSort.class, String.class };

	public DistributedSortMaster(Object masterGSName, int numberOfWorkers, int[] values, Class tupleSpaceClass) {
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

		threshold = (int) (values.length * 0.01d);
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
				new Object[] { "master_key", null, DProfiler.testKey }, DistributedSortMaster.sortingTupleTemplate),
				true, false);

		// choose on of the workers and send to it all data
		System.out.print("Adding data to tuplespace...");
		/*
		 * ArrayList<ITupleSpace> allWorkerTS = new ArrayList<>(); for(int i =
		 * 0; i < workerTSName.size(); i++) { T workerTS =
		 * getInstanceOfT(tupleSpaceClass);
		 * workerTS.startTupleSpace(workerTSName.get(i), numberOfWorkers,
		 * false); allWorkerTS.add(workerTS); }
		 */
		TupleOperations.writeTuple(
				masterSpace, masterSpace, masterSpace.formTuple("SortingTuple",
						new Object[] { "sort_array", qs, "unsorted" }, DistributedSortMaster.sortingTupleTemplate),
				true, false);

		qs = null;

		// collect sorted sections of array
		int n = 0;
		Vector<int[]> sortedPartitions = new Vector<int[]>();
		System.out.print("Waiting for sorted parts...");
		while (n < values.length) {

			// SortingTuple foundTuple = gigaSpace.take(new SortingTuple(null,
			// "sorted"), takeTimeout);
			Object foundTupleTupleTemplate = masterSpace.formTuple("SortingTuple",
					new Object[] { "sorting", null, "sorted" }, DistributedSortMaster.sortingTupleTemplate);
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
		for (int i = 0; i < numberOfWorkers; i++)
			TupleOperations.writeTuple(masterSpace, masterSpace, masterSpace.formTuple("SortingTuple",
					new Object[] { "sort_array", null, "complete" }, DistributedSortMaster.sortingTupleTemplate), true,
					false);

		TupleLogger.end("Master::TotalRuntime");
		DProfiler.writeTestKeyToFile(DProfiler.testKey);

		String executionFolder = System.getProperty("user.dir");
		System.out.println(executionFolder);
		TupleLogger.writeAllToFile(DProfiler.testKey);
		TupleLogger.printStatistics(executionFolder, DProfiler.testKey,
				new String[] { "take::remote", "take::local", "write::remote", "write::local", "takeE::local",
						"takeE::remote", "read::l-r", "nodeVisited", "Master::TotalRuntime" });

		System.out.println("Master process finished...Shutting down...");

		Thread.sleep(600000);
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
