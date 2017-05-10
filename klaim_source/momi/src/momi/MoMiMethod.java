package momi;

import java.util.Stack;


/**
 * Class representing a method in a mixin or a class.  The idea would be
 * that a single instance for every subclass of this class exists in the
 * system.  However this is not ensured by the class (i.e. no Singleton
 * pattern is implemented).  Every method owns a private stack where
 * parameters are pushed before calling the methods, popped from within
 * the method, and where the result value is pushed as a last instruction
 * from within the method.
 *
 * @version $Revision: 1.1 $
 * @author $author$
 */
public class MoMiMethod implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -265210770258071907L;

    /** constant representing a method defined in a class or in a mixin */
    public static final int DEFINED = 0;

    /** constant representing a method redefined in a mixin */
    public static final int REDEFINED = 1;

    /** constant representing a method expected by a mixin */
    public static final int EXPECTED = 2;

    /** constant representing a method in the base class that will
     *  be redefined by a mixin.  Not used in this release */
    public static final int OLD = 3;

    /** constant representing a method inherited from a superclass
     *  by a mixin.  It is assumed that that method was expected by
     *  the mixin */
    public static final int INHERITED = 3;

    /** utility table for translating constants in strings, useful for
     *  debugging */
    protected static final String[] kinds = { "DEFINED", "REDEFINED", "EXPECTED" };

    /** parameter stack for this method */
    protected Stack<Object> stack = new Stack<Object>();

    /** name of this method */
    protected String name;

    /** kind of this method (DEFINED, REDEFINED, etc.) */
    protected int kind;

    /**
     * Creates a new MoMiMethod object.
     *
     * @param name the name of this method
     * @param kind the kind of this method (DEFINED, REDEFINED, etc.)
     */
    public MoMiMethod(String name, int kind) {
        this.name = name;
        this.kind = kind;
    }

    /**
     * Pass an argument to this method.  The argument is pushed on the
     * stack for this method
     *
     * @param arg tha argument to pass to this method
     */
    public void pass_argument(Object arg) {
        stack.push(arg);
    }

    /**
     * Retrieve the result of this method.  The result has been pushed onto
     * the stack by the method itself
     *
     * @return the result of this method
     */
    public Object get_result() {
        return stack.pop();
    }

    /**
     * Invoke this method.  The parameters must have already been passed to
     * the method.  The result, should the method provide one, can be
     * retrieved with get_result().
     *
     * @param self the self pointer to pass to this method.
     *
     * @throws NoSuchMethodException should this method encounter a
     * "message-not-understood" error
     */
    public void invoke(MoMiObject self) throws NoSuchMethodException {
        throw new NoSuchMethodException(name);
    }

    /**
     * Create a string representation of this method
     *
     * @return the string representation of this method
     */
    public String toString() {
        return "(" + getClass().getName() + "-" + kinds[kind] + ")";
    }

     /**
      * Turn the method into a String
      *
      * @param indent the indentation level
      *
      * @return the string representation of this method
      */
     String _toString(int indent) {
        String indent_str = MoMiPrinter.build_indent(indent);

        return indent_str + "(" + getClass().getName() + "-" + kinds[kind] + ")";
    }
}