/*
 * Created on 23 Aug 2016
 */
package klava.new_communication;

import klava.PhysicalLocality;
import klava.Tuple;
import klava.new_communication.TuplePack.eTupleOperation;

public class TupleSpaceInteractionWithReplication 
{
    PhysicalLocality sender;
    PhysicalLocality receiver;
    TCPNIOEntity tcpnioEntity;
    
    public TupleSpaceInteractionWithReplication(PhysicalLocality sender, PhysicalLocality receiver, TCPNIOEntity tcpnioEntity)
    {
        this.sender = sender;
        this.receiver = receiver;
        this.tcpnioEntity = tcpnioEntity;
    }
    
    public boolean tupleOperation(TuplePack.eTupleOperation operation, Tuple tuple,
            boolean blocking, long timeout) throws InterruptedException
    {
        boolean operationResult = false;
        TuplePack tPack = new TuplePack(operation, tuple, blocking, timeout);
        
        // getSessionId().port is not common 
       
        tPack.lastSenderIPAddress = new IPAddress(sender.getSessionId().ip, sender.getSessionId().port);             
        NIOSender nioSender = tcpnioEntity.getSender(new IPAddress(receiver.getSessionId().ip, receiver.getSessionId().port));
        if(operation == eTupleOperation.OUT_REPL) {
            nioSender.write(tPack);
            operationResult = true;
        } 
        else if(operation == eTupleOperation.IN_REPL) {
            operationResult = read_inRepliOperations(tuple, blocking, true, tPack, nioSender);   
        } else if(operation == eTupleOperation.READ_REPL) {
            operationResult = read_inRepliOperations(tuple, blocking, false, tPack, nioSender);    
        }
        
//        else if(operation == eTupleOperation.OUT) {
//            nioSender.write(tPack);
//            operationResult = true;
//        } 
//        else if(operation == eTupleOperation.READ || operation == eTupleOperation.IN) {
//            operationResult = read_inOperations(tuple, blocking, tPack, nioSender);
//        }
        return operationResult;
    }

    private boolean read_inRepliOperations(Tuple tuple, boolean blocking, boolean remove, TuplePack tPack, NIOSender nioSender)
            throws InterruptedException {
        boolean operationResult = false;
                
        long nextOperationID = tcpnioEntity.getNextOperationSequenceID();
        tPack.operationID = nextOperationID;
        
        setSynchObject(tcpnioEntity, nextOperationID);
        
        // send the packet with remote operation
        nioSender.write(tPack);

        // wait for a response
        while (true)
        {
            tcpnioEntity.dataPairLock.lock();

            // wait for the response
            tcpnioEntity.responseIsBack.await();
                            
            NullableTuplePack resultPack = tcpnioEntity.responseMap.get(nextOperationID);
            if(resultPack != null)
            {                   
                if(resultPack.getPacket().operation == eTupleOperation.TUPLEABSENT)
                    operationResult = false;
                else  if(resultPack.getPacket().operation == eTupleOperation.TUPLEBACK)
                {
                    tuple.copy_tuple(resultPack.getPacket().tuple);
                    operationResult = true;
                } else {
                    System.err.println("TupleSpaceInteraction: unknown state");
                }
                
                tcpnioEntity.responseMap.remove(nextOperationID);
                break;
            } 
            else
                System.err.println("TupleSpaceInteraction: resultPack == null");
            
            tcpnioEntity.dataPairLock.unlock();                        
        }
        return operationResult;
    }
   
    private boolean read_inOperations(Tuple tuple, boolean blocking, TuplePack tPack, NIOSender nioSender)
            throws InterruptedException {
        boolean operationResult = false;
               
        long nextOperationID = tcpnioEntity.getNextOperationSequenceID();
        tPack.operationID = nextOperationID;
        
        // it is necessary to wait for the response
        setSynchObject(tcpnioEntity, nextOperationID);
        
        // send the packet with remote operation
        nioSender.write(tPack);

        while (true)
        {
            tcpnioEntity.dataPairLock.lock();
            tcpnioEntity.responseIsBack.await();              

            NullableTuplePack resultPack = tcpnioEntity.responseMap.get(nextOperationID);

            if(resultPack != null)
            {                
                if(resultPack.getPacket().operation == eTupleOperation.TUPLEABSENT)
                    operationResult = false;
                else  if(resultPack.getPacket().operation == eTupleOperation.TUPLEBACK)
                {
                    tuple.copy_tuple(resultPack.getPacket().tuple);
                    operationResult = true;
                } else {
                    System.err.println("TupleSpaceInteraction: unknown state");
                }
                
                tcpnioEntity.responseMap.remove(nextOperationID);
                break;
            } 
            else
                System.err.println("TupleSpaceInteraction: resultPack == null");

            tcpnioEntity.dataPairLock.unlock();
                         
        }
        return operationResult;
    }
    
    
    public static void setSynchObject(TCPNIOEntity tcpnioEntity, long nextOperationID) {
        // it is necessary to wait for the response
        tcpnioEntity.dataPairLock.lock();
        if(tcpnioEntity.responseMap.containsKey(nextOperationID))
            System.err.println("tupleOperation - mistake:tcpnioEntity.pair != null");
        
        tcpnioEntity.responseMap.put(nextOperationID, new NullableTuplePack());
        tcpnioEntity.dataPairLock.unlock();
    }
    
    
    
    
}
