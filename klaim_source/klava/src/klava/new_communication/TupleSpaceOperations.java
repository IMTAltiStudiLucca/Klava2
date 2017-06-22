/*
 * Created on 26Apr.,2017
 */
package klava.new_communication;

import klava.KlavaException;
import klava.Locality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleSpace;
import klava.new_communication.TuplePack.eTupleOperation;

public class TupleSpaceOperations 
{
    public enum eOperationTypes {ORDINARY, WITH_REPLICATION}
    
    eOperationTypes type;
    TupleSpace tupleSpace;
    boolean newCommunicationPart;
    PhysicalLocality nodePhysicalLocality;
    TCPNIOEntity tcpnioEntity;
    
    // TODO - factory class??
    public TupleSpaceOperations(eOperationTypes type, TupleSpace tupleSpace, boolean newCommunicationPart, TCPNIOEntity tcpnioEntity, PhysicalLocality nodePhysicalLocality)
    {
        this.type = type;
        this.tupleSpace = tupleSpace;
        this.newCommunicationPart = newCommunicationPart;
        this.nodePhysicalLocality = nodePhysicalLocality;
        this.tcpnioEntity = tcpnioEntity;
    }
    
    
    public void out(Tuple tuple) {
        tupleSpace.out(tuple);
    }
    
    /**
      * Performs an OUT operation of the specified Tuple at the destination
      * locality.
      * 
      * @param tuple
      * @param destination
      * @throws KlavaException
      */
    public void out(Tuple tuple, Locality destination) throws KlavaException
    {
      if (checkLocalDestination(destination, nodePhysicalLocality)) {
          
          //this is a local operation
          out(tuple);
          return;
      }

      if(newCommunicationPart) {
          PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
          
          // getPhysical(self) to nodePhysicalLocality
          TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(nodePhysicalLocality, desPhyLoc, tcpnioEntity);
          try {
              interaction.tupleOperation(eTupleOperation.OUT, tuple, false, 0);
          } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
      }
    }
    
    
    public void in(Tuple tuple) throws KlavaException {
        try {
            tupleSpace.in(tuple);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        }
    }
    
