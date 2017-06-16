/*
 * Created on 26 Oct 2016
 */
package klaim.concurrent_structures;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mikado.imc.events.EventGeneratorAdapter;

import klaim.localspace.SecondCheckHT;
import klaim.localspace.SecondCheckHT.SecondCheckDataHT;
import klaim.localspace.TupleSpaceHashtable;
import klava.KlavaException;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpace;
import klava.index_space.IndexedTupleSpace;

public class TupleSpaceHashtableCS extends EventGeneratorAdapter implements
        TupleItem, Serializable, TupleSpace
{   
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    SecondCheckHT sCheck = new SecondCheckHT();
    
    ConcurrentHashMap<String, ConcurrentHashMap<Long, LinkedList<Tuple>>> tupleTable = new ConcurrentHashMap<>();
    // settings
    Hashtable<String, List<Object>> settingsTable = new Hashtable<>();
    
    public void out(Tuple tuple) {   
        
     //   System.out.println(Thread.currentThread().getName() + " before lock" + tuple.toString());
        
        tuple.removed.set(false);
        List<Object> patternCollection = settingsTable.get(tuple.getTupleType());
        ArrayList<Long> dataKeyList = new ArrayList<>(patternCollection.size());
        for(int i=0; i<patternCollection.size(); i++)
        {
            Boolean[]  pattern = (Boolean[])patternCollection.get(i);
            if(pattern == null)
                continue;
            String patternKey = TupleSpaceHashtableCS.getPattern(pattern);
            String treeKey = TupleSpaceHashtableCS.getTableKey(tuple.getTupleType(), patternKey);
            tupleTable.putIfAbsent(treeKey, new ConcurrentHashMap<Long, LinkedList<Tuple>>());
            
            ConcurrentHashMap<Long, LinkedList<Tuple>> smallTable = tupleTable.get(treeKey);
            Long dataKey = getHashedValue(tuple, patternKey);  
            
            smallTable.putIfAbsent(dataKey, new LinkedList<Tuple>());
            
            LinkedList<Tuple> list = smallTable.get(dataKey);
            synchronized (list) {
                list.add(tuple);
            }   
            dataKeyList.add(dataKey);
        }
        
        
        // if there is no processes waiting and reading - do not add it here
        if(sCheck.readingProcesses.size() > 0) 
        {
            dataLock.lock();

    //        System.out.println(Thread.currentThread().getName() + " out " + tuple.toString());
            sCheck.addTuple(tuple, dataKeyList);

            dataLock.unlock();
            
            sCheck.notifyProcesses();
            
        } 
//        else
//            System.out.println(Thread.currentThread().getName() + " miss out" + tuple.toString());
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
            ConcurrentHashMap<Long, LinkedList<Tuple>> smallTable = tupleTable.get(tableKey);
            
            if(smallTable.containsKey(dataKey)) {
                LinkedList<Tuple> list = smallTable.get(dataKey);
                synchronized (list) 
                {              

                    for(Iterator<Tuple> iterator = list.iterator(); iterator.hasNext();)
                    {
                        Tuple currentTuple = iterator.next();
                        try {
                            if(IndexedTupleSpace.checkTupleByValue(currentTuple, template)) {
                                                               
                                // if it is required to remove the tuple
                                if(remove) {
                                    result = currentTuple.removed.compareAndSet(false, true);
                                     if (result) {
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
                                    }
                                }
                            }
                        } catch (IOException e) {
                            result = false;
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
            
        return result;
    }
    
    private boolean searchForTuple(Tuple template, boolean remove, boolean blocking)
    { 
        String subscriptionName = Thread.currentThread().getName();
        dataLock.lock();
        SecondCheckDataHT scData = sCheck.subscribeProcess(subscriptionName);
        dataLock.unlock();

        String patternKey = getPattern(template);
        String tableKey = TupleSpaceHashtableCS.getTableKey(template);
        Long tupleKey = getHashedValue(template, patternKey);
        boolean result = false;
        result = findTuple(template, tableKey, tupleKey, remove);
 //       System.out.println(Thread.currentThread().getName() + " initial read finished " + result + " "+ template.toString());
        
        if(!blocking)
        {
            dataLock.lock();
            sCheck.unSubscribeProcess(subscriptionName);
            dataLock.unlock();
            return result;
        }
             

        if(!result) {
            while(true) {

               // long oldState = tupleSpaceState.get();
                long oldState = scData.processState.get();
                result = SecondCheckHT.checkIncommingTuples(scData, tupleKey, template, remove);
                if(result) {
                    break;
                }
                              
                // here the new tuple comes
                try {
                    scData.syncObject.lock.lock();
                    long newState = scData.processState.get();
                    if(newState == oldState)
                    {
            //            System.out.println(Thread.currentThread().getName() + " go to wait ");
                        scData.syncObject.await();
            //            System.out.println(Thread.currentThread().getName() + " wake up ");
                    }
                    scData.syncObject.lock.unlock();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                


            } 
        }
  
        sCheck.unSubscribeProcess(subscriptionName);        
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
           
    public static Long getHashedValue(Tuple tuple, String pattern) {
        if(tuple == null)
            return null;
           
        StringBuilder buider = new StringBuilder();
        for(int i=0; i<pattern.length(); i++)
        {
            if(pattern.charAt(i) != '1')
                continue;
            boolean acceptableType = TupleSpaceHashtable.acceptedFieldDataTypeList.contains(tuple.getItem(i).getClass().getName());
            if (acceptableType)
            {
                buider.append(tuple.getItem(i).toString() + "|");
            }
        }
        Long hashtableKey = getHashCode(buider.toString());
        return hashtableKey;
    }
    
    public static String getPattern(Boolean[] pattern) {
        StringBuilder sb = new StringBuilder(pattern.length);
        for(int i=0; i<pattern.length; i++) {
            sb.append(pattern[i] ? '1' : '0');
        }
        return sb.toString();
    }
    
    public static String getPattern(Tuple template) {
        StringBuilder sb = new StringBuilder(template.length());
        for(int i=0; i<template.length(); i++) {
            char c = template.getItem(i) instanceof Class ? '0' : '1';
            sb.append(c);
        }
        return sb.toString();
    }    

    public static String getTableKey(String tupleType, Boolean[] pattern) {
        String patternKey = TupleSpaceHashtableCS.getPattern(pattern);
        return getTableKey(tupleType, patternKey);
    }
    
    public static String getTableKey(String tupleType, String patternKey) {
        String tableKey = tupleType + "_" + patternKey;
        return tableKey;
    }
    
    public static String getTableKey(Tuple template) {
        String patternKey = TupleSpaceHashtableCS.getPattern(template);
        return getTableKey(template.getTupleType(), patternKey);
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
            return hash(str);
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
