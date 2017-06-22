/*
 * Created on 23 Aug 2016
 */
package klava.new_communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import klava.Tuple;

public class TuplePack implements Serializable  
{
    public enum eTupleOperation {READ, IN, OUT, EVAL, TUPLEBACK, TUPLEABSENT, OUT_REPL ,READ_REPL, IN_REPL, ASK_IN_OWNER, ASK_READ_OWNER, DELETE,
        OPERATION_COMPLETED};

    // type of the operation
    public eTupleOperation operation;
    
    // unique identifier of the operation
    public long operationID;
    
    // tuple or template
    public Tuple tuple;
    
    // blocking operation or not
    public boolean blocking = false;
    public long timeout = -1;

    // return address
    IPAddress lastSenderIPAddress;
    
    // if there are more than 1 participants
    //IPAddress firstSenderIPAddress;   
    
    private void zeroInit()
    {
        this.operation = null;
        this.tuple = null;
        this.blocking = false;
        this.timeout = 0;
        this.lastSenderIPAddress = null;
        this.operationID = -1;
    }
        
    public TuplePack(eTupleOperation operation, Tuple tuple,  boolean blocking, long timeout)
    {
        this.operation = operation;
        this.tuple = tuple;
        this.blocking = blocking;
    }
    
    public TuplePack(eTupleOperation operation, long operationID, IPAddress senderAddress)
    {
        zeroInit();
        this.operation = operation;
        this.operationID = operationID;
        this.lastSenderIPAddress = senderAddress;
    }
    
    public static byte[] serializeObject(Object obj)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);   
            out.writeObject(obj);
            byte[] yourBytes = bos.toByteArray();
            return yourBytes;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
             try {
                bos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static byte[] serializeObject(Object obj, byte[] delimeter)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);   
            out.writeObject(obj);
            byte[] yourBytes = bos.toByteArray();
            return yourBytes;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
             try {
                bos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static Object deserializeObject(byte[] initialByteArray)
    {
        //if(initialByteArray.length < 820)
        //    System.out.println("LESS " + initialByteArray.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(initialByteArray);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object obj = in.readObject(); 
            return obj;
        } catch (Exception exc) {
            // TODO Auto-generated catch block
            exc.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
