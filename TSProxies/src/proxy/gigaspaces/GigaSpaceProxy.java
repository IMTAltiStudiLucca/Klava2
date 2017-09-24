package proxy.gigaspaces;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.lrmi.LRMIManager;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SpaceURL;

import apps.sorting.QSort;
import interfaces.ITupleSpace;


public class GigaSpaceProxy extends ITupleSpace
{
	public static long takeTimeout = 3600000;
	
	public GigaSpace space = null;
	@Override
	public Object startTupleSpace(Object ...objects) {
		String address = (String)objects[0];
		// TODO Auto-generated method stub
        space = getGigaSpace(address);     
		return space;
	}

	@Override
	public void stopTupleSpace() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(Object ...objects) {
		Object tuple = objects[0];
		space.write(tuple);
	}

	@Override
	public Object read(Object ...objects) {
		Object template = objects[0];
		Object result = space.read(template, takeTimeout);
		return result;
	}

	@Override
	public Object readIfExist(Object ...objects) {
		Object template = objects[0];
		Object result = space.readIfExists(template);
		return result;
	}

	@Override
	public Object take(Object ...objects) {
		Object template = objects[0];
		Object result = space.take(template, takeTimeout);
		return result;
	}

	@Override
	public Object takeIfExist(Object ...objects) {
		Object template = objects[0];
		Object result = space.takeIfExists(template, takeTimeout);
		return result;
	}

	@Override
	public Object formTuple(String type, Object[] tupleFields, Object[] fieldsType) {

		if(type.startsWith("MatrixRowTuple"))
		{
			MatrixRowTuple tuple = new MatrixRowTuple((String)tupleFields[1], (Boolean)tupleFields[2], (Integer)tupleFields[3], (Integer)tupleFields[4], (int[])tupleFields[5], (Integer)tupleFields[6]);
			return tuple;
		} else if(type.startsWith("OperationTuple"))
		{
			OperationTuple tuple = new OperationTuple((String)tupleFields[1], (String)tupleFields[2]);
			return tuple;
		} else if(type.startsWith("SearchTuple"))
		{
			SearchTuple tuple = new SearchTuple((String)tupleFields[0], (String)tupleFields[1], (String)tupleFields[2]);
			return tuple;
		} else if(type.startsWith("SortingTuple"))
		{
			SortingTuple tuple = new SortingTuple((String)tupleFields[0], (QSort)tupleFields[1], (String)tupleFields[2]);
			return tuple;
		} else if(type.startsWith("PanelTuple"))
		{
			PanelTuple tuple = new PanelTuple((String)tupleFields[0], (String)tupleFields[1], (case_studies.Panel)tupleFields[2]);
			return tuple;
		} else if(type.startsWith("UpdateOceanModelTuple"))
		{
			UpdateDataTuple tuple = new UpdateDataTuple((String)tupleFields[1], (Integer)tupleFields[2], (Integer)tupleFields[3], (Vector)tupleFields[4]);
			return tuple;
		} else if(type.startsWith("LogTuple"))
		{
			LogTuple tuple = new LogTuple((String)tupleFields[0], (ArrayList)tupleFields[1]);
			return tuple;
		} 
			
			
		
		return null;
	}
	
