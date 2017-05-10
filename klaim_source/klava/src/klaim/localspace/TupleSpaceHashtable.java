/*
 * Created on 26 Oct 2016
 */
package klaim.localspace;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mikado.imc.events.EventGeneratorAdapter;

import klava.KlavaException;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpace;
import klava.index_space.IndexedTupleSpace;
import klava.index_space.MultiIndex;

public class TupleSpaceHashtable extends EventGeneratorAdapter implements
        TupleItem, Serializable, TupleSpace
{
    public static final ArrayList<String> acceptedFieldDataTypeList = setAcceptedFieldDataType();
    
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    Hashtable<Long, ArrayList<Tuple>> table = new Hashtable<Long, ArrayList<Tuple>>();
    
    // settings
    Hashtable<String, Boolean[]> settingsTable = new Hashtable<String, Boolean[]>();
    
    public void out(Tuple tuple) {
        
        Long hashtableKey = getHashcode(tuple); 
        
        // put data into the hashtable
        dataLock.lock();
        if (table.containsKey(hashtableKey))
        {
            ArrayList<Tuple> array = table.get(hashtableKey);
            array.add(tuple);
        }
        else {
            ArrayList<Tuple> array = new ArrayList<Tuple>();
            array.add(tuple);
            table.put(hashtableKey, array);
        }        
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

    private boolean findTuple(Tuple template, Long hashtableKey,
            boolean remove) {
        boolean result = false;
        ArrayList<Tuple> array = table.get(hashtableKey);
        // check type
        for(int i=0; i<array.size(); i++)
        {
            Tuple tuple = array.get(i);
            try {
                if(IndexedTupleSpace.checkTupleByValue(tuple, template))
                {
                    template.copy(tuple);
                    // if it is required to remove the tuple
                    if(remove)
                        array.remove(i);
                    result = true;
                    break;
                }
            } catch (IOException e) {
                result = false;
                e.printStackTrace();
            }
        }
        return result;
    }
    
    private boolean searchForTuple(Tuple template, boolean remove, boolean blocking)
    {
        Long hashtableKey = getHashcode(template);
        boolean result = false;
        while(!result)
        {
            dataLock.lock();
            if (table.containsKey(hashtableKey))
            {
                result = findTuple(template, hashtableKey, remove);
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
            
           //if(!blocking)
           //     break;
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
    
    public static Integer getHashCode(String str)
    {
        if(str == null)
            return null;
        else
            return str.hashCode();
    }
    
    private Long getHashcode(Tuple tuple) {
        if(tuple == null)
            return null;
        StringBuilder buider = new StringBuilder();
        Boolean[] settings = settingsTable.get(tuple.getTupleType());
        if(settings == null)
            System.out.println(tuple.getTupleType());
        for(int i=0; i<tuple.length(); i++)
        {
            if(settings[i] != true)
                continue;
            boolean acceptableType = acceptedFieldDataTypeList.contains(tuple.getItem(i).getClass().getName());
            if (acceptableType)
            {
                buider.append(tuple.getItem(i).toString() + "|");
            }
        }
        Long hashtableKey = MultiIndex.getHashCode(buider.toString());
        return hashtableKey;
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



    public void setSettings(Hashtable<String, Boolean[]> settings) {
        this.settingsTable = settings;
        
    }
    

}
