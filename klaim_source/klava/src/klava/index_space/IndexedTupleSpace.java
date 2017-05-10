/*
 * Created on Mar 7, 2016
 */
package klava.index_space;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mikado.imc.events.EventGeneratorAdapter;

import klava.KlavaException;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpace;
import klava.manager.TupleRequest;

/*
 * TODO 
 */



/**
 * @author Vitaly Buravlev
 * @version $Revision$
 */
public class IndexedTupleSpace extends EventGeneratorAdapter implements
TupleItem, Serializable, TupleSpace
{

    // current number of tuple
    Long tupleIndex = 0L;
    
    // collection of tuples
    Hashtable<Long, Pair<Tuple, String>> tupleList = null;
    
    // structure of indexes
    MultiIndex multiIndex;
    
    final Lock tupleIndexLock = new ReentrantLock();
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    ConcurrentLinkedQueue<TupleRequest> requestToTupleSpace;
    
    RequestQueueProcessor requestProcessor;
    
    
    public IndexedTupleSpace()
    {
        tupleList = new Hashtable<Long, Pair<Tuple, String>>();
        multiIndex = new MultiIndex();
        
        requestProcessor = new RequestQueueProcessor();
 //       requestToTupleSpace = new ConcurrentLinkedQueue<TupleRequest>();
        
    }
    
    /**
     * write tuple to tuple space
     * @param tuple
     */
    public void out(Tuple tuple)
    {      
//        Profiler.begin("out-overall");
        
//        Profiler.begin("out-ID");
        
        // get next tupleID
        Long tupleIndex = getNextTupleIndex();
//        Profiler.end("out-ID");
        
//        Profiler.begin("out-struct");
        // just put the tuple into the list
        Pair<Tuple, String> pair = new Pair<Tuple, String>(tuple, getStructure(tuple));
//        Profiler.end("out-struct");
        
 //       Profiler.begin("out-put");
        tupleList.put(tupleIndex, pair);
//        Profiler.end("out-put");


//        Profiler.begin("out-index");
        // update index
        dataLock.lock();
        multiIndex.add(tuple, tupleIndex);
        responseIsBack.signalAll();
        dataLock.unlock();
        
//        Profiler.end("out-index");
        
 //       Profiler.end("out-overall");
    }
    
    /**
     * get tuple from tuple space by template
     * @param template
     * @param removeTuple
     * @return
     * @throws Exception
     */
    public boolean get(Tuple template, boolean removeTuple) throws Exception
    {
        //synchronized (multiIndex) 
        {
            Pair<Long, Tuple> tupleData = searchOneTuple(template);
    
            if(tupleData != null)
            {
                if(removeTuple)
                    deleteTuple(tupleData.getSecond(), tupleData.getFirst());
                
                // first we save the original template
                template.setOriginalTemplate();
                // we update the original
                template.copy(tupleData.getSecond());
                return true;
            } else
                return false;
        }
    }
     
    
    /**
     * remove tuple from tuple space
     * @param tuple
     * @param tupleID
     * @return
     * @throws Exception
     */
    boolean deleteTuple(Tuple tuple, long tupleID) throws Exception
    {
        tupleList.remove(tupleID);
                
        dataLock.lock();
        multiIndex.removeIndexDataByTuple(tuple, tupleID); 
        dataLock.unlock();
        return true;
    }
    
    /**
     * search one tuple
     * @param template
     * @return
     * @throws IOException 
     */
    Pair<Long, Tuple> searchOneTuple(Tuple template) throws IOException
    {
        Set<Long> tupleIDList = null;
        dataLock.lock();
        tupleIDList = filterByTemplate(template);
        dataLock.unlock();
        Pair<Long, Tuple> resultTuple = filterByTupleStructure(template, tupleIDList);
        return resultTuple;
    }

    /**
     * filter by template and take tupleIDs which satisfy it
     * @param template
     * @return
     */
    Set<Long> filterByTemplate(Tuple template)
    {
        List<Map.Entry<Integer, Integer>> indexList = multiIndex.getSortedIndexes();
        
        Set<Long> intersection = null;
        // check available index by index
        for(int i = 0; i<indexList.size(); i++)
        {
            int indexID = indexList.get(i).getKey();
            if(indexID > template.length())
                continue;
            
            // if true - means that it is formal field
            if(template.getItem(indexID) instanceof Class)
            {
                continue;
            }
            
            Long item = MultiIndex.getHashCode(template.getItem(indexID).toString()); //String.valueOf( template.getItem(indexID).toString().hashCode() );  // (String) template.getItem(indexID);
            if(item != null)
            {
                Long fieldValue = item;
                if(intersection == null)
                {
                    intersection = multiIndex.indexesMap.get(indexID).get(fieldValue);
                }
                else
                {
                    HashSet<Long> set = multiIndex.indexesMap.get(indexID).get(fieldValue);
                    if(set != null)
                        intersection.retainAll(set);
                    else
                        intersection = null;
                }
                
                if(intersection == null || intersection.size() < 1)
                    return null;
                else if(intersection.size() > 1)
                    continue;
                else 
                {
                    // if only one result
                    return intersection;
                }             
            }
        }
        
        // finally if all tuples look like the same - send all
        return intersection;
    }
    
    
    /**
     * check structure of tuple and values (if necessary)
     * @param template
     * @param tupleIDList
     * @return
     * @throws IOException 
     */
    Pair<Long, Tuple> filterByTupleStructure(Tuple template, Set<Long> tupleIDList) throws IOException
    {
        if(tupleIDList == null || template == null || tupleIDList.size() == 0)
            return null;
        // obtain structure of template
        String templateStructure = getStructure(template);
        int templateLength = template.length();
        
        // check all tuples
        Iterator<Long> iterator = tupleIDList.iterator(); 
        while (iterator.hasNext())
        {
            Long tupleID = iterator.next();
            Pair<Tuple, String> pair = tupleList.get(tupleID);
            if(pair.getFirst().length() != templateLength ||
               !templateStructure.equals(pair.getSecond()))
            {
                continue;
            } else
            {
                // check by value
                if(checkTupleByValue(pair.getFirst(), template))
                    return new Pair<Long, Tuple>(tupleID, pair.getFirst());
                else 
                    continue;
            }            
        }
        return null;
    }
    
    /**
     * filter by template
     * @param tuple
     * @param template
     * @return
     * @throws IOException 
     */
    public static boolean checkTupleByValue(Tuple tuple, Tuple template) throws IOException
    {
        // check field by field
        for(int i = 0; i<template.length(); i++)
        {
            if (template.getItem(i) instanceof Class)
            {
                continue;
            } else
            {
                if(MultiIndex.acceptedFieldDataTypeList.contains(template.getItem(i).getClass().getName()))
                {
                    if (!template.getItem(i).equals(tuple.getItem(i)))
                        return false;
                } else
                {
                    return byteLevelComparision(template.getItem(i), tuple.getItem(i));
                }
            }        
        }
        return true;
    }
    
    /**
     * get string which represents the structure of tuple
     * @param tuple
     * @return
     */
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
    
    /**
     * get next tuple number
     * @return
     */
    Long getNextTupleIndex()
    {
        Long nextIndex = 0L;
        tupleIndexLock.lock();
        nextIndex = tupleIndex++;
        tupleIndexLock.unlock();
        return nextIndex;
    }
    
    
    public static boolean byteLevelComparision(Object obj1, Object obj2) throws IOException
    {
        if(obj1 == null && obj2 == null)
            return true;
        else if(obj1 == null || obj2 == null)
            return false;
        byte[] byteArray1 = serialize(obj1);
        byte[] byteArray2 = serialize(obj2);
        return Arrays.equals(byteArray1, byteArray2);
    }
    
    
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    
    
    private boolean searchForTuple(Tuple template, boolean removeTuple, boolean blocking)
    {       
        boolean result = false;
        while(!result)
        {
            dataLock.lock();
            try {
                result = get(template, removeTuple);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } 
            
            if(!blocking)
            {
                // unlock
                dataLock.unlock();
                break;
            }
            if(!result)
            {
                try {
                    responseIsBack.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // unlock
            dataLock.unlock();
        }
        return result;
    }

    @Override
    public boolean read(Tuple template) throws InterruptedException {
        return searchForTuple(template, false, true);   
    }

    @Override
    public boolean in(Tuple template) throws InterruptedException {
        return searchForTuple(template, true, true);  
    }

    @Override
    public boolean read_nb(Tuple template) {
        return searchForTuple(template, false, false);  
    }

    @Override
    public boolean in_nb(Tuple template) {
        return searchForTuple(template, true, false);  
    }
   

    @Override
    public void setSettings(Hashtable<String, Boolean[]> settings) {
        // TODO Auto-generated method stub
        
    }

    
    @Override
    public boolean read_t(Tuple t, long TimeOut) throws InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean in_t(Tuple t, long TimeOut) throws InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public int length() {
        return 0;
    }

    @Override
    public void removeTuple(int i) {
    }

    @Override
    public void removeAllTuples() {        
    }

    @Override
    public Enumeration<Tuple> getTupleEnumeration() {
        return null;
    }

    @Override
    public boolean isFormal() {
        return false;
    }

    @Override
    public void setValue(Object o) {    
    }

    @Override
    public void setValue(String o) throws KlavaException {       
    }

    @Override
    public Object duplicate() {
        return null;
    }
}
