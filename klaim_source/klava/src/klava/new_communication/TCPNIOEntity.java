package klava.new_communication;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import common.CustomPair;
import klava.TupleSpace;


// Requirements:
// 

public class TCPNIOEntity 
{
	String host;
	public int port;
	
	// listener
	NIOListener nioListener = null;
	
    Hashtable<String, NIOSender> senderTable = new Hashtable<String, NIOSender>();
	
    CustomPair<Long, TuplePack> pair = null;
    
    final Lock dataPairLock = new ReentrantLock();
    final Condition responseIsBack = dataPairLock.newCondition();
	
	
	// reference to local tuple space
	TupleSpace tupleSpace;
	
	private Long operationSequenceID = 0L;
	
	Queue<TuplePack> requestQueue = new LinkedList<TuplePack>();
    private Thread processorThread; 
	
	
	public TCPNIOEntity(int port, TupleSpace tupleSpace) throws IOException
	{
	    this.port = port;
	    this.tupleSpace = tupleSpace;
		nioListener = new NIOListener(port, this);
		nioListener.start();

	}
	
	public NIOSender getSender(int senderPort)
	{
	    String address = String.valueOf(senderPort);
	    if(senderTable.containsKey(address))
	    {
	        return senderTable.get(address);
	    } else
	    {
	        NIOSender newSender = new NIOSender(senderPort);
	        senderTable.put(address, newSender);
	        return newSender;
	    }
	}
	
	
	public long getNextOperationSequenceID()
	{
	    long threadName = Thread.currentThread().getId();
	    Long nexOperationID;
	    synchronized (operationSequenceID) {
	        nexOperationID = threadName*10000 + operationSequenceID++;
        }
	    return nexOperationID;
	}
    
	
	
}
