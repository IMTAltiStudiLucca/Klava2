/*
 * Created on 6 Nov 2016
 */
package klava.new_communication;

import klava.TupleSpace;
import klava.new_communication.TuplePack.eTupleOperation;

public class ProcessTuplePackSE extends Thread {


    TuplePack tPacket;
    // reference to local tuple space;
    TupleSpace tupleSpace;
    
    TCPNIOEntity tcpnioEntity;
    
    public ProcessTuplePackSE(TuplePack tPacket, TupleSpace tupleSpace, TCPNIOEntity tcpnioEntity) {
        this.tPacket = tPacket;
        this.tupleSpace = tupleSpace;
        this.tcpnioEntity = tcpnioEntity;
    }

    public void run() {
        try {
            processPacket(tPacket, tupleSpace, tcpnioEntity);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    public static void processPacket(TuplePack tPacket, TupleSpace tupleSpace, TCPNIOEntity tcpnioEntity) throws InterruptedException
    {             
       if(tPacket.operation == eTupleOperation.OUT) 
       {
           // write to the local tuple space
           tupleSpace.out(tPacket.tuple);
       } 
       else if(tPacket.operation == eTupleOperation.READ || 
               tPacket.operation == eTupleOperation.IN) 
       {
           
           if(!tPacket.blocking)
           {
               double startTime = System.nanoTime()/1000000d;
               
               boolean result = false;
               
               if(tPacket.operation == eTupleOperation.READ)
                   result = tupleSpace.read_nb(tPacket.tuple);
               else
                   result = tupleSpace.in_nb(tPacket.tuple);
               
               // #TODOVB
               // here we can use the same channel or use standard NIOSender
              // NIOSender sender = new NIOSender(tPacket.senderPort);
               NIOSender sender = tcpnioEntity.getSender(tPacket.lastSenderIPAddress);
               eTupleOperation previousOp = tPacket.operation;
               if(result)
               {
                   tPacket.tuple = tPacket.tuple;
                   tPacket.operation = eTupleOperation.TUPLEBACK;
               } else
               {
                   // just send that the tuple is absent
                   tPacket.operation = eTupleOperation.TUPLEABSENT;
               }
               
               // write response             
               sender.write(tPacket);            
           } else
           {
             //  Profiler.begin("READ_BLOCK_INSIDE");
               
               // #TODOVB
               // here is the simple version of waiting
                            
               long startTime = System.currentTimeMillis();
               
               if(tPacket.operation == eTupleOperation.READ)
                   tupleSpace.read(tPacket.tuple);
               else {
                   tupleSpace.in(tPacket.tuple);
               }

               //tPacket.tuple = result;
               tPacket.operation = eTupleOperation.TUPLEBACK;
               
               // write a response
               //NIOSender sender = new NIOSender(tPacket.senderPort);
               NIOSender sender = tcpnioEntity.getSender(tPacket.lastSenderIPAddress);
               sender.write(tPacket);                       
           }
       }
       else if(tPacket.operation == eTupleOperation.TUPLEABSENT || tPacket.operation == eTupleOperation.TUPLEBACK)
       {
           
           // just find a request and insert result packet   
           ProcessTuplePackRE.notifyAboutNewPacket(tcpnioEntity, tPacket);
           
           
//           // just find a request and insert result packet                     
//           tcpnioEntity.dataPairLock.lock();
//           {    
//               if(tcpnioEntity.pair == null)
//               {
//                   System.out.println("tPacket.operationID " + tPacket.operationID);
//                   System.out.println("tPacket.operation " + tPacket.operation);
//                   
//               }
//               if(tcpnioEntity.pair.getKey().equals(tPacket.operationID))
//               {                  
//                   tcpnioEntity.pair.setValue(tPacket);
//               }   
//               else
//                   System.err.println("Mistake in RequestProcessor");
//               
//               tcpnioEntity.responseIsBack.signal();
//           }
//           tcpnioEntity.dataPairLock.unlock();  
       }
       
       
       long end = System.currentTimeMillis();
    }
}
