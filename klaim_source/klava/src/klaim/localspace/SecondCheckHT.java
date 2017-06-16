/*
 * Created on 22May,2017
 */
package klaim.localspace;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import klava.Tuple;

public class SecondCheckHT {
    
    final static int MAX_INCOMMING_LIST_SIZE = 20;
    public class SecondCheckDataHT {
       // eTupleOperation operationType;
        String processName;
        public SynchroObject syncObject;
        LinkedList<Hashtable<Long, LinkedList<Tuple>>> partsToCheck;
        public AtomicLong processState = new AtomicLong(0);
        
        public SecondCheckDataHT(String name) {
            this.processName = name;
            this.syncObject = SynchroObject.generateLockWithCondition();
            this.partsToCheck = new LinkedList<>();
        }
    }  
   
    public Hashtable<Long, LinkedList<Tuple>> incomingTupleList = null;
    public ConcurrentHashMap<String, SecondCheckDataHT> readingProcesses = null;
        
    public SecondCheckHT() {
        incomingTupleList = new Hashtable(MAX_INCOMMING_LIST_SIZE);
        readingProcesses = new ConcurrentHashMap<>();
    }
       
    public void addTuple(Tuple tuple, ArrayList<Long> dataKeys) {
        //incommingTuplesListLock.lock();
        
        boolean reachedMaxSize = false;
        
        Hashtable<Long, LinkedList<Tuple>> newIncomingTupleList = new Hashtable<>(MAX_INCOMMING_LIST_SIZE);
//        List<Tuple> oldList = null;
        synchronized (incomingTupleList) 
        {
    //        incomingTupleList.add(tuple);
            for(int i=0; i<dataKeys.size(); i++) {
                Long dataKey = dataKeys.get(i);
                incomingTupleList.putIfAbsent(dataKey, new LinkedList<Tuple>());
                incomingTupleList.get(dataKey).add(tuple);
            }

        //    System.out.println(incomingTupleList.size());
            reachedMaxSize = incomingTupleList.size() >= MAX_INCOMMING_LIST_SIZE;
            if(reachedMaxSize) {
        //        oldList = incomingTupleList;
                incomingTupleList = newIncomingTupleList;
            }
        }
             
        if(reachedMaxSize)
        {
            Object[] readingProcessesNames = readingProcesses.keySet().toArray();
            for(int i=0; i<readingProcessesNames.length; i++) 
            {
                String readerKey = (String) readingProcessesNames[i];
                SecondCheckDataHT scData = readingProcesses.get(readerKey);
                if(scData != null) {
                    synchronized (scData.partsToCheck) 
                    {
                        scData.partsToCheck.add(newIncomingTupleList);                              
                    }
                }
            }
            //System.out.println(Thread.currentThread().getName() + " NEW PART");
        }
    }
    
    public SecondCheckDataHT subscribeProcess(String name) {
               
        SecondCheckDataHT scData = new SecondCheckDataHT(name);
        synchronized (readingProcesses) {
            synchronized (scData.partsToCheck) {
                scData.partsToCheck.add(incomingTupleList);
            }
            readingProcesses.put(name, scData);
        }
        return scData;
    }
    
    public void unSubscribeProcess(String name) {
        synchronized (readingProcesses) {
            readingProcesses.remove(name);
        }
       
        return;
    }    
    
    public static boolean check(Hashtable<Long, LinkedList<Tuple>> table, long hashedValue, Tuple template, boolean remove) {
        
        boolean found = false;
        if(table.containsKey(hashedValue)) {
            LinkedList<Tuple> list = table.get(hashedValue);
            synchronized (list)
            {              
                for(Iterator<Tuple> iterator = list.iterator(); iterator.hasNext();)
                {
                    Tuple currentTuple = iterator.next();
                    if(remove) {
                        found = currentTuple.removed.compareAndSet(false, true);
                        if (found) {
                            template.copy(currentTuple);
                            found = true;
                            iterator.remove();
                            break;
                        } else {
                            iterator.remove();
                        }
                    } else {
                        if(!currentTuple.removed.get()) {
                            template.copy(currentTuple);
                            found = true;
                            break;
                        } else {
                            iterator.remove();
                        }
                    }    
                    
                }
            }
        }
        
       
        return found;
    }
    
    public static boolean checkIncommingTuples(SecondCheckDataHT data, long hashedValue, Tuple template, boolean remove) {
               
        synchronized (data.partsToCheck) 
        {
     //       System.out.println(Thread.currentThread().getName() + " check_part " + template + data.partsToCheck);
     //       System.out.println(Thread.currentThread().getName() + " check_part " + data.partsToCheck.size());
            
            Iterator<Hashtable<Long, LinkedList<Tuple>>> iter = data.partsToCheck.iterator();
            while (iter.hasNext()) {
                Hashtable<Long, LinkedList<Tuple>> table = iter.next();
                synchronized (table) 
                {
       //             System.out.println(Thread.currentThread().getName() + " check_part #"  + hashedValue + " "  + template + " " + table);

                    if(table!=null && check(table, hashedValue, template, remove)) {
                        // remove all links to arrays of tuples
                        data.partsToCheck.clear();
             //           System.out.println(Thread.currentThread().getName() + " FOUND " + template.toString());
                        return true;
                    }
                    
                    // add back
                    if(table.size() >= MAX_INCOMMING_LIST_SIZE) {
                        iter.remove();
          //              System.out.println(Thread.currentThread().getName() + " remove  ");
                    }
                }
            }
        }

     //   System.out.println(Thread.currentThread().getName() + " not found " + template.toString());
        return false;
    }    
        
    public void notifyProcesses() {
        // add it to processes to check        
        
        Object[] readingProcessesNames = readingProcesses.keySet().toArray();
        for(int i=0; i<readingProcessesNames.length; i++) 
        {
            String readerKey = (String) readingProcessesNames[i];
            SecondCheckDataHT scData = readingProcesses.get(readerKey);
            if(scData!= null) {
    //            System.out.println(Thread.currentThread().getName() + " notify " + readerKey);
                
                scData.syncObject.lock.lock();
                scData.processState.incrementAndGet();
                scData.syncObject.condition.signal();
                scData.syncObject.lock.unlock();
                //scData.syncObject.signal();
            }   
        }
    }

}
