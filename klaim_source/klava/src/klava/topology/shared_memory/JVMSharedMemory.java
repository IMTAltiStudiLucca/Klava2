package klava.topology.shared_memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import klava.proto.TuplePacket;

public class JVMSharedMemory
{
    static public Lock SynchonizationLock = new ReentrantLock();
	static private HashMap<String, ArrayList<TuplePacket>> SharedMemory = new HashMap<String, ArrayList<TuplePacket>>();
	
	public JVMSharedMemory()
	{
		//SharedMemory = new HashMap<String, ArrayList<Object>>();
	}
	
	/**
	 * get data from the shared memory using a thread identifier
	 * @param recieverName
	 * @return
	 */
	public static ArrayList<TuplePacket> getData(String recieverName)
	{
		synchronized (SharedMemory) 
		{
		    //SynchonizationLock.lock();
			ArrayList<TuplePacket> data = SharedMemory.get(recieverName);
			SharedMemory.remove(recieverName);
			//SynchonizationLock.unlock();
			return data;
		}
	}

	/**
	 * add data to the shared memory
	 * @param recieverName
	 * @param obj
	 */
	public static void addData(String recieverName, TuplePacket obj)
	{
	    //SynchonizationLock.lock();
		synchronized (SharedMemory) 
		{
			if(SharedMemory.containsKey(recieverName))
				SharedMemory.get(recieverName).add(obj);
			else
			{
				ArrayList<TuplePacket> list = new ArrayList<TuplePacket>();
				list.add(obj);
				SharedMemory.put(recieverName, list);
				
				
				// System.out.println("new data added");
			}
		}
		//SynchonizationLock.unlock();
		
	}
	
	
}
