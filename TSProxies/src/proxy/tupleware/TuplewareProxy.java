package proxy.tupleware;

import java.io.IOException;

import interfaces.ITupleSpace;
import runtime.TupleSpaceRuntime;
import scope.Scope;
import space.Tuple;
import space.TupleTemplate;

public class TuplewareProxy extends ITupleSpace
{
	
	TupleSpaceRuntime ts = null;
	Scope scope = new Scope("matrix");
	
	@Override
	public Object startTupleSpace(Object ...objects) 
	{
		Integer port = (Integer)objects[0];
		Integer numberOfWorkers = (Integer)objects[1];
		Boolean mainInitialization = (Boolean)objects[2];
		
		if(mainInitialization)
		{
			boolean mainPort = port == 6001;
			ts = new TupleSpaceRuntime(port, mainPort, numberOfWorkers);
		    ts.start();
			return ts;
		} else
			return null;
	}

	@Override
	public void stopTupleSpace() {
		
		ts.stop();		
	}

	@Override
	public void write(Object ...objects) {
		Tuple tuple = (Tuple)objects[0];
		Object tupleSpace = objects[1];
		TupleSpaceRuntime localSpace = ((TuplewareProxy)objects[2]).ts;
		Boolean local = (Boolean)objects[3];
		
		if(local)
			localSpace.ts.out(tuple);
		else
		{
			try {
				localSpace.gts.out(tuple);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	/*	if(tupleSpace instanceof TupleSpaceRuntime)
		{
			((TupleSpaceRuntime)tupleSpace).out(tuple);
		} 
		else if(tupleSpace instanceof TupleSpaceStub)
		{
			try {
				((TupleSpaceStub)tupleSpace).out(tuple);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}

	@Override
	public Object read(Object ...objects) 
	{
		TupleTemplate template = transformFromTupleToTemplate(objects[0]);	
		Object tupleSpace = objects[1];
		TupleSpaceRuntime localSpace = ((TuplewareProxy)objects[2]).ts;
		Boolean local = (Boolean)objects[3];
		
		Tuple result = null;
		if(local)
			result = localSpace.ts.rd(template);
		else
		{
			try {
				result = localSpace.gts.rd(template);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
	/*	if(tupleSpace instanceof TupleSpaceRuntime)
		{
			result = ((TupleSpaceRuntime)tupleSpace).rd(template);
		} 
		else if(tupleSpace instanceof TupleSpaceStub)
		{
			try {
				result = ((TupleSpaceStub)tupleSpace).rd(template);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

		return result;
	}

	@Override
	public Object readIfExist(Object ...objects) 
	{
		TupleTemplate template = transformFromTupleToTemplate(objects[0]);	
		Object tupleSpace = objects[1];
		TupleSpaceRuntime localSpace = ((TuplewareProxy)objects[2]).ts;
		Boolean local = (Boolean)objects[3];
		
		Tuple result = null;
		
		if(local)
			result = localSpace.rdp2(template);
		else
		{
			try {
				result = localSpace.gts.rdp(template);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		/*if(tupleSpace instanceof TupleSpaceRuntime)
		{
			result = ((TupleSpaceRuntime)tupleSpace).rdp(template);
		} 
		else if(tupleSpace instanceof TupleSpaceStub)
		{
			try {
				result = ((TupleSpaceStub)tupleSpace).rdp(template);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		return result;
	}

	@Override
	public Object take(Object ...objects) 
	{
		TupleTemplate template = transformFromTupleToTemplate(objects[0]);		
		Object tupleSpace = objects[1];
		TupleSpaceRuntime localSpace = ((TuplewareProxy)objects[2]).ts;
		Boolean local = (Boolean)objects[3];
		
		Tuple result = null;
		
		if(local)
			result = localSpace.in(template);  // ts.in
		else
		{
			try {
				result = localSpace.gts.in(template);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		/*if(tupleSpace instanceof TupleSpaceRuntime)
		{
			result = ((TupleSpaceRuntime)tupleSpace).in(template);
		} 
		else if(tupleSpace instanceof TupleSpaceStub)
		{
			try {
				result = ((TupleSpaceStub)tupleSpace).in(template);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		return result;
	}

	@Override
	public Object takeIfExist(Object ...objects) 
	{
		TupleTemplate template = transformFromTupleToTemplate(objects[0]);	
		Object tupleSpace = objects[1];
		TupleSpaceRuntime localSpace = ((TuplewareProxy)objects[2]).ts;
		Boolean local = (Boolean)objects[3];
		
		Tuple result = null;
		
		if(local)
			result = localSpace.inp2(template); // ts
		else
		{
			try {
				result = localSpace.gts.inp(template);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		/*if(tupleSpace instanceof TupleSpaceRuntime)
		{
			result = ((TupleSpaceRuntime)tupleSpace).inp(template);
		} 
		else if(tupleSpace instanceof TupleSpaceStub)
		{
			try {
				result = ((TupleSpaceStub)tupleSpace).inp(template);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		return result;
	}
	
	// from Tuple to TupleTemplate
	private TupleTemplate transformFromTupleToTemplate(Object tuple) 
	{
		Tuple templateAsTuple = (Tuple)tuple;
		TupleTemplate template = new TupleTemplate(scope, tupleToObjectArray(null, templateAsTuple));
		return template;
	}
	
	@Override
	public Object formTuple(String type, Object[] tupleFields, Object[] fieldsType) 
	{		
		
		Tuple tuple = new Tuple(scope, tupleFields);	
		return tuple;
	}
	
	@Override
	public Object[] tupleToObjectArray(String type, Object generalTuple) 
	{
		Tuple tuple = (Tuple)generalTuple;
		Object[] objects = new Object[tuple.size()];
		for(int i = 0; i < tuple.size(); i++)
		{
			objects[i] = tuple.field(i);
		}
		return objects;
	}
	

}
