/*
 * Created on Mar 13, 2016
 */
package klava.index_space;

import java.util.Hashtable;

import common.GeneralMethods;
import klava.Tuple;

public class IndexingThread extends Thread 
{
    IndexedAsyncTupleSpace space;
    public IndexingThread(IndexedAsyncTupleSpace space)
    {
        this.space = space;   
    }

    public void run() {
        System.out.println("loop started");
        
        asyncProcessWithAxillaryList();
        
        System.out.println("loop finished");
    }
        
    
    void asyncProcessWithAxillaryList()
    {
        int portion = 0;
               
        int minSizeToNotify = 5000;
        
        while(true)
        {

            //if(space.newTuples)
            if(space.preTupleList.size() > 0)
            {
                Tuple tuple = null;
                space.preTupleListLock.lock();
                tuple = space.preTupleList.poll();
                space.preTupleListLock.unlock();
                
                space.asyncWrite(tuple);                
                portion++;
                

                if(portion >= minSizeToNotify || space.preTupleList.size() == 0)
                {
                    portion = 0;
                    space.dataLock.lock();
                    space.responseIsBack.signalAll();
                    space.dataLock.unlock();
                    
                    System.out.println( " notify + lastTupleIndex " + space.tupleIndex + " - ");
                }
                
            } else
            { 
                try {
                    // with package ??
                    Thread.sleep(1);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    
    
    int packProcessing(long initalIndex, final long packSize)
    {
        int processedTuples = 0;
        Hashtable<Integer, Pair<Tuple, String>> dataToProcess = new Hashtable<Integer, Pair<Tuple, String>>();
        
        synchronized (space.tupleList)
        {
            for(int i = 0; i <packSize; i++)
            {
                Pair<Tuple, String> tuple = space.tupleList.get(initalIndex + i);
                dataToProcess.put(i, tuple);
                       
                processedTuples++;
            }
        }
        
        for(int i = 0; i <packSize; i++)
        {
            Pair<Tuple, String> tuple = dataToProcess.get(i);
            space.multiIndex.add(tuple.getFirst(), initalIndex + i);
        }
        return processedTuples;
    }
    
    
    
    
    void otherVersions()
    {
        long lastTupleIndex = 0;
        int portion = 0;
        boolean everythingIsDone = false;
        
        
        int minimumSize = 1000;
        
        while(true)  //this != null
        {

            //if(space.newTuples)
            if(space.preTupleList.size() > 0)
            {
    
                Tuple tuple = null;
                synchronized (space.preTupleList) {
                    tuple = space.preTupleList.poll();
                }
                
                space.asyncWrite(tuple);
                
                portion++;
                
                
                if(portion >= minimumSize || space.preTupleList.size() == 0)
                {
                    everythingIsDone = false;
                    portion = 0;
                    synchronized (space) 
                    {
                        space.notifyAll();
                    }
                    
                    System.out.println(GeneralMethods.getCurrrentDatetimeAsString() + 
                            "notify + lastTupleIndex " + space.tupleIndex + " - " + lastTupleIndex);
                }
                
                // one by one
//                if(space.tupleIndex <= lastTupleIndex)
//                    continue;
//                
//                synchronized (space.tupleList)
//                {
//                    Pair<Tuple, String> tuple = space.tupleList.get(lastTupleIndex);
//                    space.multiIndex.add(tuple.getFirst(), lastTupleIndex);
//                }
//
//                lastTupleIndex++;
//                portion++;

              // by package
               
//                long packSize = space.tupleIndex > lastTupleIndex + minimumSize ? minimumSize : space.tupleIndex - lastTupleIndex;
//                
//                int processed = packProcessing(lastTupleIndex, packSize);
//                lastTupleIndex += processed;
//                portion += processed;
                
                
              /*  if(lastTupleIndex == space.tupleIndex )  // && space.tupleList.size() != 0
                {
                    space.newTuples = false;
                    everythingIsDone = true;
                    
                    System.out.println("lastTupleIndex" );
                } else
                {
                    space.newTuples = true;
                    everythingIsDone = false;
                }
                
                
                if(portion >= minimumSize*5 || everythingIsDone == true)
                {
                    everythingIsDone = false;
                    portion = 0;
                    synchronized (space) 
                    {
                        space.notifyAll();
                    }
                    
                    System.out.println("notify + lastTupleIndex " + space.tupleIndex + " - " + lastTupleIndex);
                }*/
            } else
            { 
                try {
                    //pakage
                    Thread.sleep(1);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    
    
    
    
    
}