/*
 * Created on Mar 23, 2006
 */
package klava.examples.mobility;

import org.mikado.imc.common.IMCException;

import klava.KInteger;
import klava.KString;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.examples.process.EvalProcess;
import klava.examples.process.InOutProcess;
import klava.topology.ClientNode;
import klava.topology.KlavaNode;
import klava.topology.Net;

/**
 * There are two nodes, one logged to the other, via login, and a process
 * performs an in from one node and outs result to the other.
 * 
 * @author Lorenzo Bettini
 * @version $Revision $
 */
public class EvalProcessAtRemote {

    public static void main(String[] args) throws IMCException, KlavaException {
        PhysicalLocality serverLoc = new PhysicalLocality("tcp-127.0.0.1:9999");
        KlavaNode serverNode = new Net(serverLoc);

        /* insert a tuple in the server node */
        Tuple tuple = new Tuple(new KString("foo"), new KInteger(10));
        serverNode.out(tuple);

        /* will automatically log to the server */
        KlavaNode clientNode = new ClientNode(serverLoc);
        
        /* retrieve the physical locality of the client */
        PhysicalLocality clientLoc = clientNode.getPhysical(KlavaNode.self);

        /*
         * the process will be sent to the server by EvalProcess
         */
        InOutProcess inOutProcess = new InOutProcess(new Tuple(new KString(),
                new KInteger()));
        inOutProcess.outDestination = clientLoc;
        clientNode.addNodeProcess(new EvalProcess(inOutProcess, serverLoc));

        /* let's wait for the response at the client */
        Tuple result = new Tuple(new KString(), new KInteger());
        clientNode.in(result);

        System.out.println("result: " + result);
        System.exit(0);
    }

}
