/*
 * Created on 26 Oct 2016
 */
package klaim.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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

public class TupleSpaceTree extends EventGeneratorAdapter implements
        TupleItem, Serializable, TupleSpace
{
    public static final ArrayList<String> acceptedFieldDataTypeList = setAcceptedFieldDataType();
    
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    ConcurrentHashMap<String, RedBlackTree<Tuple>> tupleTable = new ConcurrentHashMap<>();
    
    // settings
    Hashtable<String, List<Object>> settingsTable = new Hashtable<>();
    
    AtomicLong tupleSpaceState = new AtomicLong(0);
    
    public void out(Tuple tuple) {
             
        List<Object> patternCollection = settingsTable.get(tuple.getTupleType());
        for(int i=0; i<patternCollection.size(); i++)
        {
            Boolean[]  pattern = (Boolean[])patternCollection.get(i);
            if(pattern == null)
                continue; 
            String treeKey = TupleSpaceHashtableCS.getTableKey(tuple.getTupleType(), pattern);
            tupleTable.putIfAbsent(treeKey, new RedBlackTree<Tuple>());
            
            RedBlackTree<Tuple> smallTree = tupleTable.get(treeKey);
            if(smallTree.comparator == null)
                smallTree.comparator = new TupleComparator(pattern);
            
            synchronized (smallTree) {
                smallTree.insert(tuple);
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

    private boolean findTuple(Tuple template, String patternKey, boolean remove) {
        boolean result = false;
        
        if(tupleTable.containsKey(patternKey)) {
            RedBlackTree<Tuple> smallTree = tupleTable.get(patternKey);
            
            RedBlackNode<Tuple> data = null;
            synchronized (smallTree) {
                data = smallTree.search(template);

                if (data != null) {
                    Tuple currentTuple = data.key;
        
                    if(remove) {
                        result = currentTuple.removed.compareAndSet(false, true);
                        if (result) {
                            template.copy(currentTuple);
                            smallTree.remove(data);  
                            result = true;
                        } else {
                            smallTree.remove(data);  
                        }
                    } else {
                        if(!currentTuple.removed.get()) {
                            template.copy(currentTuple);
                            result = true;
                        } else
                            smallTree.remove(data);  
                    }
                }
            }
        }
            
        return result;
    }
    
    private boolean searchForTuple(Tuple template, boolean remove, boolean blocking)
    {
        String treeKey = TupleSpaceHashtableCS.getTableKey(template);
        boolean result = false;
        while(!result)
        {
            long oldState = tupleSpaceState.get();
            result = findTuple(template, treeKey, remove);
            
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
                 //   System.err.println("!!!!!!!");
                }
                dataLock.unlock();
                
//                try {
//                    dataLock.lock();
//                    responseIsBack.await();
//                    dataLock.unlock();
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
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
