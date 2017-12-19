package test;

import java.util.ArrayList;
import java.util.List;

import klaim.localspace.TupleSpaceList;
import klava.KlavaException;
import klava.Locality;
import klava.PhysicalLocality;
import klava.replication.RepliTuple;
import klava.replication.eConsistencyLevel;
import klava.topology.KlavaNode;
import klava.topology.KlavaNode.eReplicationType;

public class RepliKlaimTest {
    public static void main(String[] args) throws KlavaException, InterruptedException {
        List<KlavaNode> nodes = new ArrayList<KlavaNode>();

        List<Locality> localities = new ArrayList<Locality>();
        PhysicalLocality l1 = new PhysicalLocality("127.0.0.1", 8001);
        PhysicalLocality l2 = new PhysicalLocality("127.0.0.1", 8002);
        PhysicalLocality l3 = new PhysicalLocality("127.0.0.1", 8003);

        localities.add(l1);
        localities.add(l2);
        localities.add(l3);
        
        for(int i=0; i<localities.size(); i++)
        	nodes.add(new KlavaNode((PhysicalLocality)localities.get(i), TupleSpaceList.class, eReplicationType.REPLIKLAIM_REPLICATION));   

        //wait until all nodes is initialized
        Thread.sleep(1000);
       
        RepliTuple rt = new RepliTuple(new Object[]{"address", "street A"});
        rt.setConsistencyLevel(eConsistencyLevel.WEAK);
        nodes.get(0).outRK(rt, localities);
        
        // read a tuple
        RepliTuple template = new RepliTuple(new Object[]{"address", String.class});
        nodes.get(1).readRK(template);
        // print the resulting tuple
        System.out.println(template);
        
        
        // read a tuple
        template = new RepliTuple(new Object[]{"address", String.class});
        nodes.get(2).inRK(template);
        // print the resulting tuple
        System.out.println(template);
        
        // non-blocking operation
        template = new RepliTuple(new Object[]{"address", String.class});
        nodes.get(2).in_nbRK(template);
        System.out.println(template);
        
        
        Thread.sleep(1000);
        for(int i=0; i<nodes.size(); i++)
        	nodes.get(i).stopNode();
        System.out.print("Program finished");
    }
}