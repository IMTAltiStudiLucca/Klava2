/*
 * Created on Nov 3, 2005
 */
package klava.tests.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All the junit tests.
 * 
 * @author Lorenzo Bettini
 */
public class AllTests {

    public static void main(String[] args) {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for junit");
        //$JUnit-BEGIN$
        suite.addTestSuite(TupleTest.class);
        suite.addTestSuite(EnvironmentTest.class);
        suite.addTestSuite(TupleStateTest.class);
        suite.addTestSuite(ResponseStateTest.class);
        suite.addTestSuite(PropagateLocalityTest.class);
        suite.addTestSuite(MessageStateTest.class);
        suite.addTestSuite(AcceptRegisterNodeTest.class);
        suite.addTestSuite(ForwardRequestTest.class);
        suite.addTestSuite(NodeOperationTest.class);
        suite.addTestSuite(NodeProcessTest.class);
        suite.addTestSuite(NodeCoordinatorTest.class);
        suite.addTestSuite(NetTest.class);
        suite.addTestSuite(NodeTest.class);        
        suite.addTestSuite(NewlocTest.class);
        suite.addTestSuite(ProcessMigrationTest.class);
        //$JUnit-END$
        return suite;
    }

}
