package repliklaim;
import java.util.UUID;

import klaim.localspace.TupleSpaceList;
import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;
import klava.topology.KlavaNode;
import klava.topology.KlavaNode.eReplicationType;

public class Sensor extends Thread {

	public String nodeAddress = null;
	// klava node
	public KlavaNode node = null;
	// unique identifier
	public String identifier = UUID.randomUUID().toString();
	
	// constructor
	public Sensor(String name, String address) throws KlavaMalformedPhyLocalityException {
		this.identifier = name;
		this.nodeAddress = address;
		PhysicalLocality physLoc = new PhysicalLocality(address);
		this.node = new KlavaNode(physLoc, TupleSpaceList.class, eReplicationType.REPLIKLAIM_REPLICATION);
		
		System.out.println("Process " + identifier + " started");
	}
}
