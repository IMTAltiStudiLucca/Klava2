package momi;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A container of methods' table. It is used as the base class for Mixin and
 * MoMiClass and also for MoMiObject.
 * 
 * @version $Revision: 1.1 $
 * @author $author$
 */
public class WithMethods implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6987144968366923902L;

    /**
     * The table of methods. The key is the name of the method and the value is
     * a MoMiMethod object
     * 
     * @see momi.MoMiMethod
     */
    protected Hashtable<String,MoMiMethod> methods = new Hashtable<String,MoMiMethod>();

    public WithMethods() {
    }

    public WithMethods(Hashtable<String,MoMiMethod> methods) {
        this.methods = methods;
    }

    /**
     * Clone the table of methods. This is a shallow copy, so the MoMiMethod
     * objects are not cloned.
     * 
     * @return a (shallow) copy of the method table
     */
    @SuppressWarnings("unchecked")
    public final Hashtable<String,MoMiMethod> clone_methods() {
        return (Hashtable<String,MoMiMethod>) methods.clone();
    }

    public final int size() {
        return methods.size();
    }

    /**
     * Return the method table
     * 
     * @return the method table
     */
    public final Hashtable<String,MoMiMethod> get_methods() {
        return methods;
    }

    /**
     * Set the method table
     * 
     * @param methods
     *            the new method table
     */
    public final void set_methods(Hashtable<String,MoMiMethod> methods) {
        this.methods = methods;
    }

    /**
     * Set a method in the method table with a specific name
     * 
     * @param name
     *            the name of the method
     * @param meth
     *            the MoMiMethod object representing the method
     */
    public final void set_method(String name, MoMiMethod meth) {
        methods.put(name, meth);
    }

    /**
     * Retrieve a method given its name
     * 
     * @param name
     *            the name of the method to retrieve
     * 
     * @return the MoMiMethod object representing the requested method
     * 
     * @throws NoSuchMethodException
     *             if the requested method is not found
     */
    public final MoMiMethod get_method(String name)
            throws NoSuchMethodException {
        MoMiMethod method = methods.get(name);

        if (method == null) {
            throw new NoSuchMethodException(name);
        }

        return method;
    }

    /**
     * Retrieve a method given its name
     * 
     * @param name
     *            the name of the method to retrieve
     * 
     * @return the MoMiMethod object representing the requested method or null
     *         if it is not found
     */
    final MoMiMethod retrieve_method(String name) {
        MoMiMethod method = methods.get(name);

        return method;
    }

    /**
     * Test the presence of a method
     * 
     * @param name
     *            the name of the method to look for
     * 
     * @return true if the method is found, false otherwise
     */
    public final boolean contains_method(String name) {
        return methods.containsKey(name);
    }

    /**
     * Turn the method table into a String
     * 
     * @return the string representation of this method table
     */
    public String toString() {
        return _with_methods_string() + " = \n" + _toString(2);
    }

    /**
     * Return a string denoting the kind of this method table (e.g. class or
     * mixin)
     * 
     * @return the string denoting the kind of this method table
     */
    protected String _with_methods_string() {
        return "";
    }

    /**
     * Turn the method table into a String
     * 
     * @param indent
     *            the indentation level
     * 
     * @return the string representation of this method table
     */
    String _toString(int indent) {
        StringBuffer buffer = new StringBuffer();

        Enumeration<MoMiMethod> meths = methods.elements();
        while (meths.hasMoreElements()) {
            buffer.append(meths.nextElement()._toString(indent));
            if (meths.hasMoreElements())
                buffer.append('\n');
        }

        return buffer.toString();
    }
}