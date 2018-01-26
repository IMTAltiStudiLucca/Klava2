/*
 * Created on 26Apr.,2017
 */
package klava.new_communication;

import java.util.ArrayList;
import java.util.List;

import auxiliary.LocalityTranslation;
import klaim.localspace.SeparableTupleSpace;
import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.PhysicalLocality;
import klava.TupleSpace;
import klava.new_communication.TuplePack.eTupleOperation;
import klava.new_communication.TupleSpaceOperations.eOperationTypes;
import klava.replication.RepliTuple;
import network.algorithms.BetweennessStrategy;
import network.algorithms.GraphProcessing;
import network.algorithms.IReplicationStrategy;
import network.structures.GraphData;

public class TupleSpaceRepliOperations 
{
    //public enum eOperationTypes {ORDINARY, WITH_REPLICATION}
    
    eOperationTypes type;
    TupleSpace tupleSpace;
    boolean newCommunicationPart;
    PhysicalLocality nodePhysicalLocality;
    TCPNIOEntity tcpnioEntity;
    IReplicationStrategy replicationStrategy = new BetweennessStrategy();
    
    // TODO - factory class??
    public TupleSpaceRepliOperations(eOperationTypes type, TupleSpace tupleSpace, boolean newCommunicationPart, TCPNIOEntity tcpnioEntity, PhysicalLocality nodePhysicalLocality)
    {
        this.type = type;
        this.tupleSpace = tupleSpace;
        this.newCommunicationPart = newCommunicationPart;
        this.nodePhysicalLocality = nodePhysicalLocality;
        this.tcpnioEntity = tcpnioEntity;
    }
    
    public void out(RepliTuple tuple, List<Locality> localitiesToShare) throws KlavaException
    {
        int numberOfReplics = localitiesToShare.size() > 1 ? (int)(Math.log(localitiesToShare.size() / Math.log(2.))) : 1;
        GraphData graphData = GraphData.loadGraph("data\\all_pairs_hops_sf100.txt", "data\\all_pairs_distance_sf100.txt");
        
        List<Integer> nodeIDs = TupleSpaceRepliOperations.localitiesToNodeIDs(localitiesToShare);
        ArrayList<Integer> replicationNodes = replicationStrategy.getReplicaNodes(graphData, nodeIDs, numberOfReplics);

        // get the list of the replication nodes
        List<Locality> replicationLocalities = TupleSpaceRepliOperations.nodeIDsToLocalities(replicationNodes);
        
        // set owner
        Integer ownerNodeID = replicationStrategy.getOwnerNode(graphData, replicationNodes); //choose owner, maybe center of the network of replications nodes

        Locality ownerLocality = nodeIDToLocality(ownerNodeID);
        tuple.setOwner(ownerLocality.toString());
        
        int tiedLocalitiesCode = TupleSpaceRepliOperations.getTiedLocationsCode(localitiesToShare);
        tuple.setTiedLocalityGroup(String.valueOf(tiedLocalitiesCode));
        
        switch(tuple.getConsistencyLevel()) {
            case WEAK:
                sendTupleInParallel(tuple, replicationLocalities, false, nodePhysicalLocality, tcpnioEntity);
                break;
            case STRONG:
                sendTupleInParallel(tuple, replicationLocalities, true, nodePhysicalLocality, tcpnioEntity);
                break;
        }
    }
    
