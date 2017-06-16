package axillary;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import common.TupleLogger;
import profiler.DProfiler;

public class SimpleTest {

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		
		ArrayList<String> collection = new ArrayList<String>();
	
		for(int t = 0; t< 10; t++)
		{
	        for(int i=0; i<1000000; i++)
	        {
	           // TupleLogger.begin("write::local");
	            collection.add("lalalalalalalalala");
	           // TupleLogger.end("write::local");
	        }
	        Thread.sleep(100);
	        collection.clear();
		}
        

        
        Thread.sleep(1000);
        
        for(int i=0; i<10000; i++)
        {
            TupleLogger.begin("writeWU::local");
            collection.add("lalalalalalalalala");
            TupleLogger.end("writeWU::local");
        }
       
	    System.out.println("tuple's work finished");    
	    
	    String testKey = UUID.randomUUID().toString();
	    DProfiler.writeTestKeyToFile(testKey);
	
	    String executionFolder = System.getProperty("user.dir");
	    System.out.println(executionFolder);
	    TupleLogger.writeAllToFile(testKey);
	    TupleLogger.printStatistics(executionFolder, testKey,
	            new String[] { "write::local", "writeWU::local"});

	}

}
