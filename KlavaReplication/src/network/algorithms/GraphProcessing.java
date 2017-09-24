package network.algorithms;

import java.util.ArrayList;

import network.structures.GraphData;
import network.structures.Pair;

public class GraphProcessing 
{

	/***
	 * get the closest replica for the current node
	 * @param graphData
	 * @param replicaNodes
	 * @param currentNode
	 * @return
	 */
	public static Integer getClosestReplicaToNode(GraphData graphData, ArrayList<Integer> replicaNodes, Integer currentNode)
	{	
		if(replicaNodes.size() == 0)
			return null;
		
		// find the replica with minimum distance from the currentNode
		Pair<Integer, Integer> minDistanceReplica = null;
		for(int i=0; i<replicaNodes.size(); i++)
		{
			Integer relipcaNodeID = replicaNodes.get(i);
			Pair<Integer, Integer> edge = currentNode < relipcaNodeID ? 
					new Pair<Integer, Integer>(currentNode, relipcaNodeID) : new Pair<Integer, Integer>(relipcaNodeID, currentNode);
			Integer weight = graphData.getWeightTable().get(edge);
			if(minDistanceReplica == null || minDistanceReplica.getRight() < weight)
				minDistanceReplica = new Pair<Integer, Integer>(relipcaNodeID, weight);			
		}
		
		// return node id
		return minDistanceReplica.getLeft();
	}
	
	
//	public static Integer getOwnerNode(GraphData graphData, ArrayList<Integer> replicaNodes)
//	{		
//		if (replicaNodes.size() == 1)
//			return replicaNodes.get(0);
//		else 
//		{
//			ArrayList<Integer> mostImportantNodes = BetweennessStrategy.getMostImportantVertices(graphData, replicaNodes, 1);
//			return mostImportantNodes.get(0);
//		}
//	}
}