	@Override
	public Object[] tupleToObjectArray(String type, Object generalTuple) {

		if(type.startsWith("MatrixRowTuple"))
		{
			MatrixRowTuple tuple = (MatrixRowTuple)generalTuple;
			Object[] data = new Object[7];
			data[0] = "matrix";
			data[1] = tuple.getMatrixName();
			data[2] = tuple.getIsComplete();
			data[3] = tuple.getRowID();
			data[4] = tuple.getDimension();
			data[5] = tuple.getData();
			data[6] = tuple.getUpdateCounter();		
			return data;
		} 
		else if(type.startsWith("OperationTuple"))
		{
			OperationTuple tuple = (OperationTuple)generalTuple;
			Object[] data = new Object[3];
			data[0] = "matrix";
			data[1] = tuple.getOperationName();
			data[2] = tuple.getStatus();	
			return data;
		}
		else if(type.startsWith("SearchTuple"))
		{
			SearchTuple tuple = (SearchTuple)generalTuple;
			Object[] data = new Object[3];
			data[0] = tuple.getTupleName();
			data[1] = tuple.getHashValue();
			data[2] = tuple.getValue();	
			return data;
		} else if(type.startsWith("SortingTuple"))
		{
			SortingTuple tuple = (SortingTuple)generalTuple;
			Object[] data = new Object[3];
			data[0] = tuple.getOperationType();
			data[1] = tuple.getQsort();
			data[2] = tuple.getStatus();
			return data;
		} else if(type.startsWith("PanelTuple"))
		{
			PanelTuple tuple = (PanelTuple)generalTuple;
			Object[] data = new Object[3];
			data[0] = tuple.getTaskName();
			data[1] = tuple.getName();
			data[2] = tuple.getPanel();
			return data;
		} else if(type.startsWith("UpdateOceanModelTuple"))
		{
			UpdateDataTuple tuple = (UpdateDataTuple)generalTuple;
			Object[] data = new Object[5];
			data[0] = tuple.getTaskType();
			data[1] = tuple.getFirstField();
			data[2] = tuple.getSecondField();
			data[3] = tuple.getThirdField();
			data[4] = tuple.getData();
			return data;
		} else if(type.startsWith("LogTuple"))
		{
			LogTuple tuple = (LogTuple)generalTuple;
			Object[] data = new Object[2];
			data[0] = tuple.getLogName();
			data[1] = tuple.getData();
			return data;
		}
			
		System.out.println("Attention! The type is not found");
		
		return null;
	}
	
	public static GigaSpace getGigaSpace(String serverName)
	{
		IJSpace configurer = new UrlSpaceConfigurer(serverName).space();
        GigaSpace gigaSpace = null;
        
    	boolean connected = false;
    	for (int i =0; i < 5; i++)
    	{
	    	try{
	    		gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();
	    		connected = true;
	            break;
	    	} catch (Exception exc)
	    	{
	    		System.out.println("Can't connect to server");
	    	}
    	}
    	if (!connected)
    		return null;
    	
        return gigaSpace;
	}
	
	public static GigaSpace createGigaspace(String serverName)
	{
        UrlSpaceConfigurer configurer = new UrlSpaceConfigurer(serverName);
        GigaSpace gigaspace = new GigaSpaceConfigurer(configurer).create();
              
        return gigaspace;
	}
	
	public static void startGigaspaceServer(String serverName)
	{
        UrlSpaceConfigurer configurer = new UrlSpaceConfigurer(serverName);      
        GigaSpace gigaspace = new GigaSpaceConfigurer(configurer).create();
	}
	
	public static void destroySpace(Object space)
	{
		String spaceName = (String) space;
		for(int i =0; i < 10; i++)
		{
			UrlSpaceConfigurer urlConfigurer = new UrlSpaceConfigurer(spaceName);
			
			try {
				urlConfigurer.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		// Local cache
//		LocalCacheSpaceConfigurer localCacheConfigurer = new LocalCacheSpaceConfigurer(urlConfigurer);
//		localCacheConfigurer.destroy();

		// Local view
//		LocalViewSpaceConfigurer localViewConfigurer = new LocalViewSpaceConfigurer(urlConfigurer);
//		localViewConfigurer.destroy();

		LRMIManager.shutdown();
	}

	
	public static String getGigaspaceServerAddress(GigaSpaceProxy spaceProxy)
	{
		IJSpace space = spaceProxy.space.getSpace();
		SpaceURL url = space.getFinderURL();

		String ipAddress = null;
		for(Map.Entry<Object, Object> obj : url.entrySet())
		{
			if(obj.getKey().equals("host"))
			{
				ipAddress = (String)obj.getValue();
			}
		}
		return ipAddress;
	}
}
