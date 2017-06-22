/*
 * Created on 6 Nov 2016
 */
package klava.new_communication;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import klava.Tuple;
import klava.TupleSpace;
import klava.new_communication.TuplePack.eTupleOperation;
import klava.replication.RepliTuple;

public class ProcessTuplePackRE  extends Thread {

    TuplePack tPacket;
    
    // reference to a local tuple space;
    TupleSpace tupleSpace;
    
    TCPNIOEntity tcpnioEntity;
    
    public ProcessTuplePackRE(TuplePack tPacket, TupleSpace tupleSpace, TCPNIOEntity tcpnioEntity) {
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
       if(tPacket.operation == eTupleOperation.OUT || tPacket.operation == eTupleOperation.OUT_REPL) {
           tupleSpace.out(tPacket.tuple);
       } 
       else if(tPacket.operation == eTupleOperation.READ || 
               tPacket.operation == eTupleOperation.IN) {
           
           if(!tPacket.blocking) {               
               boolean result = false;
               
               if(tPacket.operation == eTupleOperation.READ)
                   result = tupleSpace.read_nb(tPacket.tuple);
               else
                   result = tupleSpace.in_nb(tPacket.tuple);
               
               // #TODOVB
               // here we can use the same channel or use standard NIOSender
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
                                            
               sender.write(tPacket);            
           } else {              
               // #TODOVB
               // here is the simple version of waiting                         
               
               if(tPacket.operation == eTupleOperation.READ)
                   tupleSpace.read(tPacket.tuple);
               else if (tPacket.operation == eTupleOperation.IN)
                   tupleSpace.in(tPacket.tuple);

               //tPacket.tuple = result;
               tPacket.operation = eTupleOperation.TUPLEBACK;
               
               // write a response
               NIOSender sender = tcpnioEntity.getSender(tPacket.lastSenderIPAddress);
               sender.write(tPacket);                       
           }
       }
       else if(tPacket.operation == eTupleOperation.TUPLEABSENT || 
               tPacket.operation == eTupleOperation.TUPLEBACK ||
               tPacket.operation == eTupleOperation.OPERATION_COMPLETED) {
           
           // just find a request and insert result packet   
           notifyAboutNewPacket(tcpnioEntity, tPacket);
           
       } else if(tPacket.operation == eTupleOperation.DELETE) {
           // to delete a tuple
           tupleSpace.in_nb(tPacket.tuple);
           
           // send notification
           TuplePack notPacket = new TuplePack(eTupleOperation.OPERATION_COMPLETED, tPacket.operationID, tcpnioEntity.ipAddress);
           NIOSender sender = new NIOSender(tPacket.lastSenderIPAddress);
           sender.write(notPacket);
           
       } else if(tPacket.operation == eTupleOperation.READ_REPL || tPacket.operation == eTupleOperation.IN_REPL) {
           
           boolean result = doReadOrIN(tupleSpace, tPacket.tuple, tPacket.blocking, (tPacket.operation == eTupleOperation.IN_REPL));
                                          
           NIOSender sender = tcpnioEntity.getSender(tPacket.lastSenderIPAddress);
            
           if(result)
           {
               if (!(tPacket.tuple instanceof RepliTuple))
                   System.err.println("tPacket.tuple ! instanceof RepliTuple!!");

               // check the owner
               String ownerAddressStr = ((RepliTuple)tPacket.tuple).getOwner();
                
               // if the owner of the found tuple is this node - send back the found tuple
               String localIPAddressStr = tcpnioEntity.ipAddress.returnFullAddress();
               if (ownerAddressStr.equals(localIPAddressStr)) {
                   tPacket.tuple = tPacket.tuple;
                   tPacket.operation = eTupleOperation.TUPLEBACK;
                   sender.write(tPacket);    
                   return;
               } else {
                   
                   // ask owner of the tuple
                   TupleSpaceRepliOperations.askOwner((RepliTuple)tPacket.tuple, tPacket.blocking, ownerAddressStr, tPacket.lastSenderIPAddress, tcpnioEntity);
                                         
                   // this processing is finished, the listener waits for the response
                   return;   
               }     
           } else {
               // just send that the tuple is absent
               tPacket.operation = eTupleOperation.TUPLEABSENT;
               sender.write(tPacket);    
               return;
           }                                          
       
       } else if(tPacket.operation == eTupleOperation.ASK_READ_OWNER ||
               tPacket.operation == eTupleOperation.ASK_IN_OWNER) {
           
           boolean result = false;           
           result = doReadOrIN(tupleSpace, tPacket.tuple, tPacket.blocking, (tPacket.operation == eTupleOperation.ASK_IN_OWNER));
                               
           NIOSender sender = tcpnioEntity.getSender(tPacket.lastSenderIPAddress);
           if(result)
           {
               if (!(tPacket.tuple instanceof RepliTuple))
                   System.err.println("tPacket.tuple ! instanceof RepliTuple!!");

               // check the owner
               String ownerAddressStr = ((RepliTuple)tPacket.tuple).getOwner();
               
               // if the owner of the found tuple is this node - send back the found tuple
               if (ownerAddressStr.equals(tcpnioEntity.ipAddress.returnFullAddress())) {
                   tPacket.tuple = tPacket.tuple;
                   tPacket.operation = eTupleOperation.TUPLEBACK;
                   sender.write(tPacket);    

                   // if it is a withdrawing operation
                   if(tPacket.operation == eTupleOperation.ASK_IN_OWNER) {
                       List<String> localitiesWithReplica = ((RepliTuple)tPacket.tuple).getLocationList();
                       sendTupleToRemove(tcpnioEntity, tPacket.tuple, localitiesWithReplica);
                   }
               } else {

                   System.err.print("Here is a mistake: this node should be the owner");
                   return;
               }     
           } else {
               // just send that the tuple is absent
               tPacket.operation = eTupleOperation.TUPLEABSENT;
               sender.write(tPacket);    
               return;
           }                                          
       } else {                 
           
           if(tPacket.operation == eTupleOperation.READ)
               tupleSpace.read(tPacket.tuple);
           else if (tPacket.operation == eTupleOperation.IN)
               tupleSpace.in(tPacket.tuple);

           tPacket.operation = eTupleOperation.TUPLEBACK;
           
           // write a response
           NIOSender sender = tcpnioEntity.getSender(tPacket.lastSenderIPAddress);
           sender.write(tPacket);                       
       }
    }

