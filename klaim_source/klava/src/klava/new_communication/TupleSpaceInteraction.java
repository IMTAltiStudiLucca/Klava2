/*
 * Created on 23 Aug 2016
 */
package klava.new_communication;

import common.CustomPair;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.new_communication.TuplePack.eTupleOperation;

public class TupleSpaceInteraction 
{
    PhysicalLocality sender;
    PhysicalLocality receiver;
    TCPNIOEntity tcpnioEntity;
    
        
    
    public TupleSpaceInteraction(PhysicalLocality sender, PhysicalLocality receiver, TCPNIOEntity tcpnioEntity)
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
        tPack.lastSenderIPAddress = new IPAddress(sender.getSessionId().ip, sender.getSessionId().port);    
        //tPack.senderPort = sender.getSessionId().port;             
        //NIOSender nioSender = tcpnioEntity.getSender(receiver.getSessionId().port);
        
        NIOSender nioSender = tcpnioEntity.getSender(new IPAddress(receiver.getSessionId().ip, receiver.getSessionId().port));
        
        if(operation == eTupleOperation.OUT)
        {
            nioSender.write(tPack);
            operationResult = true;
            
        } else if(operation == eTupleOperation.READ || 
                operation == eTupleOperation.IN)
        {
            double startTime = System.nanoTime()/1000000d;
            
            
            long nextOperationID = tcpnioEntity.getNextOperationSequenceID();
            tPack.operationID = nextOperationID;
            
            // it is necessary to wait for the response
            TupleSpaceInteractionWithReplication.setSynchObject(tcpnioEntity, nextOperationID);

            // send the packet with remote operation
            nioSender.write(tPack);

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
        } 
        return operationResult;        
    }  
}
