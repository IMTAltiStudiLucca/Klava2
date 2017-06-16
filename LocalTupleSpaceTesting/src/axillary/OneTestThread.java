package axillary;
import java.util.ArrayList;

import common.TupleLogger;
import klava.Tuple;
import klava.TupleSpace;

public class OneTestThread extends Thread
{
	int threadNumber;
	TupleSpace space;
	ArrayList<Tuple> tuples; 
	ArrayList<Tuple> templatesForRead; 
	ArrayList<Tuple> templatesForTake;
	String testKey; 
	SyncObjectClass syncObj;
	
	
	public OneTestThread(int threadNumber, TupleSpace space, 
			ArrayList<Tuple> tuples, ArrayList<Tuple> templatesForRead, ArrayList<Tuple> templatesForTake,
			String testKey, SyncObjectClass syncObj)
	{
		this.threadNumber = threadNumber;
		this.space = space;
		this.tuples = tuples; 
		this.templatesForRead = templatesForRead; 
		this.templatesForTake = templatesForTake;
		this.testKey = testKey;
		this.syncObj = syncObj;
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

	        System.out.println("writing started");
	        TupleLogger.begin("total::local");
	        for(int i=0; i<tuples.size(); i++)
	        {            
	            TupleLogger.begin("out::local");
	            space.out(tuples.get(i));
	            TupleLogger.end("out::local");
	            if (i%10000 == 0)
	            	System.out.println(Thread.currentThread().getName() + ":write " + i);
	        }
	        syncObj.inc();
	        
	        System.out.println("writing finished");
	        
			while(true)
			{
				if(syncObj.count == threadNumber*2)
					break;
				Thread.sleep(5);
			}
	        System.out.println("reading started");
	        
	        
	        TupleLogger.begin("read::total");
	        for(int i=0; i<templatesForRead.size(); i++)
	        {
	            TupleLogger.begin("in_nb::local");
	            space.in_nb(templatesForTake.get(i));
	            TupleLogger.end("in_nb::local");
	            
	            //Thread.sleep(2);
	            
	            TupleLogger.begin("read_nb::local");
	            space.read_nb(templatesForRead.get(i));
	            TupleLogger.end("read_nb::local");
	            
            	System.out.println(Thread.currentThread().getName() + ":read " + i);
	            
	        }
	        TupleLogger.end("read::total");
	        
	        TupleLogger.end("total::local");
	        
	        
		    System.out.println("taking finished");    
		    

		    
		    String executionFolder = System.getProperty("user.dir");
		    System.out.println(executionFolder);
		    TupleLogger.writeAllToFile(testKey);
		    
	        syncObj.inc();
		    System.out.println("tuple's work finished");    

		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}