    public static boolean doReadOrIN(TupleSpace tupleSpace, Tuple template, boolean isBlocking, boolean isINOperation)
            throws InterruptedException {
        boolean result;
        if(isINOperation)
           {
               if(isBlocking)    
                   result = tupleSpace.in(template); 
               else 
                   result = tupleSpace.in_nb(template); 
           } else {
               if(isBlocking)
                   result = tupleSpace.read(template); 
               else
                   result = tupleSpace.read_nb(template); 
           }
        return result;
    }

    public static void notifyAboutNewPacket(TCPNIOEntity tcpnioEntity, TuplePack tPacket) {
        // put packet and signal about the arrival of a new packet
        tcpnioEntity.dataPairLock.lock();
        if(tcpnioEntity.responseMap.containsKey(tPacket.operationID)) {
            tcpnioEntity.responseMap.put(tPacket.operationID, new NullableTuplePack(tPacket));
           tcpnioEntity.responseIsBack.signal();
        }          
        tcpnioEntity.dataPairLock.unlock();
    }
    
    private static boolean sendTupleToRemove(TCPNIOEntity tcpnioEntity, Tuple template, List<String> ipAddressList) throws InterruptedException
    {
        boolean result = false;
        
        HashSet<Long> removalOperations = new HashSet<>();
        for(int i=0; i<ipAddressList.size(); i++)
        {
            TuplePack deletePacket = new TuplePack(eTupleOperation.DELETE, template, false, 0);
            deletePacket.operationID = tcpnioEntity.getNextOperationSequenceID();
            deletePacket.lastSenderIPAddress = tcpnioEntity.ipAddress;
            
            NIOSender nioSender = tcpnioEntity.getSender(IPAddress.parseAddress(ipAddressList.get(i)));
            
            // here there is a problem - next set of two operations is not an atomic operation #TODO
            removalOperations.add(deletePacket.operationID);
            TupleSpaceInteractionWithReplication.setSynchObject(tcpnioEntity, deletePacket.operationID);
            nioSender.write(deletePacket);       
            
        }
        // receive all notifications
        while(!removalOperations.isEmpty())
        {
            tcpnioEntity.dataPairLock.lock();
            tcpnioEntity.responseIsBack.await();     
            
            Iterator<Long> iter = removalOperations.iterator();
            while(iter.hasNext()) {
                long operationID = iter.next();
                if(tcpnioEntity.responseMap.get(operationID) != null) {
                    tcpnioEntity.responseMap.remove(operationID);
                    removalOperations.remove(operationID);
                }
            }
            
            tcpnioEntity.dataPairLock.unlock();
        }
        return result;
    }
    
}
