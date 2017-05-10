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
        
        // getSessionId().port is not common
        tPack.senderPort = sender.getSessionId().port;             
        NIOSender nioSender = tcpnioEntity.getSender(receiver.getSessionId().port);
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
            tcpnioEntity.dataPairLock.lock();
            if(tcpnioEntity.pair != null)
                System.err.println("tupleOperation - mistake:tcpnioEntity.pair != null");
              
            tcpnioEntity.pair = new CustomPair<Long, TuplePack>(nextOperationID, null);
            tcpnioEntity.dataPairLock.unlock();
            
            // send the packet with remote operation
            nioSender.write(tPack);

            while (true)
            {
                tcpnioEntity.dataPairLock.lock();

                tcpnioEntity.responseIsBack.await();
                
           
                if(!tcpnioEntity.pair.getKey().equals(nextOperationID))
                    System.err.println("Mistake in nextOperationID");
                    
                    TuplePack resultPack = tcpnioEntity.pair.getValue();

                    if(resultPack != null)
                    {
                        if(!blocking)
                        {   
 //                           System.out.println(resultPack.senderPort + ":TupleSpaceInteraction:got_from_resptable");
                    }
                    
                    if(resultPack.operation == eTupleOperation.TUPLEABSENT)
                        operationResult = false;
                    else  if(resultPack.operation == eTupleOperation.TUPLEBACK)
                    {
                        tuple.copy_tuple(resultPack.tuple);
                        operationResult = true;
                    } else {
                        System.err.println("TupleSpaceInteraction: unknown state");
                    }
                    
                    tcpnioEntity.pair = null;
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
