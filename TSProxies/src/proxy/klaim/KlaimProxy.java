package proxy.klaim;

import java.util.Vector;

import apps.sorting.QSort;
import case_studies.Panel;
import common.DataGeneration;
import interfaces.ITupleSpace;
import klava.KlavaException;
import klava.Tuple;
import klava.topology.KlavaNode;

public class KlaimProxy extends ITupleSpace
{
	
	public KlavaNode localNode = null;
	@Override
	public Object startTupleSpace(Object ...objects) 
	{
		KlavaNode space = (KlavaNode)objects[0];
//		Boolean IsfirstCreation = (Boolean)objects[2];
	//	if(IsfirstCreation)
	//	    space.startTupleSpace();

		localNode = space;
		return localNode;
	}

	@Override
	public void stopTupleSpace() {
	/*	try {
			localNode.close();
		} catch (IMCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	@Override
	public void write(Object ...objects) {
		Tuple tuple = (Tuple)objects[0];
		
		for(int i = 0; i < tuple.length(); i++)
		{
			Object item = tuple.getItem(i);
			if(item != null && (item instanceof Integer[]) || (item instanceof int[]))
			{
				int[] array = (int[])item;
				tuple.setItem(i, KlaimQData.convertArrayToString(array));
			}
			
			if(item != null && (item instanceof QSort))
			{
				int[] array = (int[])item;
				tuple.setItem(i, DataGeneration.serializeObject(array));
			}
		}
		
		KlaimProxy receiverNode = (KlaimProxy)objects[1];
		KlaimProxy senderNode = (KlaimProxy)objects[2];
	
		try {
			senderNode.localNode.out(tuple, receiverNode.localNode.getPhysical(KlavaNode.self));
		} catch (KlavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object read(Object ...objects) {
		Tuple template = (Tuple)objects[0];
		
		checkTemplate(template);
		
		KlaimProxy receiverNode = (KlaimProxy)objects[1];
		KlaimProxy senderNode = (KlaimProxy)objects[2];

		try {
			senderNode.localNode.read(template, receiverNode.localNode.getPhysical(KlavaNode.self));
		} catch (KlavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tuple result = template;
		return result;
	}

	private void checkTemplate(Tuple template) {
		for(int i =0; i< template.length(); i++)
		{
			if( template.getItem(i).equals(int[].class) || 
				template.getItem(i).equals(Integer[].class) ||
				template.getItem(i).equals(QSort.class)
				)
			{
				template.setItem(i, String.class);
			}
		}
	}

	@Override
	public Object readIfExist(Object ...objects) {
		Tuple template = (Tuple)objects[0];
		checkTemplate(template);
		
		KlaimProxy receiverNode = (KlaimProxy)objects[1];
		KlaimProxy senderNode = (KlaimProxy)objects[2];
		
		try {
			boolean result = senderNode.localNode.read_nb(template, receiverNode.localNode.getPhysical(KlavaNode.self));
			if(result == false)
				return null;
		} catch (KlavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tuple result = template;
		return result;
	}

	@Override
	public Object take(Object ...objects) 
	{
		Tuple template = (Tuple)objects[0];
		checkTemplate(template);
		
		KlaimProxy receiverNode = (KlaimProxy)objects[1];
		KlaimProxy senderNode = (KlaimProxy)objects[2];
		
		try {
			senderNode.localNode.in(template, receiverNode.localNode.getPhysical(KlavaNode.self));
		} catch (KlavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tuple result = template;
		return result;
	}

	@Override
	public Object takeIfExist(Object ...objects) {
		Tuple template = (Tuple)objects[0];
		checkTemplate(template);
		
		KlaimProxy receiverNode = (KlaimProxy)objects[1];
		KlaimProxy senderNode = (KlaimProxy)objects[2];
		
		try {
			boolean result = senderNode.localNode.in_nb(template, receiverNode.localNode.getPhysical(KlavaNode.self));
			if(result == false)
				return null;
		} catch (KlavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tuple result = template;
		return result;
	}

	@Override
	public Object formTuple(String type, Object[] tupleFields, Object[] fieldsType) 
	{
		for(int i =0; i<tupleFields.length; i++)
		{	
			if(tupleFields[i] != null && fieldsType[i].equals(Integer[].class))
			{
				int[] array = (int[])tupleFields[i];
				tupleFields[i] = KlaimQData.convertArrayToString(array);	
			}
			
			if(tupleFields[i] != null && (fieldsType[i].equals(QSort.class) || tupleFields[i].getClass().equals(Panel.class) || tupleFields[i].getClass().equals(Vector.class)))
			{
				tupleFields[i] = DataGeneration.serializeObject(tupleFields[i]);	
			}
			
			if(tupleFields[i] == null)
				tupleFields[i] = fieldsType[i];
			
			if(tupleFields[i].equals(Integer[].class) || tupleFields[i].equals(QSort.class) || 
			        tupleFields[i].equals(Panel.class) || tupleFields[i].equals(Vector.class))
			{
				tupleFields[i] = String.class;
			}
				
		}
		Tuple tuple = new Tuple(type, tupleFields);
		return tuple;
	}
	
	@Override
	public Object[] tupleToObjectArray(String type, Object generalTuple) 
	{
		Tuple tuple = (Tuple)generalTuple;
		Object[] objects = new Object[tuple.length()];
		for(int i = 0; i < tuple.length(); i++)
		{
			objects[i] = tuple.getItem(i);
			//Tuple template = tuple.getOriginalTemplate();
		}
		
		if(type.equals("MatrixRowTuple") && tuple != null)
		{
			String strData = (String)objects[5];
			if (strData != null)
			{
				objects[5] = KlaimQData.convertStringToArray(strData);
			}		
		}
		
		if(type.equals("SortingTuple") && tuple != null)
		{
			if(objects[1] instanceof String == false)
				objects[1] = null;
			
			String strData = (String)objects[1];
			if (strData != null)
			{
				objects[1] = DataGeneration.deserializeObject(strData);  			
			}		
		}
		
		if(type.equals("PanelTuple") && tuple != null)
        {
            if(objects[2] instanceof String == false)
                objects[2] = null;
            
            String strData = (String)objects[2];
            if (strData != null)
            {
                objects[2] = DataGeneration.deserializeObject(strData);             
            }       
        }
		
		if(type.equals("UpdateOceanModelTuple") && tuple != null)
        {
            if(objects[4] instanceof String == false)
                objects[4] = null;
            
            String strData = (String)objects[4];
            if (strData != null)
            {
                objects[4] = DataGeneration.deserializeObject(strData);             
            }       
        }
		return objects;
	}
	
}