    /**
     * Performs an IN operation of the specified Tuple at the destination
     * locality.
     * 
     * @param tuple
     * @param destination
     * @throws KlavaException
     */
    public void in(Tuple tuple, Locality destination) throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, nodePhysicalLocality)) {
            /* this is a local operation */
            in(tuple);
            return;
        }
            
        if(newCommunicationPart) {
            PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
            TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(nodePhysicalLocality, desPhyLoc, tcpnioEntity);
            try {
                interaction.tupleOperation(eTupleOperation.IN, tuple, true, 0);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } 
    }
    
    public boolean in_nb(Tuple tuple) {
        try {
            return tupleSpace.in_nb(tuple);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    
    
    /**
     * Performs a non-blocking IN operation of the specified Tuple at the
     * destination locality.
     * 
     * @param tuple
     * @param destination
     * @return Whether a matching tuple is found
     * @throws KlavaException
     * @throws InterruptedException 
     */
    public boolean in_nb(Tuple tuple, Locality destination)
            throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, nodePhysicalLocality)) {
            // this is a local operation       
            return in_nb(tuple);
        }
       

        PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
        TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(nodePhysicalLocality, desPhyLoc, tcpnioEntity);
        try {
            boolean operationResult = interaction.tupleOperation(eTupleOperation.IN, tuple, false, 0);
            return operationResult;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }  
       
    public void read(Tuple tuple) throws KlavaException {
        try {
            tupleSpace.read(tuple);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        }
    }
    
    public boolean read_nb(Tuple tuple) {
        try {
            return tupleSpace.read_nb(tuple);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    
    
    /**
     * Performs an READ operation of the specified Tuple at the destination
     * locality.
     * 
     * @param tuple
     * @param destination
     * @throws KlavaException
     */
    public void read(Tuple tuple, Locality destination) throws KlavaException {
        if (checkLocalDestination(destination, nodePhysicalLocality)) {
            /* this is a local operation */
            read(tuple);
            return;
        }

        PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
        TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(nodePhysicalLocality, desPhyLoc, tcpnioEntity);
        try {
            interaction.tupleOperation(eTupleOperation.READ_REPL, tuple, true, 0);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Performs a non-blocking READ operation of the specified Tuple at the
     * destination locality.
     * 
     * @param tuple
     * @param destination
     * @return Whether a matching tuple is found
     * @throws KlavaException
     */
    public boolean read_nb(Tuple tuple, Locality destination)
            throws KlavaException {
        if (checkLocalDestination(destination, nodePhysicalLocality)) {
            /* this is a local operation */
            return read_nb(tuple);
        }

        if(newCommunicationPart) {
            PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
            TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(nodePhysicalLocality, desPhyLoc, tcpnioEntity);
            try {
                boolean operationResult = interaction.tupleOperation(eTupleOperation.READ, tuple, false, 0);
                return operationResult;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }
    
    
    public static boolean checkLocalDestination(Locality destination,
            PhysicalLocality localPhyLoc) throws KlavaException {
        
        String destinationLocStr = destination.locality.toString();
        String localPhyLocStr = localPhyLoc.locality.toString();
        
        if (localPhyLocStr.equals(destinationLocStr)) {
            return true;
        }
        
   /*     if (destLocality == self)
            return true;
        
        if (destination.locality == newPhysLoc.locality) {
            return true;
        }

        // first translate a possible LogicalLocality 
        destination.setValue(getPhysical(destLocality));
        
        if (isLocal(new NodeLocation(destination.toString())))
            return true;
        */
        return false;
    }
    
       
    
//    /**
//     * Performs an OUT operation of the specified Tuple at the destination
//     * locality.
//     * 
//     * @param tuple
//     * @param destination
//     * @throws KlavaException
//     */
//    public void out(Tuple tuple, Locality destination) throws KlavaException {
//        PhysicalLocality realDestination = new PhysicalLocality();
//        if (checkLocalDestination(destination, nodePhysicalLocality)) {
//            
//            //this is a local operation
//            out(tuple);
//            return;
//        }
//
//        if(newCommunicationPart) {
//            PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
//            
//            // getPhysical(self) to nodePhysicalLocality
//            TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(nodePhysicalLocality, desPhyLoc, tcpnioEntity);
//            try {
//                interaction.tupleOperation(eTupleOperation.OUT, tuple, false, 0);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
////        else {       
////            // the response we will wait for
////            Response<String> response = new Response<String>();
////    
////            tupleOperation(TuplePacket.OUT_S, tuple, realDestination, true,
////                    waitingForOkResponse, response, -1);
////            
////            // if we're here the sending succeeded. But we still have to check the
////            // response contents  
////            if (response.error != null)
////                throw new KlavaException(response.error);
////        }
//    }
    
//  public void in(Tuple tuple, Locality destination) throws KlavaException {
//  PhysicalLocality realDestination = new PhysicalLocality();
//  if (checkLocalDestination(destination, nodePhysicalLocality)) {
//      /* this is a local operation */
//      in(tuple);
//      return;
//  }
//      
//  if(newCommunicationPart) {
//      PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
//      TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(getPhysical(self), desPhyLoc, tcpnioEntity);
//      try {
//          interaction.tupleOperation(eTupleOperation.IN, tuple, true, 0);
//      } catch (InterruptedException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      }
//  } 
//  else {
//      operationInRead(TuplePacket.IN_S, tuple, realDestination, true, -1);
//  }
//}
    
    
    
//    /**
//     * Performs a non-blocking IN operation of the specified Tuple at the
//     * destination locality.
//     * 
//     * @param tuple
//     * @param destination
//     * @return Whether a matching tuple is found
//     * @throws KlavaException
//     */
//    public boolean in_nb(Tuple tuple, Locality destination)
//            throws KlavaException {
//        PhysicalLocality realDestination = new PhysicalLocality();
//        if (checkLocalDestination(destination, nodePhysicalLocality)) {
//            /* this is a local operation */
//            
//            boolean result = in_nb(tuple);
//            //System.out.println("read_nb with result " + result + " - " + tupleSpace);
//            
//            return result;
//        }
//       
//        if(newCommunicationPart) {
//            PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
//            TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(getPhysical(self), desPhyLoc, tcpnioEntity);
//            try {
//                boolean operationResult = interaction.tupleOperation(eTupleOperation.IN, tuple, false, 0);
//                return operationResult;
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                return false;
//            }
//        } else {
//            return operationInRead(TuplePacket.IN_S, tuple, realDestination, false, -1);
//        }
//    }  
    
    

//    /**
//     * Performs an READ operation of the specified Tuple at the destination
//     * locality.
//     * 
//     * @param tuple
//     * @param destination
//     * @throws KlavaException
//     */
//    public void read(Tuple tuple, Locality destination) throws KlavaException {
//        PhysicalLocality realDestination = new PhysicalLocality();
//        if (checkLocalDestination(destination, nodePhysicalLocality)) {
//            /* this is a local operation */
//            read(tuple);
//            return;
//        }
//
//        if (newCommunicationPart) {
//            PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
//            TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(getPhysical(self), desPhyLoc, tcpnioEntity);
//            try {
//                interaction.tupleOperation(eTupleOperation.READ, tuple, true, 0);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        } else {
//            operationInRead(TuplePacket.READ_S, tuple, realDestination, true, -1);
//        }
//    }
    
//    /**
//     * Performs a non-blocking READ operation of the specified Tuple at the
//     * destination locality.
//     * 
//     * @param tuple
//     * @param destination
//     * @return Whether a matching tuple is found
//     * @throws KlavaException
//     */
//    public boolean read_nb(Tuple tuple, Locality destination)
//            throws KlavaException {
//        PhysicalLocality realDestination = new PhysicalLocality();
//        if (checkLocalDestination(destination, nodePhysicalLocality)) {
//            /* this is a local operation */
//            return read_nb(tuple);
//        }
//
//        if(newCommunicationPart) {
//            PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
//            TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(getPhysical(self), desPhyLoc, tcpnioEntity);
//            try {
//                boolean operationResult = interaction.tupleOperation(eTupleOperation.READ, tuple, false, 0);
//                return operationResult;
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                return false;
//            }
//        }
//        else {
//            return operationInRead(TuplePacket.READ_S, tuple, realDestination, false, -1);
//        }
//    }
    
    
}
