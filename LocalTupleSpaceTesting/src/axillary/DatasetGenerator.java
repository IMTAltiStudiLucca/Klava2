package axillary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import common.DataGeneration;
import klava.Tuple;

public class DatasetGenerator {
	
	
	/*
	 * Parameters:
	 * - number of tuples
	 * - number of fields
	 * - all tuples are the same
	 * - several templates (?)
	 * - number of types
	 * - parallel test (?)
	 * 
	 */
	public void generateData(String fileName, int numberOfTuples, int numberOfFields, int numberOfTypes, float cardinality)
	{			
		// generate banks with numbers and strings
		ArrayList<Integer> numberBank = generateIntegerBank(numberOfTuples);
		ArrayList<String> stringBank = generateStringBank(numberOfTuples);
		
		// apply the uniqueness
		int sizeWithUniquincess = (int)(numberOfTuples*cardinality);
		ArrayList<Integer> numberBankForUse = copyList(numberBank, sizeWithUniquincess);
		ArrayList<String> stringBankForUse = copyList(stringBank, sizeWithUniquincess);
			
		// create N (numberOfTypes) types
		ArrayList<Class[]> types = generateTypes(numberOfFields, numberOfTypes);
		
			
		ArrayList<Object[]> tuples = generateTuples(numberOfTuples, numberOfFields, numberOfTypes, numberBankForUse, stringBankForUse,
				types);
		
		writeInFile(fileName, tuples);
	}
	
	
	


	private static void writeInFile(String fileName, ArrayList<Object[]> tuples) 
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<tuples.size(); i++)
		{
			Object[] tupleObj = tuples.get(i);
			String tuple = "";
			for(int fInd = 0; fInd<tuples.get(i).length; fInd++)
			{
				tuple += tupleObj[fInd].getClass().getName();
				tuple += "&";
				tuple += tupleObj[fInd].toString();
				tuple += "&&";
			}
			tuple += "\n";
			sb.append(tuple);
		}
		
		DataGeneration.writeStringToFile(fileName, sb.toString());
	}

	public static ArrayList<Object[]> readFromFile(String fileName) throws IOException 
	{
		ArrayList<Object[]> tuples = new ArrayList<>();
		
		ArrayList<String> array = DataGeneration.readFromFile(fileName);
		for(int i=0; i<array.size(); i++)
		{
			String[] fieldStrArray = array.get(i).split("&&");
			
			Object[] tuple = new Object[fieldStrArray.length];
			for(int fInd=0; fInd<fieldStrArray.length; fInd++)
			{
				String[] fieldAndValue = fieldStrArray[fInd].split("&");
				if(fieldAndValue[0].equals(String.class.getName()))
					tuple[fInd] = fieldAndValue[1];
				else if(fieldAndValue[0].equals(Integer.class.getName()))
					tuple[fInd] = Integer.valueOf(fieldAndValue[1]);					
			}
			tuples.add(tuple);
		}
		
		return tuples;
	}

	private static ArrayList<Object[]> generateTuples(int numberOfTuples, int numberOfFields, int numberOfTypes,
			ArrayList<Integer> numberBankForUse, ArrayList<String> stringBankForUse, ArrayList<Class[]> types) 
	{
		ArrayList<Object[]> tuples = new ArrayList<Object[]>();
		
		Random rnd = new Random();
		Random contentRnd = new Random();
		
		// generate tuples
		for(int i=0; i<numberOfTuples; i++)
		{
			Object[] tuple = new Object[numberOfFields];

			// pock a type
			Class[] tupleType = types.get(rnd.nextInt(numberOfTypes));
			for(int fInd=0; fInd<tupleType.length; fInd++)
			{

				int contIndex = contentRnd.nextInt(numberBankForUse.size());
				if(tupleType[fInd] == String.class)
					tuple[fInd] = stringBankForUse.get(contIndex);
				else if(tupleType[fInd] == Integer.class)
					tuple[fInd] = numberBankForUse.get(contIndex);	
			}
			tuples.add(tuple);
		}
		return tuples;
	}


	private static ArrayList<Class[]> generateTypes(int numberOfFields, int numberOfTypes) 
	{
		HashSet<String> addedTypes = new HashSet<String>();
		
		ArrayList<Class[]> types = new ArrayList<Class[]>();
		Random rnd = new Random();

		while(true)
		{		
			Class[] type = new Class[numberOfFields];
			for(int fInd=0; fInd< numberOfFields; fInd++)
			{
				type[fInd] = rnd.nextInt(10) >= 5 ? String.class : Integer.class;
			}	

			// check if the type is unique
			String typeStructure = getStructureForClass(type);
			if(!addedTypes.contains(typeStructure))
			{
				addedTypes.add(typeStructure);
				types.add(type);
			}
			
			if(types.size() == numberOfTypes)
				break;
		}
		return types;
	}
	

	private static ArrayList<Integer> generateIntegerBank(int size)
    {
		ArrayList<Integer> numberBank = new ArrayList<Integer>();
		Random rnd = new Random();
		for (int i=0; i<size; i++)
		{
			numberBank.add(rnd.nextInt(size));
		}
        return numberBank;
    }
    
	private static ArrayList<String> generateStringBank(int size)
    {
		ArrayList<String> stringBank = new ArrayList<String>();
		Random rnd = new Random();
		for (int i=0; i<size; i++)
		{
			String str = "tuple_data_";
			str += rnd.nextInt(size);
			stringBank.add(str);
		}
        return stringBank;
    }
    
	private static ArrayList copyList(ArrayList sourceList, int maxSize)
    {
		ArrayList resultList = new ArrayList();
		for (int i=0; i<maxSize; i++)
		{
			resultList.add(sourceList.get(i));
		}
        return resultList;
    }
    
	
    public static String getStructureForClass(Class[] objArray)
    {
        StringBuilder  sb = new StringBuilder (objArray.length);
        for(int i =0; i<objArray.length; i++)
        {
        	sb.append(objArray[i].getName());
        }
        return sb.toString();
    }
    
    public static String getStructure(Tuple tuple)
    {
        StringBuilder  sb = new StringBuilder (tuple.length());
        for(int i =0; i<tuple.length(); i++)
        {
            Object item = tuple.getItem(i);
            if(item instanceof Class){
                Class className = (Class) item;
                sb.append(className.getName());
            }    
            else
                sb.append(item.getClass().getName());
        }
        return sb.toString();
    }
    
 /*   public static ArrayList<Class> setAcceptedFieldDataType()
    {
    	ArrayList<Class> acceptedFieldDataTypeList = new ArrayList<Class>();
          
        acceptedFieldDataTypeList.add(Byte.class);
        //acceptedFieldDataTypeList.add(Short.class);
        acceptedFieldDataTypeList.add(Integer.class);
        acceptedFieldDataTypeList.add(Long.class);
        //acceptedFieldDataTypeList.add(Float.class);
        //acceptedFieldDataTypeList.add(Double.class);
        //acceptedFieldDataTypeList.add(Character.class);        
        acceptedFieldDataTypeList.add(String.class);
        
        return acceptedFieldDataTypeList;
    }*/
	

/*	public static void datasetGeneration(int _elementsNumber) throws NoSuchAlgorithmException {
		// generate data set
		MessageDigest mdEnc = MessageDigest.getInstance("MD5"); 
		
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < _elementsNumber; i++)
		{
			String md5 = GigaspacesImpDistSearchTest.integerToHashString(mdEnc, i);
			sb.append(md5 + "," + i + "\n");

		}
		DataGeneration.writeStringToFile("hashSet.dat", sb.toString());
	}*/
	
}
