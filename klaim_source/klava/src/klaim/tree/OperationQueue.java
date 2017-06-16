/*
 * Created on 16Mar.,2017
 */
package klaim.tree;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.util.Pair;
import klava.Tuple;
import klava.TupleSpace;
import sun.misc.Queue;

public class OperationQueue 
{
    class OperationElement
    {
        Tuple argument;
        Condition accessLockCondition = null;
        
        public OperationElement(Tuple argument, Condition accessLockCondition)
        {
            this.argument = argument;
            this.accessLockCondition = accessLockCondition;
        }
    }
    
    Lock operationPlacementLock = new ReentrantLock();
    eOperations lastOperation = null;
    
    
    TupleSpace space = null;
    public enum eOperations {WRITE, READ_BLOCK, READ_NON_BLOCK, TAKE_BLOCK, TAKE_NON_BLOCK}
    Queue<Pair<eOperations, OperationElement>> queue = null;
    
    
    public OperationQueue()
    {
        queue = new Queue<>();
    }

    public boolean doOperation(eOperations type, Tuple argument) throws InterruptedException
    {
        boolean bResult = false;
            

        if (lastOperation != null)
        {
            Lock accessLock = new ReentrantLock();
            Condition accessLockCondition = accessLock.newCondition();
            
            queue.enqueue(new Pair<>(type, new OperationElement(argument, accessLockCondition)));
            operationPlacementLock.unlock();
            
            accessLock.lock();
            accessLockCondition.wait();
            accessLock.unlock();
            
            
            bResult = performOperation(type, argument);
        } else
        {
            operationPlacementLock.lock();
            lastOperation = type;
            operationPlacementLock.unlock();
            
            bResult = performOperation(type, argument);
            
            operationPlacementLock.lock();
            lastOperation = null;
            resumeOperation();
            operationPlacementLock.unlock();
        }
        
        return bResult;
    }
    
    void resumeOperation() throws InterruptedException
    {
        Pair<eOperations, OperationElement> element = null;
        operationPlacementLock.lock();
        if(!queue.isEmpty())
        {
            element = queue.dequeue();
            element.getValue().notify();
        }
        operationPlacementLock.unlock();
        
        
        
    }
    
    
    private boolean performOperation(eOperations operationType, Tuple argument) throws InterruptedException
    {
        boolean result = false;
        switch(operationType)
        {
            case WRITE: 
                space.out(argument);
                result = true;
                break;
            case READ_NON_BLOCK: 
                result = space.read_nb(argument);
                break;
            case READ_BLOCK: 
                try {
                    result = space.read(argument);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
                break; 
            case TAKE_NON_BLOCK: 
                result = space.in_nb(argument);
                break;    
                
            case TAKE_BLOCK: 
                try {
                    result = space.in(argument);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
                break; 
        }
        
        return result;
    }
    
    
/*    public void checkInAccessPermission(boolean isOperationOfModification) {
        // if false - tell to block
        boolean result = false;
        accessLock.lock();
        
        if (accessMode == eAccessModes.READ_MODE) {
            result = !isOperationOfModification ? true : false;
        } 
        else if(accessMode == eAccessModes.CHANGE_MODE) {
            result = false;
        } else if(accessMode == eAccessModes.NONE) {
            result = true;
            accessMode = isOperationOfModification ? eAccessModes.CHANGE_MODE : eAccessModes.READ_MODE;
        }
        
        if(!result == false) {
            // block
            
        }
        
        accessLock.unlock();
        
        if (!result)
        {
            if (isOperationOfModification) {
                changeLock.lock();
                try {
                    changeLockCondition.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                changeLock.lock();
            }
            
            if (!isOperationOfModification) {
                readLock.lock();
                try {
                    readLockCondition.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                readLock.lock();
            }
        }
     }
    
    public void checkOutAccessPermission() {
        
        accessLock.lock();
        
        
        accessMode = eAccessModes.NONE; // ??? if raderCounter == 0
        
        accessLock.unlock();
    }*/
}
