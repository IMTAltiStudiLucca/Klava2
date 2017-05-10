/*
 * Created on Mar 15, 2006
 */
package klava.tests.junit.gui;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for Gui tests
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class AllGuiTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for junit.gui");
        //$JUnit-BEGIN$
        suite.addTestSuite(TupleSpaceListTest.class);
        suite.addTestSuite(TupleSpaceKeyboardTest.class);
        suite.addTestSuite(TupleSpaceScreenTest.class);
        suite.addTestSuite(TupleSpaceButtonTest.class);
        //$JUnit-END$
        return suite;
    }

}
