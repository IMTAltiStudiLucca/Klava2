/*
 * Created on 28 Aug 2016
 */
package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class FindError {

    public static void main(String[] args) throws IOException {

        ArrayList<Integer> ports = new ArrayList<Integer>();
        for(int i=0; i <5; i++)
            ports.add(6002 + i);
        
        for(int i=0; i <ports.size(); i++)
            checkWriteReadRequest(ports.get(i));
        
        System.out.println("finished with request");
        
        for(int i=0; i <ports.size(); i++)
            checkWriteReadResponse(ports.get(i));

        System.out.println("finished with response");
        
        for(int i=0; i <ports.size(); i++)
            checkThreeOperations(ports.get(i));

        System.out.println("finished with checkThreeOperations");
    }

    private static void checkWriteReadRequest(int port) throws IOException
    {
        /*
        request_write_by:6003_IN_operation:1
        request_read_from:6003_IN_operation:1
         */
        
        FileInputStream stream = new FileInputStream("output.txt");
        
        InputStreamReader isReader = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isReader);
                
        HashMap<Integer, Boolean> doubleOperationList = new HashMap<Integer, Boolean>();
        
        String write = "request_write_by:" + port + "_READ";
        String read = "request_read_from:" + port + "_READ";
        
        String line;
        try {
            while((line = br.readLine()) != null)
            {
                if(line.startsWith(write))
                {
                    String strOperationID = line.replace(write+"_operation:", "");
                    Integer operationID = Integer.valueOf(strOperationID);
                    if(doubleOperationList.containsKey(operationID))
                    {
                        doubleOperationList.replace(Integer.valueOf(operationID), true);
                    } 
                    else
                        doubleOperationList.put(Integer.valueOf(operationID), false);
                }

                if(line.startsWith(read))
                {
                    String strOperationID = line.replace(read+"_operation:", "");
                    Integer operationID = Integer.valueOf(strOperationID);
                    if(doubleOperationList.containsKey(operationID))
                    {
                        doubleOperationList.replace(operationID, true);
                    } 
                    else
                        doubleOperationList.put(Integer.valueOf(operationID), false);
                }
                
                
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        stream.close();
        
        
        
        Iterator<Entry<Integer, Boolean>> it = doubleOperationList.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<Integer, Boolean> pair = (Map.Entry<Integer, Boolean>)it.next();
            if(pair.getValue().equals(false))
            {
                System.out.println("port " + port + ": operationID " + pair.getKey());
            }
            it.remove();
        }
    }
    
    
    private static void checkWriteReadResponse(int port) throws IOException
    {
        /*
         write_to:6002_operation:25
         read_by:6003_operation:31
         */
        
        FileInputStream stream = new FileInputStream("output.txt");
        
        InputStreamReader isReader = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isReader);
                
        HashMap<Integer, Boolean> doubleOperationList = new HashMap<Integer, Boolean>();
        
        String write = "write_to:" + port;
        String read = "read_by:" + port;
        
        String line;
        try {
            while((line = br.readLine()) != null)
            {
                if(line.startsWith(write))
                {
                    String strOperationID = line.replace(write+"_operation:", "");
                    Integer operationID = Integer.valueOf(strOperationID);
                    if(doubleOperationList.containsKey(operationID))
                    {
                        doubleOperationList.replace(operationID, true);
                    } 
                    else
                        doubleOperationList.put(Integer.valueOf(operationID), false);
                }

                if(line.startsWith(read))
                {
                    String strOperationID = line.replace(read+"_operation:", "");
                    Integer operationID = Integer.valueOf(strOperationID);
                    if(doubleOperationList.containsKey(operationID))
                    {
                        doubleOperationList.replace(operationID, true);
                    } 
                    else
                        doubleOperationList.put(Integer.valueOf(operationID), false);
                }
                               
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        stream.close();
        
        Iterator<Entry<Integer, Boolean>> it = doubleOperationList.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<Integer, Boolean> pair = (Map.Entry<Integer, Boolean>)it.next();
            if(pair.getValue().equals(false))
            {
                System.out.println("port " + port + ": operationID " + pair.getKey());
            }
            it.remove();
        }
        
    }
    
    private static void checkThreeOperations(int port) throws FileNotFoundException, IOException {
        FileInputStream stream = new FileInputStream("output.txt");
        
        InputStreamReader isReader = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isReader);
        

        boolean overalCheck = false;
        boolean checkStage1 = false;
        boolean checkStage2 = false;
        boolean checkStage3 = false;
        
        String stage1 = port + ":RequestProcessor:write_response";
        String stage2 = port + ":RequestProcessor:receive_response";
        String stage3 = port + ":TupleSpaceInteraction:got_from_resptable";
        
        String line;
        try {
            while((line = br.readLine()) != null)
            {
                if(line.startsWith(stage1))
                {
                    checkStage1 = true;
                    overalCheck = false;
                }

                if(checkStage1 && line.startsWith(stage2))
                {
                    checkStage2 = true;
                    overalCheck = false;
                }
                
                if(checkStage1 &&  checkStage2 && line.startsWith(stage3))
                {
                    checkStage1 = false;
                    checkStage2 = false;
                    
                    
                    overalCheck = true;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("port " + port + ": res " + checkStage1 + ", " + checkStage2 + ", overal " + overalCheck);
        
        stream.close();
    }

}
