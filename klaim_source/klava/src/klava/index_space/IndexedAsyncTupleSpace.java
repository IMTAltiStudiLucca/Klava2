/*
 * Created on Mar 7, 2016
 */
package klava.index_space;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
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
 * +1. If we have identical tuples what should we return
 */


/**
 * @author Vitaly Buravlev
 * @version $Revision$
 */
public class IndexedAsyncTupleSpace extends EventGeneratorAdapter implements
    TupleItem, Serializable, TupleSpace
{   
    // current number of tuple
    Long tupleIndex = 0L;
    final Lock tupleIndexLock = new ReentrantLock();
    
    // collection of tuples
    Hashtable<Long, Pair<Tuple, String>> tupleList = null;
    
    // structure of indexes
    MultiIndex multiIndex;
    final Lock dataLock = new ReentrantLock();
    final Condition responseIsBack = dataLock.newCondition();
    
    
    final Lock preTupleListLock = new ReentrantLock();
    protected LinkedList<Tuple> preTupleList = null;  //ConcurrentLinkedQueue   LinkedList
    boolean newTuples = false;
    IndexingThread indexingThread;
    
    
    ConcurrentLinkedQueue<TupleRequest> requestToTupleSpace;
    
    public IndexedAsyncTupleSpace()
    {
        tupleList = new Hashtable<Long, Pair<Tuple, String>>();
        multiIndex = new MultiIndex();
        
        //requestToTupleSpace = new ConcurrentLinkedQueue<TupleRequest>();
        
        
        preTupleList = new LinkedList <Tuple>();
        indexingThread = new IndexingThread(this);
        indexingThread.setPriority(Thread.NORM_PRIORITY);
        indexingThread.start();
    }
    
    protected void finalize() throws Throwable 
    {
        System.out.println("FINALIZE");
        super.finalize();
        indexingThread.stop();
    }
    
    public void out(Tuple tuple)
    {
        preTupleListLock.lock();
        preTupleList.add(tuple);
        preTupleListLock.unlock();
      }
    
    /**
     * write tuple to tuple space
     * @param tuple
     */
    public void asyncWrite(Tuple tuple)
    {
        // get next tupleID
        Long tupleIndex = getNextTupleIndex();
        // just put the tuple into the list
        Pair<Tuple, String> pair = new Pair<Tuple, String>(tuple, IndexedTupleSpace.getStructure(tuple));
        tupleList.put(tupleIndex, pair);
        // update index
        multiIndex.add(tuple, tupleIndex);
    }
    
    
    public void updateTupleIndex(Tuple tuple, long tupleID)
    {
        // get next tupleID
        Long tupleIndex = getNextTupleIndex();
        // just put the tuple into the list
        Pair<Tuple, String> pair = new Pair<Tuple, String>(tuple, IndexedTupleSpace.getStructure(tuple));
        tupleList.put(tupleIndex, pair);
        // update index
        multiIndex.add(tuple, tupleIndex);
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
        dataLock.lock();
        Set<Long> tupleIDList = filterByTemplate(template);
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
            
            String item = MultiIndex.getHashCode(template.getItem(indexID).toString()); //String.valueOf( template.getItem(indexID).toString().hashCode() );  // (String) template.getItem(indexID);
            if(item != null)
            {
                String fieldValue = item;
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
        if(tupleIDList == null || template == null)
            return null;
        // obtain structure of template
        String templateStructure = IndexedTupleSpace.getStructure(template);
        int templateLength = template.length();
        
        // check all tuples
        Iterator<Long> iterator = tupleIDList.iterator(); 
        //for(int i = 0; i < tupleIDList.size(); i++)
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
                if(IndexedTupleSpace.checkTupleByValue(pair.getFirst(), template))
                    return new Pair<Long, Tuple>(tupleID, pair.getFirst());
                else 
                    continue;
            }            
        }
        return null;
    }
    
    /**
     * get next tuple number
     * @return
     */
    Long getNextTupleIndex()
    {
        Long nextTupleIndex= 0L;
        tupleIndexLock.lock();
        nextTupleIndex = tupleIndex++;
        tupleIndexLock.unlock();
        return nextTupleIndex;
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
    public void clear()  
    {
        dataLock.lock();
        tupleIndex = 0L;
        tupleList.clear();
        
        // structure of indexes
        MultiIndex multiIndex = new MultiIndex();
        final Lock dataLock = new ReentrantLock();
        preTupleList.clear();
        
        indexingThread.stop();
        
        indexingThread = new IndexingThread(this);
        indexingThread.setPriority(Thread.NORM_PRIORITY);
        indexingThread.start();
        
        return;
    }

}
