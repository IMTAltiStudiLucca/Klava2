/*
 * Created on 30 Oct 2016
 */
package klaim.localspace;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mikado.imc.events.EventManager;

import klava.KlavaException;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpace;
import klava.new_communication.ProcessTuplePackRE;

public class SeparableTupleSpace<T extends TupleSpace> implements
TupleItem, Serializable, TupleSpace
{
    public static final String ALL = "all";
    Class<T> tupleSpaceClass;
    HashMap<String, T> table = new HashMap<String, T>();
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    // settings
    Hashtable<String, List<Object>> settingsTable = new Hashtable<>();
    
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
    
    protected String getSeparator(Tuple tuple)
    {   
        return tuple.getTupleType();
    }

    @Override
    public void out(Tuple tuple) {
 
        String tupleType = getSeparator(tuple);
        
        dataLock.lock();
        boolean contains = table.containsKey(tupleType);
        if(!contains)
        {
            T space = getInstanceOfT(tupleSpaceClass);
            space.setSettings(settingsTable);
            space.out(tuple);
            table.put(tupleType, space);
        }
        dataLock.unlock();
        if(contains)
        {
            table.get(tupleType).out(tuple);
        }  
    }

    @Override
    public boolean read(Tuple template) throws InterruptedException {
        return getTuple(template, true, false);    
    }

    @Override
    public boolean in(Tuple template) throws InterruptedException {
        return getTuple(template, true, true);    
    }

    @Override
    public boolean read_nb(Tuple template) throws InterruptedException {
        return getTuple(template, false, false); 
    }

    @Override
    public boolean in_nb(Tuple template) throws InterruptedException {
        return getTuple(template, false, true); 
    }
    
    public boolean getTuple(Tuple template, boolean isBlocking, boolean remove) throws InterruptedException {
        String separationKey = getSeparator(template);
        
        boolean result = false;
        while(!result)
        {

            // if it is necessary to check all partitions
            if(separationKey.equals(ALL))
                checkAllPartitions(template, isBlocking, remove);
            else {
                if(table.containsKey(separationKey))
                    result = ProcessTuplePackRE.doReadOrIN(table.get(separationKey), template, isBlocking, remove);
            }
            if (!isBlocking)
                return result;
            if(!result) {
                dataLock.lock();
                responseIsBack.await();
                dataLock.unlock();     
            }

        }
        return result;   
    }

    private boolean checkAllPartitions(Tuple template, boolean isBlocking, boolean remove) throws InterruptedException {
        Set<String> keys = table.keySet();
        Iterator<String> it = keys.iterator();
        
        boolean result = false;
        while(it.hasNext()) {
            result = ProcessTuplePackRE.doReadOrIN(table.get(it.next()), template, isBlocking, remove);
            if(result)
                break;
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
    
    @Override
    public void clear()  
    {
        dataLock.lock();
        
        Set<String> keys = table.keySet();
            
        //Obtaining iterator over set entries
        Iterator<String> itr = keys.iterator();
     
        // iterating on the key list
        while (itr.hasNext()) { 
           // Getting Key
           String key = itr.next();
           table.get(key).clear();
        } 
   
        table.clear();
        
        dataLock.unlock();
        return;
    }

    
}
