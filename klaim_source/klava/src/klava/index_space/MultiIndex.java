/*
 * Created on Mar 7, 2016
 */
package klava.index_space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import klava.Tuple;

/**
 * 
 * @author Vitaly Buravlev
 * @version $Revision$
 */
public class MultiIndex
{
    // collection of indexes
    public List<HashMap<String, HashSet<Long>>> indexesMap; 
    
    public final Lock dataLock = new ReentrantLock();
    
    public static final HashSet<String> acceptedFieldDataTypeList = setAcceptedFieldDataType();
    
    public MultiIndex()
    {
        indexesMap = new ArrayList<HashMap<String, HashSet<Long>>>();
    }

    
    /**
     * update indexesMap with data of a new tuple
     * @param t
     * @param tupleID
     */
    public void add(Tuple t, long tupleID)
    {
        for(int i =0; i < t.length(); i++)
        {                          
            if(indexesMap.size() <= i)
            {
                indexesMap.add(new HashMap<String, HashSet<Long>>());
            }
            
            boolean acc = acceptedFieldDataTypeList.contains(t.getItem(i).getClass().getName());
     
            if(!acc)
                continue;
            String value = MultiIndex.getHashCode(t.getItem(i).toString());
            
            // add tupleID in the hashmap associated with field content
            HashMap<String, HashSet<Long>> indexMap = indexesMap.get(i);
            boolean contain = indexMap.containsKey(value);
            if(!contain)
            {
                HashSet<Long> list = new HashSet<Long>();    
                list.add(tupleID);
                indexMap.put(value, list);
            } else
            {
                indexMap.get(value).add(tupleID);
            }          
        }
    }
   
    
    /**
     * generally, it is possible to save tuple ID and all indexes associated with it as an inverse structure to the index collection
     * but ..
     * @param tuple
     * @param tupleID
     * @throws Exception
     */
    public void removeIndexDataByTuple(Tuple tuple, long tupleID) throws Exception
    {
        // create structure for the update of the indexes
        // indexID - hash value
        HashMap<Integer, String> deletionInfo = new HashMap<Integer, String>();
        for(int i = 0; i < tuple.length(); i++)
        {
            String value = MultiIndex.getHashCode(tuple.getItem(i).toString());  //String.valueOf(tuple.getItem(i).toString().hashCode());
            if(indexesMap.size() <= i)
            {
                throw new Exception("Achtung! MultiIndex:updateIndexes. Index is corrupted");
            }
            deletionInfo.put(i, value);
        }
        
        // check all indexes
        for(Entry<Integer, String> pair : deletionInfo.entrySet())
        {
            int indexID = pair.getKey();
            String value = pair.getValue();
            
            // remove tupleID from index
            if(indexesMap.size() < indexID  || !indexesMap.get(indexID).containsKey(value))
            {
                throw new Exception("Achtung! MultiIndex:updateIndexes. Index's value was not found");
            } 
            indexesMap.get(indexID).get(value).remove(tupleID);
            
            // TODO remove values (hashes) with empty list
        }
    }
    
    /**
     * get indexes with the highest variability
     * @return
     */
    public List<Map.Entry<Integer, Integer>> getSortedIndexes(/*int maxIndexNumber*/)
    {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for(int i = 0; i < indexesMap.size() /*&& i < maxIndexNumber*/; i++)
        {
            int numberOfVariation = indexesMap.get(i).keySet().size();
            map.put(i, numberOfVariation);
        }
        
        List<Map.Entry<Integer, Integer>> sortedbyValueMap = entriesSortedByValues(map);
        return sortedbyValueMap;
    }
    
    
    
    public static String getHashCode(String str)
    {
        if(str == null)
            return null;
        else
            return str; //hash(str); //str.hashCode();
    }
    
    /**
     * get hash of the string
     * @param str
     * @return
     */
/*    public static Long getHashCode(String str)
    {
        if(str == null)
            return null;
        else
            return hash(str); //str.hashCode();
    }
    
    private static long hash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
          h = 31*h + string.charAt(i);
        }
        return h;
    }*/
    
    /*
     * method, Comparator, for sorting key-value collection by value
     */
    static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) 
    {
        List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

        Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() 
        {
            public int compare(Entry<K,V> e1, Entry<K,V> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });

        return sortedEntries;
    }

    public static HashSet<String> setAcceptedFieldDataType()
    {
        HashSet<String> acceptedFieldDataTypeList = new HashSet<String>();
          
        acceptedFieldDataTypeList.add(Boolean.class.getName());
        acceptedFieldDataTypeList.add(Byte.class.getName());
        acceptedFieldDataTypeList.add(Short.class.getName());
        acceptedFieldDataTypeList.add(Integer.class.getName());
        acceptedFieldDataTypeList.add(Long.class.getName());
        acceptedFieldDataTypeList.add(Float.class.getName());
        acceptedFieldDataTypeList.add(Double.class.getName());
        acceptedFieldDataTypeList.add(Character.class.getName());        
        acceptedFieldDataTypeList.add(String.class.getName());
        
        
        return acceptedFieldDataTypeList;
    }

}