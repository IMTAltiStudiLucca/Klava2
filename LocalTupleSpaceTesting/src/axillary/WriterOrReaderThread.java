package axillary;
import java.util.ArrayList;

import common.TupleLogger;
import klava.Tuple;
import klava.TupleSpace;

public class WriterOrReaderThread  extends Thread {

	ArrayList<Tuple> dataList = null;
	SyncObjectClass syncObj;
	int threadNumber;
	String testKey;
	TupleSpace space;
	
	boolean writer = false;
	
	public WriterOrReaderThread(boolean writer, TupleSpace space, ArrayList<Tuple> dataList, int threadNumber, SyncObjectClass syncObj, String testKey) {
		this.writer = writer;
		this.space = space;
		this.dataList = dataList;
		this.threadNumber = threadNumber;
		this.syncObj = syncObj;
		this.testKey = testKey;
	}
	
	
   @Override
    public void run()  {
       try {
	   
	        syncObj.inc();
			while(true)
			{
				if(syncObj.count == threadNumber)
					break;
				Thread.sleep(5);
			}
		   
		   if(writer) {
		        System.out.println("writing started");
		        for(int i=0; i<dataList.size(); i++)
		        {            
		            TupleLogger.begin("write::local");
		            space.out(dataList.get(i));
		            TupleLogger.end("write::local");
		            if (i%1 == 0)
		            	System.out.println(Thread.currentThread().getName() + ":write " + i);
		        }
		        syncObj.inc();
		        
		        System.out.println("writing finished");
		        
		   } else {
		        TupleLogger.begin("read::total");
		        for(int i=0; i<dataList.size(); i++)
		        {
	////		            TupleLogger.begin("take::local");
	////		            space.in_nb(templatesForTake.get(i));
	////		            TupleLogger.end("take::local");
		            
		            //Thread.sleep(2);
		            
		            TupleLogger.begin("read::local");
					space.read(dataList.get(i));
		            TupleLogger.end("read::local");
		            
	            	System.out.println(Thread.currentThread().getName() + ":read " + i);
		            
		        }
		        TupleLogger.end("read::total");
		   }
		   
		   
		    String executionFolder = System.getProperty("user.dir");
		    System.out.println(executionFolder);
		    TupleLogger.writeAllToFile(testKey);
		    
	        syncObj.inc();
		    System.out.println("tuple's work finished");   
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
    }
	
}
