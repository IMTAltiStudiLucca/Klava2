package apps.dist.matrix.implementations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import app.operations.TupleOperations;
import interfaces.ITupleSpace;
import proxy.gigaspaces.GigaSpaceProxy;
import proxy.gigaspaces.LogTuple;

public class GetDataFromDockers {

    public static Object[] logTemplateStruct =new Object[]{String.class, ArrayList.class};
	public static int ipShift = 0;
	
	public static void main(String[] args) throws IOException {

		int numberOfWorkers = 5;

		if(args.length == 2)
		{
			numberOfWorkers = Integer.valueOf(args[0]);
			ipShift = Integer.valueOf(args[1]);
		} else
			System.err.println("wrong");
		
		for(int i=0; i<numberOfWorkers + 1; i++)
		{
			String ip = "172.17.0." + (i+1 + ipShift) + ":4174";

			getLogDataFromClients(numberOfWorkers, ip);			
		}

	}

	private static void getLogDataFromClients(int numberOfWorkers, String ip)
			throws IOException {
		String workerSpaceAddress = "jini://" + ip + "/*/client";
		
		GigaSpaceProxy localGS = new GigaSpaceProxy();
		localGS.startTupleSpace(workerSpaceAddress, numberOfWorkers, false);	

		
		LogTuple tuple = (LogTuple)TupleOperations.takeTuple(localGS, localGS,
				localGS.formTuple("LogTuple", new Object[]{"log", null},
						logTemplateStruct), false, false);	
		
		writeFile(ip + ".tlf", tuple.getData());
		System.out.println("finish");
	}
		
	public static void sendLogData(ITupleSpace space) throws IOException
	{
		//ArrayList<String> data = new ArrayList<>();
	
		File dir = new File(".");
		File [] files = dir.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".tlf");
		    }
		});

		for (File file : files) {
			System.out.println("log file name: " + file.getName());
			ArrayList logData = readFile(file.getName());
			TupleOperations.writeTuple(space, space, space.formTuple("LogTuple", new Object[]{"log", logData}, logTemplateStruct), true, false);
		}   	
	}
	
	public static ArrayList readFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        ArrayList array = new ArrayList();
	        String line = br.readLine();

	        while (line != null) {
	        	array.add(line);
	        	line = br.readLine();
	        }
	        return array;
	    } finally {
	        br.close();
	    }
	}
	
	public static void writeFile(String fileName, ArrayList data) throws IOException {
		BufferedWriter bw = null;

	      try {
	         // APPEND MODE SET HERE
	         bw = new BufferedWriter(new FileWriter(fileName, true));
	         for(int i=0; i<data.size(); i++)
	         {
	    		 bw.write((String)data.get(i));
	    		 bw.newLine();
	         }

	         bw.flush();
	      } catch (IOException ioe) {
		 ioe.printStackTrace();
	      } finally {                       // always close the file
		 if (bw != null) try {
		    bw.close();
		 } catch (IOException ioe2) {
		    // just ignore it
		 }
	      } // end try/catch/finally
	}

}
