package interfaces;

/*public interface ITupleSpace
{
	public Object startTupleSpace(String address);
	
	public void stopTupleSpace(String address);
	
	public void write(Object template);
	
	public Object read(Object template);
	public Object readIfExist(Object template);
	
	public Object take(Object template);
	public Object takeIfExist(Object template);
	
	public Object formTuple(String type, Object ...objects);
	
	public Object[] tupleToObjectArray(String type, Object tuple);
}*/


public class ITupleSpace
{
	public Object startTupleSpace(Object ...objects)
	{
		return null;
	}
	
	public void stopTupleSpace()	{

	}
	
	public void write(Object ...objects)	{

	}
	
	public Object read(Object ...objects) 	{
		return null;
	}
	
	public Object readIfExist(Object ...objects)	{
		return null;
	}
	
	public Object take(Object  ...objects)	{
		return null;
	}
	
	public Object takeIfExist(Object  ...objects)	{
		return null;
	}
	
	public Object formTuple(String type, Object[] tupleFields, Object[] fieldsType)	{
		return null;
	}
	
	public Object[] tupleToObjectArray(String type, Object tuple)	{
		return null;
	}
}
