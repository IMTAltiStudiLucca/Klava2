package klava.new_communication;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import klava.TupleSpace;
import klava.topology.KlavaNode.eReplicationType;


// Requirements:
// 

public class TCPNIOEntity 
{
    // physical address
    IPAddress ipAddress = null;
	
	// listener
	NIOListener nioListener = null;
	//NIOSender sender;
	
    Hashtable<String, NIOSender> senderTable = new Hashtable<String, NIOSender>();
	
    // for tuple as response
    ConcurrentHashMap<Long, NullableTuplePack> responseMap = new ConcurrentHashMap<>();
    
    final Lock dataPairLock = new ReentrantLock();
    final Condition responseIsBack = dataPairLock.newCondition();
	
	// reference to local tuple space
	TupleSpace tupleSpace;
	
	private Long operationSequenceID = 0L;
	
	Queue<TuplePack> requestQueue = new LinkedList<TuplePack>();
	
    public TCPNIOEntity(IPAddress ipAddress, TupleSpace tupleSpace, eReplicationType replicationType) throws IOException
	{
	    this.ipAddress = ipAddress;
	    this.tupleSpace = tupleSpace;
		nioListener = new NIOListener(ipAddress.getPort(), this, replicationType != eReplicationType.NONE);
		nioListener.start();
	}
	
	public NIOSender getSender(IPAddress ipAddress)
	{
	    String address = String.valueOf(ipAddress.returnFullAddress());
	    if(senderTable.containsKey(address))
	    {
	        return senderTable.get(address);
	    } else
	    {
	        NIOSender newSender = null;
            try {
                newSender = new NIOSender(ipAddress);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
