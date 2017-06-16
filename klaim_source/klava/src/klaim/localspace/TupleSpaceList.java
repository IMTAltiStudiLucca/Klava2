/*
 * Created on 14 Nov 2016
 */
package klaim.localspace;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mikado.imc.events.EventGeneratorAdapter;

import klava.KlavaException;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpace;
import klava.index_space.MultiIndex;

public class TupleSpaceList extends EventGeneratorAdapter implements
TupleItem, Serializable, TupleSpace {

    Vector<Tuple> tupleList = new Vector<Tuple>();
    
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    @Override
    public void out(Tuple t) {
        dataLock.lock();
        tupleList.add(t);
        responseIsBack.signalAll();
        dataLock.unlock();
    }

    @Override
    public boolean read(Tuple t) throws InterruptedException {
        return searchForTuple(t, false, true);
    }

    @Override
    public boolean in(Tuple t) throws InterruptedException {
        return searchForTuple(t, true, true);
    }
    
    @Override
    public boolean read_nb(Tuple t) {
        return searchForTuple(t, false, false);
    }

    @Override
    public boolean in_nb(Tuple t) {
        return searchForTuple(t, true, false);
    }
    
    
    
    public static boolean checkTupleByValue(Tuple tuple, Tuple template) throws IOException
    {       
        boolean result = true;
        for(int i = 0; i<template.length(); i++)
        {
            boolean instOfClass = template.getItem(i) instanceof Class;
            if (instOfClass)
            {
                continue;
            } else
            {           
                String type = template.getItem(i).getClass().getName();
                if(MultiIndex.acceptedFieldDataTypeList.contains(type))
                { 
                    if(!template.getItem(i).equals(tuple.getItem(i)))
                    {
                        result = false;
                        break;
                    }
                } else
                {
                    result = false;
                }
            }        
        }     
        return result;
    }
    
    
    private boolean matchTuple(Tuple tupleToMatch, Tuple template) throws IOException {
        boolean result = false;
        
        if(template.length() != tupleToMatch.length())
            result = false;
        else
            result = checkTupleByValue(tupleToMatch, template);
        return result;
    }
    
//    private boolean matchTuple2(Tuple tupleToMatch, Tuple template) throws IOException {
//        boolean result = false;
//        
//        
//        if(template.length() != tupleToMatch.length())
//        {
//            result = false;
//        }
//        else
//        {
//            for(int i = 0; i < template.length(); i++) {
//                if(true
//                    && (!tupleToMatch.getItem(i).toString().equals(template.getItem(i).toString())))
//                    return false;
//            }
//        } 
//        return result;
//    } 
       
    private boolean findTuple(Tuple template, boolean remove) {
        boolean result = false;
        // check type        
        for(int i=0; i< tupleList.size(); i++)
        {
            Tuple currentTuple = tupleList.get(i);
            //boolean bMatch = currentTuple.match(template);
            
            boolean bMatch = false;
            try {
                bMatch = matchTuple(currentTuple, template);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(bMatch)
            {
                template.copy(currentTuple);
                // if it is required to remove the tuple
                if(remove)
                    tupleList.remove(i);
                result = true;
                break;
            }
        }
        
        return result;
    }
      
    
    private boolean searchForTuple(Tuple template, boolean remove, boolean blocking)
    {
        boolean result = false;       
        while(!result)
        {
            dataLock.lock();
            // search
            
            result = findTuple(template, remove);
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
    public void setSettings(Hashtable<String, List<Object>> settings) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int length() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void removeTuple(int i) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAllTuples() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Enumeration<Tuple> getTupleEnumeration() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFormal() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setValue(Object o) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setValue(String o) throws KlavaException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object duplicate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        dataLock.lock();
        tupleList.clear();
        dataLock.unlock();
        
    }


}
