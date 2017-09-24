package app.launcher.sorting;

import app.operations.TupleOperations;
import app.skeleton.sorting.v1.DistributedSortMaster;
import proxy.gigaspaces.GigaSpaceProxy;

public class GigaspaceLauncherTest {

	public static void main(String[] args) throws InterruptedException 
	{
		test();
		
		//launchServer("master");
	}
	
	static void launchServer(String spaceName) throws InterruptedException
	{
		// start tuple space server		
		String serverPath = "/./"; //"/./";   "jini://*/*/"     jini://10.180.53.36:8099/
		String masterServerPath = serverPath + spaceName;
		
		GigaSpaceProxy masterGS = new GigaSpaceProxy();
		masterGS.startTupleSpace(masterServerPath);
		
		while(true)
		{
			Thread.sleep(3600000);
		}
	}
	
	
	static void test()
	{
		String serverPath = "jini://10.180.53.36:4174/*/master"; 

		GigaSpaceProxy localGS = new GigaSpaceProxy();
		localGS.startTupleSpace("/./worker0");
		
		
		GigaSpaceProxy masterGS = new GigaSpaceProxy();
		masterGS.startTupleSpace(serverPath);
		
	//	SortingTuple tuple = new SortingTuple("sorting", null, "worker_ready");
	//	masterGS.write(tuple);
		
	     TupleOperations.writeTuple(masterGS, localGS, localGS.formTuple("SortingTuple", new Object[]{"sorting", null, "worker_ready"}, 
	    			DistributedSortMaster.sortingTupleTemplate), false, false);
		
	//	SortingTuple template = new SortingTuple("sorting", null, "worker_ready");
	//	SortingTuple result = (SortingTuple) masterGS.readIfExist(template);

		
	
	}
	
	

}
