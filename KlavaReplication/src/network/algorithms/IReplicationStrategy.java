package network.algorithms;

import java.util.ArrayList;

import network.structures.GraphData;

public interface IReplicationStrategy 
{
	public ArrayList<String> getMostImportantVertices(GraphData graphData, int topVerticesNumber);
}