    public boolean getData(RepliTuple template, boolean blocking, boolean remove, List<Locality> localitiesToShare, PhysicalLocality currentLocality) throws KlavaException, InterruptedException
    {
        int numberOfReplics = localitiesToShare.size() > 1 ? (int)(Math.log(localitiesToShare.size() / Math.log(2.))) : 1;
        GraphData graphData = GraphData.loadGraph("data\\all_pairs_hops_sf100.txt", "data\\all_pairs_distance_sf100.txt");
        
        List<Integer> nodeIDs = TupleSpaceRepliOperations.localitiesToNodeIDs(localitiesToShare);
        ArrayList<Integer> replicationNodes = replicationStrategy.getReplicaNodes(graphData, nodeIDs, numberOfReplics);
        
        
        int currentNodeID = localityToNodeID(currentLocality);
        int closestReplicationNodeID = GraphProcessing.getClosestReplicaToNode(graphData, replicationNodes, currentNodeID);
        
        // get the closest replication node
        Locality closestReplicationNode = nodeIDToLocality(closestReplicationNodeID);
        
        int tiedLocalitiesCode = TupleSpaceRepliOperations.getTiedLocationsCode(localitiesToShare);
        template.setTiedLocalityGroup(String.valueOf(tiedLocalitiesCode));
        
        RepliTuple foundTuple = getTuple(template, blocking, remove, closestReplicationNode, currentLocality, tcpnioEntity);
        return (foundTuple != null);
    }
    
    
    public boolean getDataLocally(RepliTuple template, boolean blocking, boolean remove) throws KlavaException, InterruptedException
    {          
        // read locally
        template.setTiedLocalityGroup(SeparableTupleSpace.ALL);
        boolean result = getTupleLocally(template, blocking, remove, tcpnioEntity);
        return result;
    }
    
