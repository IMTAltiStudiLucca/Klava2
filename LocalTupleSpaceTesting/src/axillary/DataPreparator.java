package axillary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import klava.Tuple;

public class DataPreparator {

	
	public static ArrayList<Tuple> prepareData(String fileName, Hashtable<String, List<Object>> typesTable) throws IOException
	{
		ArrayList<Object[]> objArray = DatasetGenerator.readFromFile(fileName);
		
		ArrayList<Tuple> tuples = new ArrayList<>();
		for(int i=0; i<objArray.size(); i++)
		{
			Tuple t = new Tuple(objArray.get(i));
			String type = String.valueOf(DatasetGenerator.getStructure(t).hashCode());
			t.setTupleType(type);
			
			tuples.add(t);
			
			if(!typesTable.containsKey(type))
			{
				Object[] typeStructure = new Object[t.length()];
				for(int fInd=0; fInd<t.length(); fInd++)
					typeStructure[fInd] = t.getItem(fInd).getClass();
				
				typesTable.put(type, new ArrayList<Object>(Arrays.asList(typeStructure)));
			}
		}
		
		return tuples;
		
	}
	
	public static ArrayList<Tuple> prepareQueries(ArrayList<Tuple> tuples, int numberToTest, int maxNumberQueriesPerTupleType)
	{	
		ArrayList<Tuple> templates = new ArrayList<>();
	
		// pick N (numberToTest) tuples for templates
		Random rnd = new Random();
		ArrayList<Integer> tupleIDs = new ArrayList<Integer>();
		while(true)
		{
			int id = rnd.nextInt(tuples.size());
			if(!tupleIDs.contains(id))
				tupleIDs.add(id);
			
			if(tupleIDs.size() == numberToTest)
				break;
		}
		
		HashMap<String, HashSet<Integer>> tupleTypeList = new HashMap();
		// getTypes
		for(int i =0; i<tupleIDs.size(); i++)
		{
			int tupleID = tupleIDs.get(i);
			Tuple template = (Tuple) tuples.get(tupleID).clone();
			int indexOfFieldToRemove = rnd.nextInt(template.length());
			
			
			String structureStr = DatasetGenerator.getStructure(template);
			if(!tupleTypeList.containsKey(structureStr))
				tupleTypeList.put(structureStr, new HashSet<Integer>());
				
			while(true)
			{
				if(!tupleTypeList.get(structureStr).contains(indexOfFieldToRemove) && tupleTypeList.get(structureStr).size() == maxNumberQueriesPerTupleType)
				{
					// generate new indexOfFieldToRemove
					indexOfFieldToRemove = rnd.nextInt(template.length());
				} else
				{
					tupleTypeList.get(structureStr).add(indexOfFieldToRemove);
					break;
				}
			}
				
			Object itemToChange = template.getItem(indexOfFieldToRemove);
			
			// now it is a wildcard
			//template.setItem(indexOfFieldToRemove, itemToChange.getClass());
			templates.add(template);
		}
		
		
	/*	
		// modify tuples to template by adding wildcard		
		for(int i=0; i<tupleIDs.size(); i++)
		{
			int tupleID = tupleIDs.get(i);
			Tuple template = (Tuple) tuples.get(tupleID).clone();
			int indexOfFieldToRemove = rnd.nextInt(template.length());
			Object itemToChange = template.getItem(indexOfFieldToRemove);
			// now it is a wildcard
			template.setItem(indexOfFieldToRemove, itemToChange.getClass());
			
			
			templates.add(template);
		}*/
		
		return templates;
	}

	public static Hashtable<String, List<Object>> extractSettingsForHashtable(ArrayList<Tuple> templates) {
		// now for hash table
		// it is necessary to additionally generate 
		Hashtable<String, List<Object>> hashtableSettings = new Hashtable<>();
		
		HashSet<String> verifiedTemplateTypes = new HashSet<>();
		// modify tuples to template by adding wildcard		
		for(int i=0; i<templates.size(); i++)
		{
			Tuple template = templates.get(i);
			String structure = String.valueOf(DatasetGenerator.getStructure(template).hashCode());
			
			String settingStr = "";
			Boolean[] setting = new Boolean[template.length()];
			for(int fInd=0; fInd<template.length(); fInd++)
			{
				if(template.getItem(fInd) instanceof Class)
					setting[fInd] = false;
				else
					setting[fInd] = true;
				settingStr += setting[fInd] ? "1" : "0";
			}
			
			if (!verifiedTemplateTypes.contains(structure+settingStr))
			{
				verifiedTemplateTypes.add(structure+settingStr);
				
				if(hashtableSettings.containsKey(structure))
					hashtableSettings.get(structure).add(setting);
				else {
					hashtableSettings.put(structure, new ArrayList<>());
					hashtableSettings.get(structure).add(setting);
				}	
			}	
		}
		
		return hashtableSettings;
	}
}
