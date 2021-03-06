/*
 * Created on 26 Oct 2016
 */
package klaim.localspace;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mikado.imc.events.EventGeneratorAdapter;

import klaim.concurrent_structures.TupleSpaceHashtableCS;
import klava.KlavaException;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpace;
import klava.index_space.IndexedTupleSpace;

public class TupleSpaceHashtable extends EventGeneratorAdapter implements
        TupleItem, Serializable, TupleSpace
{
    public static final ArrayList<String> acceptedFieldDataTypeList = setAcceptedFieldDataType();
    
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    ConcurrentHashMap<String, Hashtable<Long, LinkedList<Tuple>>> tupleTable = new ConcurrentHashMap<>();
    Hashtable<String, AtomicBoolean> updateControlTAble = new Hashtable<>();
    
    // settings
    Hashtable<String, List<Object>> settingsTable = new Hashtable<>();
    
    AtomicLong tupleSpaceState = new AtomicLong(0);
    
    public void out(Tuple tuple) {
            
        tuple.removed.set(false);
        List<Object> patternCollection = settingsTable.get(tuple.getTupleType());
        for(int i=0; i<patternCollection.size(); i++)
        {
            Boolean[]  pattern = (Boolean[])patternCollection.get(i);
            if(pattern == null)
                continue; 
            String patternKey = TupleSpaceHashtableCS.getPattern(pattern);
            String tableKey = TupleSpaceHashtableCS.getTableKey(tuple.getTupleType(), patternKey);
            tupleTable.putIfAbsent(tableKey, new Hashtable<Long, LinkedList<Tuple>>());
            
            Hashtable<Long, LinkedList<Tuple>> dataTable = tupleTable.get(tableKey);
            Long dataKey = TupleSpaceHashtableCS.getHashedValue(tuple, patternKey);  
            
            dataTable.putIfAbsent(dataKey, new LinkedList<Tuple>());
            
            LinkedList<Tuple> list = dataTable.get(dataKey);
            synchronized (list) 
            {
                list.add(tuple);
            }       
        }

        dataLock.lock();
        tupleSpaceState.incrementAndGet();
        responseIsBack.signalAll();
        dataLock.unlock();   
    }
    
    public boolean read(Tuple template) throws InterruptedException {  
        return searchForTuple(template, false, true);
    }
    
    public boolean in(Tuple template) throws InterruptedException {
        return searchForTuple(template, true, true);
    }
    
    public boolean read_nb(Tuple template) {
        return searchForTuple(template, false, false);
    }

    public boolean in_nb(Tuple template) {
        return searchForTuple(template, true, false);
    }

    private boolean findTuple(Tuple template, String tableKey, Long dataKey, boolean remove) {
        boolean result = false;
        
        if(tupleTable.containsKey(tableKey)) {
            Hashtable<Long, LinkedList<Tuple>> smallTable = tupleTable.get(tableKey);
            
            if(smallTable.containsKey(dataKey)) {
                
                LinkedList<Tuple> list = smallTable.get(dataKey);
                synchronized (list)
                {              
                    for(Iterator<Tuple> iterator = list.iterator(); iterator.hasNext();)
                    {
                        Tuple currentTuple = iterator.next();
                        try {
                            if(IndexedTupleSpace.checkTupleByValue(currentTuple, template))
                            {
                                // if it is required to remove the tuple
                                if(remove) {
                                    if (currentTuple.removed.compareAndSet(false, true)) {
                                        template.copy(currentTuple);
                                       iterator.remove();
                                        result = true;
                                        break;
                                    } else {
                                        iterator.remove();
                                    }
                                } else {
                                    if(!currentTuple.removed.get()) {
                                        template.copy(currentTuple);
                                        result = true;
                                        break;
                                    } else {
                                        iterator.remove();
                                    }
                                        
                                }
                            }
                        } catch (IOException e) {
                            result = false;
                            e.printStackTrace();
                        }
                        
                        System.out.println("more search " + template);
                    }
                }
            }
        }
            
        return result;
    }
    
    private boolean searchForTuple(Tuple template, boolean remove, boolean blocking)
    {
        String patternKey = TupleSpaceHashtableCS.getPattern(template);
        String tableKey = TupleSpaceHashtableCS.getTableKey(template);
        Long tupleKey = TupleSpaceHashtableCS.getHashedValue(template, patternKey);
        
        boolean result = false;
        
            
        while(!result)
        {           
            long oldState = tupleSpaceState.get();
            result = findTuple(template, tableKey, tupleKey, remove);
            
            if(!blocking) {
                break;
            }
            
            if(!result)
            {
                dataLock.lock();
                long newState = tupleSpaceState.get();
                if(newState == oldState) {
                    try {
                        responseIsBack.await();
                     } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("!!!!!!!");
                }
                dataLock.unlock();
            }
        }
        return result;
    }

   
    public boolean read_t(Tuple t, long TimeOut) throws InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean in_t(Tuple t, long TimeOut) throws InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }



    public int length() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void removeTuple(int i) {
        // TODO Auto-generated method stub
        
    }

    public void removeAllTuples() {
        // TODO Auto-generated method stub
        
    }

    public Enumeration<Tuple> getTupleEnumeration() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static ArrayList<String> setAcceptedFieldDataType()
    {
        ArrayList<String> acceptedFieldDataTypeList = new ArrayList<String>();
          
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
    
    public boolean isFormal() {
        // TODO Auto-generated method stub
        return false;
    }



    public void setValue(Object o) {
        // TODO Auto-generated method stub
        
    }



    public void setValue(String o) throws KlavaException {
        // TODO Auto-generated method stub
        
    }



    public Object duplicate() {
        // TODO Auto-generated method stub
        return null;
    }



    public void setSettings(Hashtable<String, List<Object>> settings) {
        this.settingsTable = settings;
        
    }
       
    public static Long getHashCode(String str)
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
    }

    @Override
    public void clear() {
        tupleTable.clear();
    }
    
}
