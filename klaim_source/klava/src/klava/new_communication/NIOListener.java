package klava.new_communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import klava.new_communication.TuplePack.eTupleOperation;

public class NIOListener {
	
    // main channel for listening
	ServerSocketChannel ssChannel;
	// selector for accepting and reading
	Selector acceptSelector;
	// thread for listening
    private Thread ioThread; 
    // object for stopping ioThread
    Boolean stopFLag = false;
    
    // thread pool
    final int threadPoolSize = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
    
    public static byte[] delimeter = new byte[]{(byte) 0xFF, (byte) 0x00, (byte)0xFF, (byte) 0x00};
    
    public class ConnectionBuffer
    {
        public ConnectionBuffer()
        {
            outStream = new ByteArrayOutputStream();
            lastDelimiterBytes = null;
        }
        public ByteArrayOutputStream outStream;
        public byte[] lastDelimiterBytes;
        
    }
   
    // reference to local tuple space
//    TupleSpace tupleSpace;
    TCPNIOEntity tcpnioEntity;
    
    boolean withReplication = false;
	
	public NIOListener(int port, TCPNIOEntity tcpnioEntity, boolean withReplication)
	{
	    this.tcpnioEntity = tcpnioEntity;
	    this.withReplication = withReplication;
	    
		try {
			ssChannel = ServerSocketChannel.open();
			ssChannel.bind(new InetSocketAddress(port));
			ssChannel.configureBlocking(false);
			
			acceptSelector = Selector.open();	
	        ssChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);

            Runnable run = new Runnable() {
               
                public void run() {
                	try {
						processingLoop();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                            // This runs for a long time
                    //ioThread = null;
                }   // end run
            };  // end runnable
            
			ioThread = new Thread( run, this.getClass().getName() );
			ioThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start()
	{
		stopFLag = false;
		if(ioThread != null)
			ioThread.start();
	}
	
	public void shutdown()
	{
		stopFLag = true;
		executor.shutdownNow();
	}
	
	
	Hashtable<String, ConnectionBuffer> connections = new Hashtable<String, ConnectionBuffer>();
	
	
	private void processingLoop() throws IOException
	{    
        while(true) {
 //       	System.out.println("wait");
            int count = acceptSelector.select(1000);
            
            // stop the thread
            if(stopFLag == true)
            	break;
            
            if(count != 0 )
            {
                for (Iterator<SelectionKey> i = acceptSelector.selectedKeys().iterator();i.hasNext();) 
                {                  
                    SelectionKey key = i.next();
                    
                    if (!key.isValid()) {
                        System.out.println("key is not valid");
                        key.cancel();
                        continue;
                    } 
                    
                    if(key.isAcceptable()) 
                    {
                        // accept a new connection
                		SocketChannel client = ssChannel.accept();
                		client.socket().setTcpNoDelay(true);
                		client.configureBlocking(false);
                		
                		// register for the read operation
                		client.register(acceptSelector, SelectionKey.OP_READ);		
                    } else if(key.isReadable())
                    {
                        //key.cancel();
                        
                        SocketChannel readChannel = (SocketChannel)key.channel();
                        String address = readChannel.getRemoteAddress().toString();
                        
                        // save a stream for each connection (ByteArrayOutputStream)
                        if(!connections.containsKey(address))
                            connections.put(address, new ConnectionBuffer());
                        
                        nonBlockingReading(readChannel);    
                        
                    }
                    i.remove();
                }
            }
        }		
	}

	
	/*
	 * implements non-blocking reading where first the data are read and after the new thread for data processing starts
	 */
    private void nonBlockingReading(SocketChannel readChannel) throws IOException {
       
        double startTime = System.nanoTime()/1000000d;
        
        // read the buffer
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.clear();

        ConnectionBuffer connectionBuffer = connections.get(readChannel.getRemoteAddress().toString());
        
        //int count = 0;
        int read;

        while ((read = readChannel.read(buffer)) > 0) 
        {
            
            buffer.flip();
            byte[] arr = new byte[buffer.remaining()];
            buffer.get(arr);
            
            // combine two array to prepare the data for delimiter check
            // it is necessary in the case if part of the delimiter was transfered in the previous packet
            arr = prepareDataForDelimiterChecking(connectionBuffer, arr);
            
            while(true)
            {
                // analyse the arr
                int position = -1;
                for(int i =0; i<arr.length; i++)
                {
                    int result = getDelimiterPosition(arr, i, delimeter);
                    if(result > 0)
                    {
                        position = i;
                        break;
                    }
                }
                
                // if there is no delimiter add data and finish
                if(position == -1)
                {
                    copyToConnectionBuffer(connectionBuffer, arr, delimeter.length);
                    buffer.clear();
                    // System.out.println("saved " + arr.length); 
                    break;
                } else  // there is delimiter
                {                   
                    // get part of the previous packet
                    byte[] partByteArray = new byte[position];
                    System.arraycopy(arr, 0, partByteArray, 0, position);                
                    connectionBuffer.outStream.write(partByteArray);
    
                    
                    byte[] readBytes = connectionBuffer.outStream.toByteArray();
                    connectionBuffer.outStream.reset();  
                                       
                    TuplePack tPacket = (TuplePack)TuplePack.deserializeObject(readBytes);         
                    
                    int taskNumber = ((ThreadPoolExecutor) executor).getActiveCount();
                    
                    // operations which generate new tuples are more important 
                    // because sometimes the progress of other processes depend on these tuples
                    if(taskNumber < threadPoolSize || (tPacket.operation != eTupleOperation.OUT && tPacket.operation != eTupleOperation.TUPLEBACK))
                    {
                        Runnable t = withReplication ? new ProcessTuplePackRE(tPacket, tcpnioEntity.tupleSpace, tcpnioEntity) :
                            new ProcessTuplePackSE(tPacket, tcpnioEntity.tupleSpace, tcpnioEntity);
                        executor.execute(t);
                                         
                    } else
                    {
                        Thread t = withReplication ? new ProcessTuplePackRE(tPacket, tcpnioEntity.tupleSpace, tcpnioEntity):
                            new ProcessTuplePackSE(tPacket, tcpnioEntity.tupleSpace, tcpnioEntity);
                        t.start();
                    }
                                        
                    // get part of the next packet
                    if(position + delimeter.length < arr.length)
                    {
                        // here is the problem if delimiter is splitted
                        int dataSize = arr.length - position - delimeter.length;
                        byte[] newPacketData = new byte[dataSize];
                        System.arraycopy(arr, position + delimeter.length, newPacketData, 0, dataSize);
                        
                        // continue to search for the delimiter
                        arr = newPacketData;    
                    } else
                        break;
                }
            }            
            buffer.clear();
            
            double endTime = System.nanoTime()/1000000d;
        }              
    }
    
    
    static void copyToConnectionBuffer(ConnectionBuffer connectedBuffer, byte[] incommingDataWithoutDelimiter, int delimiterSize) throws IOException
    {
        if(incommingDataWithoutDelimiter.length > delimiterSize)
        {
            connectedBuffer.outStream.write(incommingDataWithoutDelimiter, 0, incommingDataWithoutDelimiter.length - delimiterSize);
            
            // update last bytes for checking delimiter
            connectedBuffer.lastDelimiterBytes = new byte[delimiterSize];
            System.arraycopy(incommingDataWithoutDelimiter, incommingDataWithoutDelimiter.length - delimiterSize, connectedBuffer.lastDelimiterBytes, 0, delimiterSize);
        }
        else {            
            // update last bytes for checking delimiter
            int dataSize = incommingDataWithoutDelimiter.length;
            connectedBuffer.lastDelimiterBytes = new byte[dataSize];
            System.arraycopy(incommingDataWithoutDelimiter, 0, connectedBuffer.lastDelimiterBytes, 0, dataSize);
        }
        
    }
    
    static byte[] prepareDataForDelimiterChecking(ConnectionBuffer connectedBuffer, byte[] newIncommingData)
    {
        if(connectedBuffer.lastDelimiterBytes != null && connectedBuffer.lastDelimiterBytes.length > 0)
        {
            int bufferForDelimiterCheckSize = connectedBuffer.lastDelimiterBytes.length + newIncommingData.length;
            byte[] bufferForDelimiterCheck = new byte[bufferForDelimiterCheckSize];
            System.arraycopy(connectedBuffer.lastDelimiterBytes, 0, bufferForDelimiterCheck, 0, connectedBuffer.lastDelimiterBytes.length);
            System.arraycopy(newIncommingData, 0, bufferForDelimiterCheck, connectedBuffer.lastDelimiterBytes.length, newIncommingData.length);
            connectedBuffer.lastDelimiterBytes = null;
            return bufferForDelimiterCheck;
        }     

        return newIncommingData;
    }
    
    /*
     *  
     */
    static int getDelimiterPosition(byte[] array, int startPosition, byte[] delimiter)
    {
        int countBytes = 0;
        for(int i=0; i < delimiter.length; i++)
        {
            if(startPosition + i >= array.length)
                return -1;
                
            if(array[startPosition + i] == delimiter[i])
            {
                countBytes++;
                
            } else
                return -1;   
        }
        
        if(countBytes == delimiter.length)
            return 1;
        return -1;
    }
	
}
