package network.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import network.structures.GraphData;
import network.structures.Pair;



public class BetweennessStrategy implements IReplicationStrategy {


	public static void main(String[] args) 
	{	
		GraphData graphData = GraphData.loadGraph("all_pairs_hops_sf100.txt", "all_pairs_distance_sf100.txt");
		System.out.println("graph is loaded");
		
		ArrayList<Integer> nodes = new ArrayList<>(Arrays.asList(88, 50, 53));
		nodes.clear();
		for(int i=0; i<100; i++)
		{
			nodes.add(i);
		}

		
		//Graph<Integer, String> subgraph = graphData.getSubgraph(nodes);
		BetweennessStrategy replicationStraategy = new BetweennessStrategy();
		ArrayList<Integer> topVertices = replicationStraategy.getReplicaNodes(graphData, nodes, 10);
		System.out.println(topVertices); 
	
	
//		LocalityTranslation locTrans = LocalityTranslation.getInstance();
//		System.out.println("finished");
	}
	
	@Override
	public ArrayList<Integer> getReplicaNodes(GraphData graphData, List<Integer> nodes, int topVerticesNumber)
	{
		Graph<Integer, String> subgraph = graphData.getSubgraph(nodes);
		
		Function<String, Integer> edgeWeightFunc = new com.google.common.base.Function<String, Integer>()
    	{
			public Integer apply(String arg0) { return graphData.getWeightTable().get(arg0); }
    	};
    	
    	// compute betweenness for all vertices
    	BetweennessCentrality<Integer, String> bc = 
    			new BetweennessCentrality<Integer, String>(subgraph, edgeWeightFunc);
    	
    	// get topVerticesNumber top scores
    	ScoreComparator copm = new ScoreComparator();
    	ArrayList<Pair<Integer, Double>> verticesWithTopScores = new ArrayList<>(topVerticesNumber+1);
		Iterator<Integer> vertexIter = subgraph.getVertices().iterator(); //graphData.getVertexSet().iterator();
		while (vertexIter.hasNext())
		{
			Integer vertexID = vertexIter.next();
			Double vertexScore = bc.getVertexScore(vertexID);
			
			verticesWithTopScores.add(new Pair<Integer, Double>(vertexID, vertexScore));
			if(verticesWithTopScores.size() > topVerticesNumber) 
			{
				verticesWithTopScores.sort(copm);
				verticesWithTopScores.remove(topVerticesNumber);
			}
		}
		
		verticesWithTopScores.sort(copm);
		
		// return just indexes
		ArrayList<Integer> topVertices = new ArrayList<>();
		for(int i=0; i<verticesWithTopScores.size(); i++)
		{
			topVertices.add(verticesWithTopScores.get(i).getLeft());
		}
		return topVertices;		
	}
	
	
//	public static ArrayList<Integer> getMostImportantVerticesOld(GraphData graphData, int topVerticesNumber)
//	{
//    	Function<Integer, Integer> edgeWeightFunc = new Function<Integer, Integer>()
//    	{
//			public Integer apply(Integer arg0) { return graphData.getEdgesAndWeights().get(arg0); }
//    	};
//    	
//    	// compute betweenness for all vertices
//    	BetweennessCentrality<Integer, Integer> bc = 
//    			new BetweennessCentrality<Integer, Integer>(graphData.getGraph(), edgeWeightFunc);
//    	
//    	// get topVerticesNumber top scores
//    	ScoreComparator copm = new ScoreComparator();
//    	ArrayList<Pair<Integer, Double>> verticesWithTopScores = new ArrayList<>(topVerticesNumber+1);
//		Iterator<Integer> vertexIter = graphData.getVertexSet().iterator();
//		while (vertexIter.hasNext())
//		{
//			Integer vertexID = vertexIter.next();
//			Double vertexScore = bc.getVertexScore(vertexID);
//			
//			verticesWithTopScores.add(new Pair<Integer, Double>(vertexID, vertexScore));
//			if(verticesWithTopScores.size() > topVerticesNumber) 
//			{
//				verticesWithTopScores.sort(copm);
//				verticesWithTopScores.remove(topVerticesNumber);
//			}
//		}
//		
//		// return just indexes
//		ArrayList<Integer> topVertices = new ArrayList<>();
//		for(int i=0; i<verticesWithTopScores.size(); i++)
//		{
//			topVertices.add(verticesWithTopScores.get(i).getLeft());
//		}
//		return topVertices;		
//	}
	
	static class ScoreComparator implements Comparator<Pair<Integer, Double>>
	 {
	     public int compare(Pair<Integer, Double> p1, Pair<Integer, Double> p2)
	     {
	         return (-1)*p1.getRight().compareTo(p2.getRight());
	     }
	 }
	
	public static void test(String edgesFileName, String weightsFileName)
	{
	
    	//Graph<Integer, Integer> graph = new UndirectedSparseGraph<Integer, Integer>();
    	
//    	for(int i=0; i<5; i++) 
//    		graph.addVertex(i);
//
//    	int edge = 0;
//    	graph.addEdge(edge++, 0,1);
//    	graph.addEdge(edge++, 2,3);
//    	graph.addEdge(edge++, 3,1);
//    	graph.addEdge(edge++, 1,4);
//
//    	final int weights[] = {1, 1, 1, 1, 1};
//    	
//    	Function<Character, Integer> edge_weights = new Function<Character, Integer>()
//    	{
//			public Integer apply(Character arg0) { return weights[arg0 - 'a']; }
//    	};
//    	
//    	BetweennessCentrality<Integer,Character> bc = 
//    		new BetweennessCentrality<Integer,Character>(graph, edge_weights);	
	}

	@Override
	public Integer getOwnerNode(GraphData graphData, ArrayList<Integer> replicaNodes) {	
		if (replicaNodes.size() == 1)
			return replicaNodes.get(0);
		else 
		{
			ArrayList<Integer> mostImportantNodes = getReplicaNodes(graphData, replicaNodes, 1);
			return mostImportantNodes.get(0);
		}
	}



	

}
