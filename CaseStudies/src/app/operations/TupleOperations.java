package app.operations;

import common.TupleLogger;
import interfaces.ITupleSpace;

public class TupleOperations 
{

	public static void writeTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
   	{
   		if(withProfiling)
   		{
   			if(local)
   			{
	   			TupleLogger.begin("write::local");
	   			receiverSpace.write(obj, receiverSpace, senderSpace, local);
				TupleLogger.end("write::local");
   			} else
   			{
   				TupleLogger.begin("write::remote");
	   			receiverSpace.write(obj, receiverSpace, senderSpace, local);
	   			TupleLogger.end("write::remote");
   			}
   		} else
   			receiverSpace.write(obj, receiverSpace, senderSpace, local);
   	}
   	
   	public static Object readTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
   	{
		String threadName = "all" + Thread.currentThread().getName();
		
   		Object result = null;
   		if(withProfiling)
   		{
   			if(local)
   			{
   				TupleLogger.begin("take::local");
	   			result = receiverSpace.read(obj, receiverSpace, senderSpace, local);
	   			TupleLogger.end("take::local");
   			} else
   			{
   				TupleLogger.begin("take::remote");
	   			result = receiverSpace.read(obj, receiverSpace, senderSpace, local);
	   			TupleLogger.end("take::remote");
   			}
   		} else
   			result = receiverSpace.read(obj, receiverSpace, senderSpace, local);
   		
   		return result;
   	}
   	
   	public static Object readIfExistTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
   	{	
   		Object result = null;
   		if(withProfiling)
   		{
   			if(local)
   			{
   				TupleLogger.begin("takeE::local");
	   			result = receiverSpace.readIfExist(obj, receiverSpace, senderSpace, local);
	   			TupleLogger.end("takeE::local");
   			} else
   			{
   				TupleLogger.begin("takeE::remote");
	   			result = receiverSpace.readIfExist(obj, receiverSpace, senderSpace, local);
	   			TupleLogger.end("takeE::remote");
   			}
   		} else
   			result = receiverSpace.readIfExist(obj, receiverSpace, senderSpace, local);
   		
   		return result;
   	}
   	
   	public static Object takeTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
   	{
   		Object result = null;
   		if(withProfiling)
   		{
   			if(local)
   			{
   				TupleLogger.begin("take::local");
	   			result = receiverSpace.take(obj, receiverSpace, senderSpace, local);
	   			TupleLogger.end("take::local");
   			} else
   			{
   				TupleLogger.begin("take::remote");
	   			result = receiverSpace.take(obj, receiverSpace, senderSpace, local);
	   			TupleLogger.end("take::remote");
   			}
   		} else
   			result = receiverSpace.take(obj, receiverSpace, senderSpace, local);
   		
   		return result;
   	}
   	
   	public static Object takeIfExistTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
 	{
   		Object result = null;
   		if(withProfiling)
   		{
   			if(local)
   			{
   				TupleLogger.begin("takeE::local");
	   			result = receiverSpace.takeIfExist(obj, receiverSpace, senderSpace, local);
   				TupleLogger.end("takeE::local");
   			} else
   			{
   				TupleLogger.begin( "takeE::remote");
	   			result = receiverSpace.takeIfExist(obj, receiverSpace, senderSpace, local);
   				TupleLogger.end("takeE::remote");
   			}
   		} else
   			result = receiverSpace.takeIfExist(obj, receiverSpace, senderSpace, local);
   		return result;
   	}
   	
	/*
 	public static void writeTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
   	{
   		if(withProfiling)
   		{
   			if(local)
   			{
   				String threadName = "all" + Thread.currentThread().getName();
   				
	   			DProfiler.begin("write::local");
	   			
	   			TupleLogger.begin(threadName, "write::local");
	   			receiverSpace.write(obj, receiverSpace, senderSpace, local);
				TupleLogger.end(threadName, "write::local");
				
	   			DProfiler.end("write::local");
	   
   			} else
   			{
	   			DProfiler.begin("write::remote");
	   			receiverSpace.write(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("write::remote");
   			}
   		} else
   			receiverSpace.write(obj, receiverSpace, senderSpace, local);
   	}
   	
   	public static Object readTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
   	{
   		Object result = null;
   		if(withProfiling)
   		{
   			if(local)
   			{
   				DProfiler.begin("take::local");
	   			result = receiverSpace.read(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("take::local");
   			} else
   			{
   				DProfiler.begin("take::remote");
	   			result = receiverSpace.read(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("take::remote");
   			}
   		} else
   			result = receiverSpace.read(obj, receiverSpace, senderSpace, local);
   		
   		return result;
   	}
   	
   	public static Object readIfExistTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
   	{
   		Object result = null;
   		if(withProfiling)
   		{
   			if(local)
   			{
   				DProfiler.begin("take::local");
	   			result = receiverSpace.readIfExist(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("take::local");
   			} else
   			{
   				DProfiler.begin("take::remote");
	   			result = receiverSpace.readIfExist(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("take::remote");
   			}
   		} else
   			result = receiverSpace.readIfExist(obj, receiverSpace, senderSpace, local);
   		
   		return result;
   	}
   	
   	public static Object takeTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
   	{
   		Object result = null;
   		if(withProfiling)
   		{
   			if(local)
   			{
   				String threadName = "all" + Thread.currentThread().getName();
   	//			TupleLogger.begin(threadName, "take::local");
   				
   				DProfiler.begin("take::local");
	   			result = receiverSpace.take(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("take::local");
	   			
	  // 			TupleLogger.end(threadName, "take::local");
   			} else
   			{
   				DProfiler.begin("take::remote");
	   			result = receiverSpace.take(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("take::remote");
   			}
   		} else
   			result = receiverSpace.take(obj, receiverSpace, senderSpace, local);
   		
   		return result;
   	}
   	
   	public static Object takeIfExistTuple(ITupleSpace receiverSpace, ITupleSpace senderSpace, Object obj, boolean local, boolean withProfiling)
 	{
   		Object result = null;
   		if(withProfiling)
   		{
   			if(local)
   			{
   				DProfiler.begin("take::local");
	   			result = receiverSpace.takeIfExist(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("take::local");
   			} else
   			{
   				DProfiler.begin("take::remote");
	   			result = receiverSpace.takeIfExist(obj, receiverSpace, senderSpace, local);
	   			DProfiler.end("take::remote");
   			}
   		} else
   			result = receiverSpace.takeIfExist(obj, receiverSpace, senderSpace, local);
   		
   		return result;
   	}*/
}
