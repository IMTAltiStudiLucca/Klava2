/*
 * Created on 9May,2017
 */
package klaim.localspace;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SynchroObject {
   
    public ReentrantLock lock;
    public Condition condition;
    
    
    public SynchroObject(ReentrantLock lock, Condition condition)
    {
        this.lock = lock;
        this.condition = condition;
    }
    
    public static SynchroObject generateLockWithCondition() {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        return new SynchroObject(lock, condition);
    }
    
    public ReentrantLock getLock() {
        return lock;
    }
    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }
    public Condition getCondition() {
        return condition;
    }
    public void setCondition(Condition condition) {
        this.condition = condition;
    }
    
    
    public void await() throws InterruptedException {
        lock.lock();
        condition.await();
        lock.unlock();
    }
    
    public void signal() throws InterruptedException {
        lock.lock();
        condition.signal();
        lock.unlock();
    }
    
    
    
    
}
