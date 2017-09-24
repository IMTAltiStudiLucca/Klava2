package network.algorithms;

import java.util.ArrayList;
import java.util.List;

import network.structures.GraphData;

public interface IReplicationStrategy 
{
	public ArrayList<Integer> getReplicaNodes(GraphData graphData, List<Integer> nodes, int topVerticesNumber);
	
	public Integer getOwnerNode(GraphData graphData, ArrayList<Integer> replicaNodes);
}
