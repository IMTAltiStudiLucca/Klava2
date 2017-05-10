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
		public int port;
		
		public NIOSender(int port)
		{
		    this.port = port;
			try {
				sChannel = SocketChannel.open();
				sChannel.socket().setTcpNoDelay(true);
		        sChannel.configureBlocking(true);
		        if (sChannel.connect(new InetSocketAddress("localhost", port))) 
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
                   
                    
                    ByteBuffer buffer = ByteBuffer.wrap(withDelimeter);
                    sChannel.write(buffer);            

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
