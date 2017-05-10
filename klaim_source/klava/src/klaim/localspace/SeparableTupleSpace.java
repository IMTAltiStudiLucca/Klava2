/*
 * Created on 30 Oct 2016
 */
package klaim.localspace;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mikado.imc.events.EventManager;

import klava.KlavaException;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpace;

public class SeparableTupleSpace<T extends TupleSpace> implements
TupleItem, Serializable, TupleSpace
{
    Class<T> tupleSpaceClass;
    HashMap<String, T> table = new HashMap<String, T>();
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    // settings
    Hashtable<String, Boolean[]> settingsTable = new Hashtable<String, Boolean[]>();
    
    public SeparableTupleSpace(Class<T> tupleSpaceClass)
    {
        this.tupleSpaceClass = tupleSpaceClass;
    }

    @Override
    public void setEventManager(EventManager eventManager) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public EventManager getEventManager() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private String getSeparator(Tuple tuple)
    {
        //return IndexedTupleSpace.getStructure(tuple);
        return tuple.getTupleType();
    }

    @Override
    public void out(Tuple tuple) {
        String tupleType = getSeparator(tuple);
        if(table.containsKey(tupleType))
        {           
            table.get(tupleType).out(tuple);
        } else {
            T space = getInstanceOfT(tupleSpaceClass);
            space.setSettings(settingsTable);
            space.out(tuple);
            
            dataLock.lock();
            table.put(tupleType, space);
            responseIsBack.signalAll();
            dataLock.unlock();
            
        }              
    }

    @Override
    public boolean read(Tuple template) throws InterruptedException {
        String tupleType = getSeparator(template);
        
        boolean result = false;
        while(!result)
        {
            dataLock.lock();
            if(table.containsKey(tupleType))
                result = table.get(tupleType).read(template); 
            else
                responseIsBack.await();
            dataLock.unlock();
        }
        return result;     
    }

    @Override
    public boolean in(Tuple template) throws InterruptedException {
        String tupleType = getSeparator(template);
        
        boolean result = false;
        while(!result)
        {
            dataLock.lock();
            if(table.containsKey(tupleType))
                result = table.get(tupleType).in(template); 
            else
                responseIsBack.await();
            dataLock.unlock();
        }
        return result; 
    }

    @Override
    public boolean read_nb(Tuple template) {
        String tupleType = getSeparator(template);
        boolean result = table.get(tupleType).read_nb(template); 
        return result; 
    }

    @Override
    public boolean in_nb(Tuple template) {
        String tupleType = getSeparator(template);
        boolean result = table.get(tupleType).in_nb(template); 
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
    public void setSettings(Hashtable<String, Boolean[]> settings) {
        this.settingsTable = settings;
        
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
    
    
    public T getInstanceOfT(Class<T> aClass) {
        try {
             return aClass.newInstance();
         } catch (InstantiationException | IllegalAccessException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
        return null;
     }  
    
}
