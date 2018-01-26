package klava.new_communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class NIOSender
{

    SocketChannel sChannel;
    
    IPAddress ipAddress;
    
    /*  
    public NIOSender(IPAddress ipAddress)
    {
        this.ipAddress = ipAddress;
    }
    
    // should be synchronous
    public synchronized void write(Object obj)
    {
        String receiverAddress = ipAddress.getFullAddress();
        try {
            JVMSharedChannels.addData(receiverAddress, (IPack)obj);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
    
   		
		public NIOSender(IPAddress ipAddress) throws IOException
		{
		    this.ipAddress = ipAddress;
			sChannel = SocketChannel.open();
			sChannel.socket().setTcpNoDelay(true);
			sChannel.socket().setSendBufferSize(65536);
	        sChannel.configureBlocking(true);
	        if (sChannel.connect(new InetSocketAddress(ipAddress.getIp(), ipAddress.getPort()))) 
	        {
	            // do something or just wait for an exception
	        }	
		}
		

		
		// should be synchronous
		public synchronized void write(Object obj)
        {
            if(obj != null) {
                try {
                    double startTime = System.nanoTime()/1000000d;
                    byte[] bytes = TuplePack.serializeObject(obj);
                    
                    double endTime = System.nanoTime()/1000000d;
                
                    byte[] withDelimeter = new byte[bytes.length + NIOListener.delimeter.length];
                    System.arraycopy(bytes, 0, withDelimeter, 0, bytes.length);
                    System.arraycopy(NIOListener.delimeter, 0, withDelimeter, bytes.length, NIOListener.delimeter.length);
                    
                    
                    double diff = endTime - startTime;
                    
                    //System.out.println("bytes "+ bytes.length);
                    //System.out.println("time "+ diff);
                    
                    //TuplePack.deserializeObject(bytes);
                    //System.out.println("size " + bytes.length);
                     
                 //   ByteBuffer buffer = ByteBuffer.wrap(withDelimeter);
                //   sChannel.write(buffer);     

                    ByteBuffer buffer = ByteBuffer.allocate(withDelimeter.length);
                    buffer.clear();
                    buffer.put(withDelimeter);

                    buffer.flip();
                    while(buffer.hasRemaining()) {
                        sChannel.write(buffer);
                    }
                    

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    
                    System.err.println("error writeObject");
                    e.printStackTrace();
                }
            }
            
            
        }
		
		public void shutdown()
		{
			try {
				sChannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
	
		
}
