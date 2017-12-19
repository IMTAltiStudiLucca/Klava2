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

public class TupleSpaceRepliKlaimOperations 
{    
    eOperationTypes type;
    TupleSpace tupleSpace;
    PhysicalLocality nodePhysicalLocality;
    TCPNIOEntity tcpnioEntity;
    IReplicationStrategy replicationStrategy = new BetweennessStrategy();
    
    // TODO - factory class??
    public TupleSpaceRepliKlaimOperations(eOperationTypes type, TupleSpace tupleSpace, TCPNIOEntity tcpnioEntity, PhysicalLocality nodePhysicalLocality)
    {
        this.type = type;
        this.tupleSpace = tupleSpace;
        this.nodePhysicalLocality = nodePhysicalLocality;
        this.tcpnioEntity = tcpnioEntity;
    }
    
    /**
     * perform a writing operation
     * @param tuple
     * @param replicationLocalities
     * @throws KlavaException
     */
    public void out(RepliTuple tuple, List<Locality> replicationLocalities) throws KlavaException
    {
    	// the owner is just a first node in the list
        Locality ownerLocality = replicationLocalities.get(0);
        tuple.setOwner(ownerLocality.toString());
        
        int tiedLocalitiesCode = TupleSpaceRepliOperations.getTiedLocationsCode(replicationLocalities);
        tuple.setTiedLocalityGroup(String.valueOf(tiedLocalitiesCode));
        
        switch(tuple.getConsistencyLevel()) {
            case WEAK:
            	TupleSpaceRepliOperations.sendTupleInParallel(tuple, replicationLocalities, false, nodePhysicalLocality, tcpnioEntity);
                break;
            case STRONG:
            	TupleSpaceRepliOperations.sendTupleInParallel(tuple, replicationLocalities, true, nodePhysicalLocality, tcpnioEntity);
                break;
        }
    }
    
    /**
     * perform a querying operation: for read- and in-operations
     * @param template
     * @param blocking
     * @param remove
     * @param replicaLocality
     * @param localLocality
     * @return
     * @throws KlavaException
     * @throws InterruptedException
     */
    public boolean getData(RepliTuple template, boolean blocking, boolean remove, Locality replicaLocality, PhysicalLocality localLocality) throws KlavaException, InterruptedException
    {        
        RepliTuple foundTuple = TupleSpaceRepliOperations.getTuple(template, blocking, remove, replicaLocality, localLocality, tcpnioEntity);
        return (foundTuple != null);
    }
    
    /**
     * perform a local operation
     * @param template
     * @param blocking
     * @param remove
     * @return
     * @throws KlavaException
     * @throws InterruptedException
     */
    public boolean getDataLocally(RepliTuple template, boolean blocking, boolean remove) throws KlavaException, InterruptedException
    {          
        // read locally
        template.setTiedLocalityGroup(SeparableTupleSpace.ALL);
        boolean result = TupleSpaceRepliOperations.getTupleLocally(template, blocking, remove, tcpnioEntity);
        return result;
    }
    

}
