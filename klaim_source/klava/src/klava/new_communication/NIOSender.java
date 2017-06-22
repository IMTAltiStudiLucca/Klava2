package klava.new_communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOSender
{
		//int port;
		SocketChannel sChannel;
		//ObjectOutputStream  ooStream;
		//public int port;
		public NIOSender(IPAddress ipAddress)
		{
		    //this.port = port;
			try {
				sChannel = SocketChannel.open();
				sChannel.socket().setTcpNoDelay(true);
		        sChannel.configureBlocking(true);
		        if (sChannel.connect(new InetSocketAddress(ipAddress.getIp(), ipAddress.getPort()))) 
		        {
		            // do something or just wait for an exception
		        }
	        
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		// should be synchronous
		public synchronized void write(Object obj)
        {
            if(obj != null)
            {
                try {
                    byte[] bytes = TuplePack.serializeObject(obj);
                    
                    
                    byte[] withDelimeter = new byte[bytes.length + NIOListener.delimeter.length];
                    System.arraycopy(bytes, 0, withDelimeter, 0, bytes.length);
                    System.arraycopy(NIOListener.delimeter, 0, withDelimeter, bytes.length, NIOListener.delimeter.length);
                    
                    //TuplePack.deserializeObject(bytes);
                //  System.out.println("size " + bytes.length);
                    
                    
                    ByteBuffer buffer = ByteBuffer.wrap(withDelimeter);
                    sChannel.write(buffer);
                    

                    /*ByteBuffer buffer = ByteBuffer.allocate(withDelimeter.length);
                    buffer.clear();
                    buffer.put(withDelimeter);

                    buffer.flip();

                    while(buffer.hasRemaining()) {
                        sChannel.write(buffer);
                    }*/
                    
                    
                 /*   ByteBuffer buf = ByteBuffer.allocate(withDelimeter.length);
                    buf.clear();
                    buf.put(withDelimeter);

                    buf.flip();

                    while(buf.hasRemaining()) {
                        sChannel.write(buf);
                    }*/

 //                   shutdown();
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
