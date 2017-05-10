/*
 * Created on Jan 18, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.mobility.JavaMigratingCode;
import org.mikado.imc.protocols.ProtocolStack;

/**
 * A node process that can execute standard actions.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public abstract class NodeProcess extends JavaMigratingCode implements Closeable {
    /** The proxy for the node. */
    protected transient NodeProcessProxy nodeProcessProxy;

    /**
     * This is used to generate unique process name when no explicit name is
     * specified.
     */
    protected static int nextId = 0;

    /**
     * The default name of the process is created by using the process class
     * name and an unique (incremented) number.
     * 
     * Creates a new NodeCoordinator object.
     */
    public NodeProcess() {
        setName(getClass().getName() + "-" + getNextId());
    }

    /**
     * Creates a new NodeCoordinator object.
     * 
     * @param name
     *            The name of this process
     */
    public NodeProcess(String name) {
        super(name);
    }

    /**
     * @return Returns the NodeProcessProxy.
     */
    protected NodeProcessProxy getNodeProcessProxy() {
        return nodeProcessProxy;
    }

    /**
     * @param nodeProcessProxy
     *            The NodeProcessProxy to set.
     * @throws IMCException
     */
    protected void setNodeProcessProxy(NodeProcessProxy nodeProcessProxy)
            throws IMCException {
        this.nodeProcessProxy = nodeProcessProxy;
    }

    /**
     * This is the method invoked to actually start the process. Derived classes
     * must provide an implementation for it.
     * 
     * @throws IMCException
     */
    public abstract void execute() throws IMCException;

    /**
     * Method used to start the process. This will implicitly call execute().
     */
    public final void run() {
        try {
            preExecute();
            execute();
            postExecute();
        } catch (IMCException e) {
            e.printStackTrace();
        } finally {
            /* finally remove me from the list of running processes */
            if (nodeProcessProxy != null)
                nodeProcessProxy.removeNodeProcess(this);

            System.out.println("terminated " + getName());
        }
    }

    /**
     * Called before execute. The default implementation does nothing.
     * 
     * @throws IMCException
     */
    public void preExecute() throws IMCException {

    }

    /**
     * Called after execute. The default implementation does nothing.
     * 
     * @throws IMCException
     */
    public void postExecute() throws IMCException {

    }

    /**
     * @return The EventManager
     */
    public EventManager getEventManager() {
        return nodeProcessProxy.getEventManager();
    }

    /**
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        nodeProcessProxy.setEventManager(eventManager);
    }

    /**
     * @param nodeLocation
     * @return the ProtocolStack
     */
    public ProtocolStack getNodeStack(NodeLocation nodeLocation) {
        return nodeProcessProxy.getNodeStack(nodeLocation);
    }

    /**
     * @param nodeLocation
     * @return whether the NodeLocation is local
     */
    public boolean isLocal(NodeLocation nodeLocation) {
        return nodeProcessProxy.isLocal(nodeLocation);
    }

    /**
     * @param nodeProcess
     * @throws IMCException
     */
    public void addNodeProcess(NodeProcess nodeProcess) throws IMCException {
        nodeProcessProxy.addNodeProcess(nodeProcess);
    }

    /**
     * @param nodeProcess
     * @throws InterruptedException
     * @throws IMCException
     */
    public void executeNodeProcess(NodeProcess nodeProcess)
            throws InterruptedException, IMCException {
        nodeProcessProxy.executeNodeProcess(nodeProcess);
    }

    /**
     * This is called to tell the process that it should terminate.
     * 
     * Subclasses can override it in order to make the process terminate somehow
     * (e.g., by closing a stream used by the process, or a socket, etc.).
     * 
     * The default implementation does nothing.
     * 
     * @throws IMCException
     */
    public void close() throws IMCException {

    }

    /**
     * Returns the next id (incremented)
     * 
     * @return Returns the nextId.
     */
    synchronized protected static int getNextId() {
        return ++nextId;
    }
    
    /**
     * Prints by prefixing the process name.
     * 
     * This will be printed on the (possibly redirected) standard output
     * of the node.
     * 
     * @param s The string to print
     */
    protected void SystemOutPrint(String s) {
        nodeProcessProxy.SystemOutPrint(getName() + ": " + s);
    }
    
    /**
     * Prints by prefixing the process name.
     * 
     * This will be printed on the (possibly redirected) standard error
     * of the node.
     * 
     * @param s The string to print
     */
    protected void SystemErrPrint(String s) {
        nodeProcessProxy.SystemErrPrint(getName() + ": " + s);
    }
}
