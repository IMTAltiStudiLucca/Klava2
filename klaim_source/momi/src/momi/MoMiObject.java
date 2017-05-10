package momi;

import java.util.Hashtable;

/**
 * Base class for every instance of a MoMi class. The structure of a MoMiObject
 * is as follows:
 * 
 * <pre>
 *  this ----&gt; next (the subpart inherited from the base class
 *      |            that has to be passed to inherited methods)
 *      +----&gt; original_class (the methods inherited as they are, i.e.
 *      |                      with no redefinition, from the super class)
 *      +----&gt; methods (the methods of this object; dynamic binding
 *                      is applied for these methods, so they contain
 *                      the redefined versions).
 * </pre>
 * 
 * @version $Revision: 1.1 $
 * @author $author$
 */
public class MoMiObject extends WithMethods implements Cloneable {
    /**
     * 
     */
    private static final long serialVersionUID = 7660733900785429918L;

    /** part inherited by the parent class */
    protected MoMiObject next;

    /** the class from which this object is instantiated from */
    protected Mixin original_class;

    protected Hashtable<String, MoMiObject> self_table = new Hashtable<String, MoMiObject>();

    protected Hashtable<String, MoMiObject> next_table = new Hashtable<String, MoMiObject>();

    /**
     * methods defined in this object's class, and called from within methods of
     * this object's class: if a method calls another method defined in its own
     * class it will implicitly use this table, where some methods (those for
     * which there has been a name clash) are statically bound to the original
     * implementation.
     */
    protected WithMethods internal_methods;

    /** used together with internal_methods */
    protected Hashtable<String,MoMiObject> internal_self_table = new Hashtable<String,MoMiObject>();

    /**
     * Set the inherited part of this object
     * 
     * @param next:
     *            the inherited sub-object
     */
    public final void set_next(MoMiObject next) {
        this.next = next;
    }

    /**
     * Retrieve the inherited part of this object
     * 
     * @return next the inherited sub-object
     */
    public final MoMiObject get_next() {
        return next;
    }

    public final MoMiObject get_self(String meth) {
        return self_table.get(meth);
    }

    public final MoMiObject get_next(String meth) {
        return next.next_table.get(meth);
    }

    public final MoMiMethod get_internal_method(String meth)
            throws NoSuchMethodException {
        return internal_methods.get_method(meth);
    }

    public final MoMiObject get_internal_self(String meth) {
        return internal_self_table.get(meth);
    }

    /**
     * Retrieve the original implementation of a method
     * 
     * @param name
     *            the name of the method to retrieve
     * 
     * @return the original implementation for this method
     * 
     * @throws NoSuchMethodException
     */
    public final MoMiMethod get_next_method(String name)
            throws NoSuchMethodException {
        return next.original_class.get_method(name);
    }

    /**
     * Set the original class of this object
     * 
     * @param my_class
     *            the original class
     */
    public final void set_original_class(Mixin my_class) {
        original_class = my_class;
    }

    /**
     * Return the original class of this object
     * 
     */
    final Mixin get_original_class() {
        return original_class;
    }

    void update_self(String meth, MoMiObject obj, boolean recursive) {
        if (obj == this && dynamic_name_clash(meth))
            self_table.put(meth, next.get_self(meth));
        else
            self_table.put(meth, obj);

        if (obj == this || dynamic_name_clash(meth))
            internal_self_table.put(meth, this);
        else
            internal_self_table.put(meth, obj);

        if (recursive && next != null) {
            next.update_self(meth, obj, recursive);

            if (dynamic_name_clash(meth)) {
                MoMiObject _next = get_next(meth);
                if (_next != null)
                    next_table.put(meth, _next);
                else
                    next_table.put(meth, next);
            } else
                next_table.put(meth, this);
        } else
            next_table.put(meth, this);
    }

    protected final boolean dynamic_name_clash(String meth) {
        return original_class.name_clashes.contains_method(meth);
    }

    /**
     * Convert this object into a string
     * 
     * @return the string representation of this object
     */
    public String toString() {
        return _toString(0);
    }

    /**
     * Convert this object to a string with indentation
     * 
     * @param indent
     *            the indentation level
     * 
     * @return the string representation of this object
     */
    String _toString(int indent) {
        String indent_str = MoMiPrinter.build_indent(indent);
        String indent_str2 = MoMiPrinter.build_indent(indent + 2);

        return indent_str
                + "object = \n"
                + indent_str2
                + "original class = \n"
                + original_class._toString(indent + 4)
                + "\n"
                + indent_str2
                + "methods =\n"
                + super._toString(indent + 4)
                + "\n"
                + indent_str2
                + (original_class.name_clashes.size() != 0 ? "name clashes =\n"
                        + original_class.name_clashes._toString(indent + 4)
                        + "\n" + indent_str2 : "") + "next ="
                + (next != null ? "\n" + next._toString(indent + 4) : " null");
    }
}