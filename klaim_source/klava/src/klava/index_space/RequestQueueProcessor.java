/*
 * Created on 20 Apr 2016
 */
package klava.index_space;


import java.util.Hashtable;
import java.util.UUID;

public class RequestQueueProcessor 
{
    Hashtable<UUID, Object> lockList = null;
    RequestThread thread;
    
    public Object processorNotifier = new Object();
    boolean notificationFlag = false;
    
    public RequestQueueProcessor()
    {
       lockList = new Hashtable<UUID, Object>();
       thread = new RequestThread(processorNotifier);
       thread.start();
    }
   
   
   
    public static UUID getKey()
    {
        UUID key = UUID.randomUUID();
        return key;
    }
   
    /*
     * if the lock was taken first time - it means nothing
     * not the first time - it means that it means that something was done and process can have a rest))
     */
    public Object getLock(UUID key)
    {
        Object lock = lockList.get(key); 
        if(lock == null)
        {
            System.out.println("new lock " + key);
            lock = new Object();
            lockList.put(key, lock);
        }
        return lock;
    }
   
    public void cancelFollowing(UUID key)
    {
        lockList.remove(key);
    }
   
    
    
    class RequestThread extends Thread
    {
        Object notifier;

        public RequestThread(Object _notifier)
        {
            notifier = _notifier;
        }
        
        public void run()
        {  

//            while(true)
//            {
//                
//                Set<UUID> keys = lockList.keySet();
//                for(UUID key: keys)
//                {
//                    Object lock = lockList.get(key);
//                    synchronized (lock) {
//                        lock.notify();
////                        try {
////                            Thread.sleep(1);
////                        } catch (InterruptedException e) {
////                            // TODO Auto-generated catch block
////                            e.printStackTrace();
////                        }
//                    }
//                }
//                
//                if(notificationFlag)
//                {
//                    notificationFlag = false;
//                   // continue;
//                }
//                
//                try {
//                  Thread.sleep(2);
//              } catch (InterruptedException e) {
//                  // TODO Auto-generated catch block
//                  e.printStackTrace();
//              }
//            }
        }
        
        
    }
   
}