    protected static void sendTupleInParallel(RepliTuple tuple, List<Locality> destinationList, boolean waitForAccomplishment, 
    		PhysicalLocality currentLocality, TCPNIOEntity tcpnioEntity) throws KlavaException
    {
        // create a ip-list of replication nodes
        List<String> destIPs = new ArrayList<>();
        for(int i=0; i<destinationList.size(); i++)
            destIPs.add(destinationList.get(i).toString());
        
        for(int i=0; i<destinationList.size(); i++)
        {
            Locality destination = destinationList.get(i);
            if(tuple.getOwner().equals(destination.toString()))
                tuple.setLocationList(destIPs);
            
            if (!TupleSpaceOperations.checkLocalDestination(destination, currentLocality))
            {
                PhysicalLocality desPhyLoc = new PhysicalLocality(destination.toString()); 
                TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(currentLocality, desPhyLoc, tcpnioEntity);
                try {
                    boolean result = interaction.tupleOperation(eTupleOperation.OUT_REPL, tuple, false, 0);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
            } else
            	tcpnioEntity.tupleSpace.out(tuple);
        }
    }
    
    protected static RepliTuple getTuple(RepliTuple template, boolean blocking, boolean remove, Locality replicationNode, PhysicalLocality localLocality, TCPNIOEntity tcpnioEntity) throws KlavaException, InterruptedException
    {
        boolean result = false;
        if (!TupleSpaceOperations.checkLocalDestination(replicationNode, localLocality))
        {
            PhysicalLocality desPhyLoc = new PhysicalLocality(replicationNode.toString()); 
            TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(localLocality, desPhyLoc, tcpnioEntity);
            if (remove)
                result = interaction.tupleOperation(eTupleOperation.IN_REPL, template, blocking, 0);
            else
                result = interaction.tupleOperation(eTupleOperation.READ_REPL, template, blocking, 0);
        } else {
            // get locally
            result = getTupleLocally(template, blocking, remove, tcpnioEntity);
        }
        if (result)
            return template;

        return null;
    }
    
    protected static boolean getTupleLocally(RepliTuple template, boolean blocking, boolean remove, TCPNIOEntity tcpnioEntity) throws InterruptedException
    {
        boolean result = ProcessTuplePackRE.doReadOrIN(tcpnioEntity.tupleSpace, template, blocking, (remove == true));
        
        if(result && template != null) {    
            String ownerAddressStr = ((RepliTuple)template).getOwner();
            
            // only for the withdrawing operation
            if (remove && ownerAddressStr.equals(tcpnioEntity.ipAddress.returnFullAddress()))
            	TupleSpaceRepliOperations.askOwner(template, blocking, ownerAddressStr, tcpnioEntity.ipAddress, tcpnioEntity);
        }
        
        return result;
    }
/*    
    private RepliTuple getTuple(RepliTuple template, boolean blocking, boolean remove, Locality replicationNode) throws KlavaException, InterruptedException
    {
        boolean result = false;
        if (!TupleSpaceOperations.checkLocalDestination(replicationNode, nodePhysicalLocality))
        {
            PhysicalLocality desPhyLoc = new PhysicalLocality(replicationNode.toString()); 
            TupleSpaceInteractionWithReplication interaction = new TupleSpaceInteractionWithReplication(nodePhysicalLocality, desPhyLoc, tcpnioEntity);
            if (remove)
                result = interaction.tupleOperation(eTupleOperation.IN_REPL, template, blocking, 0);
            else
                result = interaction.tupleOperation(eTupleOperation.READ_REPL, template, blocking, 0);
        } else {
            // get locally
            result = getTupleLocally(template, blocking, remove);
        }
        if (result)
            return template;

        return null;
    }
    
    private boolean getTupleLocally(RepliTuple template, boolean blocking, boolean remove) throws InterruptedException
    {
        boolean result = ProcessTuplePackRE.doReadOrIN(tupleSpace, template, blocking, (remove == true));
        
        if(result && template != null)
        {    
            String ownerAddressStr = ((RepliTuple)template).getOwner();
            
            // only for the withdrawing operation
            if (remove && ownerAddressStr.equals(tcpnioEntity.ipAddress.returnFullAddress()))
            	TupleSpaceRepliOperations.askOwner(template, blocking, ownerAddressStr, tcpnioEntity.ipAddress, tcpnioEntity);
        }
        
        return result;
    }*/

    public static void askOwner(RepliTuple tupleToCheck, boolean blocking, String ownerAddressStr, IPAddress ipAddressToReturnResult,
            TCPNIOEntity tcpnioEntity) {

        TuplePack askOwnerPack = new TuplePack(eTupleOperation.ASK_IN_OWNER, tupleToCheck, blocking, 0);  
        IPAddress ownerIPAddress = IPAddress.parseAddress(ownerAddressStr);
         
        // set the address where to send a result
        askOwnerPack.lastSenderIPAddress = ipAddressToReturnResult;
        NIOSender senderForOwner = tcpnioEntity.getSender(ownerIPAddress);
        senderForOwner.write(askOwnerPack);
    }
       
    private static Integer localityToNodeID(Locality locality)
    {
        LocalityTranslation translator = LocalityTranslation.getInstance();
        int nodeID = translator.getNodeID(locality.toString());
        return nodeID;
    }
    
    private static Locality nodeIDToLocality(Integer nodeID)
    {
        LocalityTranslation translator = LocalityTranslation.getInstance();
        
        String locAddress = translator.getIP(nodeID);
        try {
            Locality locality = (new PhysicalLocality(locAddress));
            return locality;
        } catch (KlavaMalformedPhyLocalityException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static List<Integer> localitiesToNodeIDs(List<Locality> localities)
    {       
        // get the list of the replication nodes
        List<Integer> nodeIDs = new ArrayList<Integer>();
        for(int i=0; i<localities.size(); i++) {
            int nodeID = localityToNodeID(localities.get(i));
            nodeIDs.add(nodeID);
        }
        return nodeIDs;
    }
    
    private static List<Locality> nodeIDsToLocalities(ArrayList<Integer> nodeIDs)
    {
        LocalityTranslation translator = LocalityTranslation.getInstance();
        
        // get the list of the replication nodes
        List<Locality> localities = new ArrayList<Locality>();
        for(int i=0; i<nodeIDs.size(); i++) {
            Locality locality = nodeIDToLocality(nodeIDs.get(i));
            localities.add(locality);
        }
        return localities;
    }

    protected static int getTiedLocationsCode(List<Locality> localities)
    {
        StringBuffer buf = new StringBuffer(localities.size());
        for(int i=0; i<localities.size(); i++)
            buf.append(localities.get(i).toString());
        return buf.toString().hashCode();
    }
}
