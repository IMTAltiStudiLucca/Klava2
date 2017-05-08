package proxy.mozartspaces;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apps.sorting.QSort;
import case_studies.Panel;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import interfaces.ITupleSpace;
import proxy.gigaspaces.MatrixRowTuple;
import proxy.gigaspaces.OperationTuple;
import proxy.gigaspaces.PanelTuple;
import proxy.gigaspaces.SearchTuple;
import proxy.gigaspaces.SortingTuple;
import proxy.gigaspaces.UpdateDataTuple;

public class MozartProxy  extends ITupleSpace 
{
	MzsCore localCore = null;
	Capi localCapi = null;
	ContainerReference localContainer = null;
	
	public MozartProxy ()
	{
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		Logger LOG = (Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger) LOG).setLevel(Level.ERROR);
	}
	@Override
	public Object startTupleSpace(Object ...objects)
	{
		int port = (int)objects[0];
		Boolean mainInitialization = (Boolean)objects[2];
		
		if(mainInitialization.booleanValue())
		{
			localCore = DefaultMzsCore.newInstance(port);
			localCapi = new Capi(localCore);
			
			// create a container
			try {
				URI uri = new URI("xvsm://127.0.0.1:" + port);
				List<LindaCoordinator> coords = Arrays.asList(new LindaCoordinator(false));
	         	localContainer = localCapi.createContainer("coord" + port, uri, MzsConstants.Container.UNBOUNDED, coords, null, null);
				return localContainer;
			} catch (MzsCoreException | URISyntaxException e) {
				e.printStackTrace();
				return null;
			}
		} else
		{
			localCore = DefaultMzsCore.newInstanceWithoutSpace();
		
			localCapi = new Capi(localCore);
			try {
				URI uri = new URI("xvsm://127.0.0.1:" + port);
				localContainer = localCapi.lookupContainer("coord" + port, uri, 5000, null);
				return localContainer;
			} catch (MzsCoreException | URISyntaxException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	@Override
	public void stopTupleSpace()	{
		// destroy the container
		 try {
			localCapi.destroyContainer(localContainer, null);
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 // shutdown the core
		 localCore.shutdown(true);
	}
	
	@Override
	public void write(Object ...objects)	{
		Object tuple = objects[0];
		MozartProxy proxy = (MozartProxy)objects[1];
	//	Boolean local = (Boolean)objects[3];
		
		try {
			localCapi.write(proxy.localContainer, new Entry((Serializable) tuple));
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Object read(Object ...objects) 	{
		Object template = objects[0];
		MozartProxy proxy = (MozartProxy)objects[1];
	//	Boolean local = (Boolean)objects[3];
		
		try {
			ArrayList<Entry> readEntries = localCapi.read(proxy.localContainer, LindaCoordinator.newSelector((Serializable) template),  RequestTimeout.INFINITE, null);
			if(readEntries != null && readEntries.size() == 1)
				return readEntries.get(0);
			else
				return null;
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Object readIfExist(Object ...objects)	{
		Object template = objects[0];
		MozartProxy proxy = (MozartProxy)objects[1];
	//	Boolean local = (Boolean)objects[3];
		
		try {
			ArrayList<Entry> readEntries = localCapi.read(proxy.localContainer, LindaCoordinator.newSelector((Serializable) template), RequestTimeout.TRY_ONCE, null);
			if(readEntries != null && readEntries.size() == 1)
				return readEntries.get(0);
			else
				return null;
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Object take(Object  ...objects)	{
		Object template = objects[0];
		MozartProxy proxy = (MozartProxy)objects[1];
	//	Boolean local = (Boolean)objects[3];
		
		try {
			ArrayList<Entry> readEntries = localCapi.take(proxy.localContainer, LindaCoordinator.newSelector((Serializable) template),  RequestTimeout.INFINITE, null);
			if(readEntries != null && readEntries.size() == 1)
				return readEntries.get(0);
			else
				return null;
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Object takeIfExist(Object  ...objects)	{
		Object template = objects[0];
		MozartProxy proxy = (MozartProxy)objects[1];
	//	Boolean local = (Boolean)objects[3];
		
		try {
			ArrayList<Entry> readEntries = localCapi.take(proxy.localContainer, LindaCoordinator.newSelector((Serializable) template), RequestTimeout.TRY_ONCE, null);
			if(readEntries != null && readEntries.size() == 1)
				return readEntries.get(0);
			else
				return null;
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
			return null;
		}
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
			PanelTuple tuple = new PanelTuple((String)tupleFields[0], (String)tupleFields[1], (Panel)tupleFields[2]);
			return tuple;
		} else if(type.startsWith("UpdateOceanModelTuple"))
		{
			UpdateDataTuple tuple = new UpdateDataTuple((String)tupleFields[1], (Integer)tupleFields[2], (Integer)tupleFields[3], (Vector)tupleFields[4]);
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
		} else if(type.equals("UpdateOceanModelTuple"))
		{
			UpdateDataTuple tuple = (UpdateDataTuple)generalTuple;
			Object[] data = new Object[5];
			data[0] = tuple.getTaskType();
			data[1] = tuple.getFirstField();
			data[2] = tuple.getSecondField();
			data[3] = tuple.getThirdField();
			data[4] = tuple.getData();
			return data;
		}

		return null;
	}
}
