/*
 * Created on Jan 28, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.UnMarshaler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Contains the processes that wait for notifications. The processes are
 * identified by their name.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class WaitingForNotification {
    /**
     * The structure containing objects waiting for notifications. The key the
     * identifier of the object.
     */
    Map<String, SendAndWaitForNotification> waiting = 
    	Collections.synchronizedMap(new HashMap<String, SendAndWaitForNotification>());

    /**
     * Inserts an object in the waiting structure.
     *
     * @param sendAndWaitForNotification
     */
    public void goWaiting(SendAndWaitForNotification sendAndWaitForNotification) {
        waiting.put(sendAndWaitForNotification.getId(),
            sendAndWaitForNotification);
    }

    /**
     * Notifies the object that a notification is ready for it to be read.
     * 
     * @param id The identifier of the object to notify.
     * @param unMarshaler It can be used to read additional information.
     *
     * @return <tt>false</tt> if there's no object with the specified
     *         identifier.
     *
     * @throws ProtocolException
     */
    public synchronized boolean wakeUp(String id, UnMarshaler unMarshaler) throws ProtocolException {
        SendAndWaitForNotification sendAndWaitForNotification = 
        	waiting.remove(id);

        if (sendAndWaitForNotification == null) {
            return false;
        }

        sendAndWaitForNotification.wakeup(unMarshaler);

        return true;
    }
    
    /**
     * Notifies the exception to all the waiting objects.
     * 
     * @param protocolException
     */
    public synchronized void notifyException(ProtocolException protocolException) {
    	Iterator<SendAndWaitForNotification> waitings =
    		waiting.values().iterator();
    	
    	while (waitings.hasNext()) {
    		waitings.next().notifyException(protocolException);
    	}
    }
}
