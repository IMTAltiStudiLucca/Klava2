package network.structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GraphData {

	private String previousInitialFile = null;
	private GraphData() {

	}
	
	// Inner class initializes instance on load, won't be loaded
	// until referenced by getInstance()
	private static class GraphDataHolder {
		public static final GraphData instance = new GraphData();
	}
	// Return the singleton instance.
	public static GraphData getInstance() { return GraphDataHolder.instance; }
	
	
	
	
	// JUNG graph
	private Graph<Integer, Integer> jungGraph = null;
	
	// loaded edges and weights
	private Hashtable<Integer, Pair<Integer, Integer>> edgesSet = null;
	private Hashtable<String, Integer> weightTable = null;
	private Hashtable<Integer, Integer> edgesAndWeights = null;
	private TreeSet<Integer> vertexSet = null;
	

	public synchronized static GraphData loadGraph(String edgesFileName, String weightsFileName)
	{
		if(edgesFileName.equals(GraphData.getInstance().previousInitialFile))
			return getInstance();
		else
			GraphData.getInstance().previousInitialFile = edgesFileName;
			
		TreeSet<Integer> vertexSet = new TreeSet<Integer>();
		Hashtable<Integer, Pair<Integer, Integer>> edgesSet = loadEdgesAndVertices(edgesFileName, vertexSet);
		Hashtable<String, Integer> weightTable = loadWeights(weightsFileName);
		
		Graph<Integer, Integer> jungGraph = new UndirectedSparseGraph<Integer, Integer>();
		int edgeCount = 0;

		// iterate on edges
		Hashtable<Integer, Integer> edgesAndWeights = new Hashtable<Integer, Integer>();
		
		Iterator<Integer> edgeItearator = edgesSet.keySet().iterator();
		while(edgeItearator.hasNext()) {
			int edgeID = edgeItearator.next();
			Pair<Integer, Integer> edge = edgesSet.get(edgeID);
			jungGraph.addEdge(edgeCount++, edge.getLeft(), edge.getRight());
			
			String edgeName = edge.getLeft() < edge.getRight() ? String.valueOf(edge.getLeft()) + "-" + String.valueOf(edge.getRight()) :
				String.valueOf(edge.getRight()) + "-" + String.valueOf(edge.getLeft());
			edgesAndWeights.put(edgeID, weightTable.get(edgeName));
		}
		
		GraphData graphData = GraphData.getInstance();
		graphData.setGraph(jungGraph);
		graphData.setVertexSet(vertexSet);
		graphData.setEdgesSet(edgesSet);
		graphData.setWeightTable(weightTable);
		graphData.setEdgesAndWeights(edgesAndWeights);
		
		return graphData;
	}
	

	private static Hashtable<Integer, Pair<Integer, Integer>> loadEdgesAndVertices(String edgesFileName, TreeSet<Integer> vertexSet) {
		// all edges
		Hashtable<Integer, Pair<Integer, Integer>> edgesSet = new Hashtable<Integer, Pair<Integer, Integer>>();
		
		File edgesFile = new File(edgesFileName);
		if(edgesFile.exists() && !edgesFile.isDirectory()) { 
			try {
				File file = new File(edgesFileName);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);

				String line;
				while ((line = bufferedReader.readLine()) != null) {

					String[] parts = line.split(" ");
					int firstNode = Integer.valueOf(parts[0]);
					int secondNode = Integer.valueOf(parts[1]);			
					int hopsNumber = Integer.valueOf(parts[2]);
					if(firstNode < secondNode && hopsNumber == 1) {
						vertexSet.add(firstNode);
						vertexSet.add(secondNode);
						edgesSet.put(edgesSet.size(), new Pair<Integer, Integer>(firstNode, secondNode));
					}
				}
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			
			return edgesSet;
		} else
			return null;
		
	}
	
	
	private static Hashtable<String, Integer> loadWeights(String weightsFileName) {
		// all edges
		Hashtable<String, Integer> weightTable = new Hashtable<String, Integer>();
		
		File weightsFile = new File(weightsFileName);
		if(weightsFile.exists() && !weightsFile.isDirectory()) { 

			try {
				File file = new File(weightsFileName);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);

				String line;
				while ((line = bufferedReader.readLine()) != null) {

					String[] parts = line.split(" ");
					int firstNode = Integer.valueOf(parts[0]);
					int secondNode = Integer.valueOf(parts[1]);	
					
					if(firstNode < secondNode)
					{
						String edgeName = String.valueOf(firstNode) + "-" + String.valueOf(secondNode);
						
						int weight = Float.valueOf(parts[2]).intValue();
						weightTable.put(edgeName, weight);
					}
				}
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			
			return weightTable;
		} else
			return null;
	}
	
	
	
//	public Graph<Integer, Integer> getGraph() {
//		return jungGraph;
//	}
	public void setGraph(Graph<Integer, Integer> graph) {
		this.jungGraph = graph;
	}
	
	public Hashtable<Integer, Pair<Integer, Integer>> getEdgesSet() {
		return edgesSet;
	}

	public void setEdgesSet(Hashtable<Integer, Pair<Integer, Integer>> edgesSet) {
		this.edgesSet = edgesSet;
	}

	public Hashtable<String, Integer> getWeightTable() {
		return weightTable;
	}

	public void setWeightTable(Hashtable<String, Integer> weightTable) {
		this.weightTable = weightTable;
	}

	public Hashtable<Integer, Integer> getEdgesAndWeights() {
		return edgesAndWeights;
	}

	public void setEdgesAndWeights(Hashtable<Integer, Integer> edgesAndWeights) {
		this.edgesAndWeights = edgesAndWeights;
	}


	public TreeSet<Integer> getVertexSet() {
		return vertexSet;
	}


	public void setVertexSet(TreeSet<Integer> vertexSet) {
		this.vertexSet = vertexSet;
	}
	
	public Graph<Integer, String> getSubgraph(List<Integer> node) {
				
		Graph<Integer, String> jungSubgraph = new UndirectedSparseGraph<Integer, String>();		
		if(node.size() == 1)
		{
		    jungSubgraph.addVertex(node.get(0));
		}
		for(int i=0; i<node.size(); i++)
			for(int j=0; j<node.size(); j++)		
			{
				Integer firstNodeID = node.get(i);
				Integer secondNodeID = node.get(j);
				
				if (firstNodeID<secondNodeID)
				{
					String edgeName = String.valueOf(firstNodeID) + "-" + String.valueOf(secondNodeID);
					jungSubgraph.addEdge(edgeName, firstNodeID, secondNodeID);
				}				
			}	
		
		return jungSubgraph;
	}
}
