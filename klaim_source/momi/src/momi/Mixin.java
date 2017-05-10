package momi;

import java.util.Enumeration;

/**
 * The class that represents a mixin.  It is intended both as a repository
 * of methods defined/redefined/expected by the mixin, and as a generator
 * for new classes and objects.  It is also used to represent a (new)
 * derived class formed by applying a mixin (stored in "original_mixin")
 * to a parent class (stored in "original_class").
 *
 * @version $Revision: 1.1 $
 * @author $author$
 */
public class Mixin extends WithMethods {
    /**
     * 
     */
    private static final long serialVersionUID = -27528558054488925L;

    /** The original mixin this was constructed from.  It is useful
     *  when a mixin actually represents a class.
     *  @see momi.MoMiClass */
    protected Mixin original_mixin;

    /** The original class that original_mixin was applied to */
    protected MoMiClass original_class;

    /** the methods inherited from the parent class where some methods
     *  are redefined by the mixin */
    protected MoMiClass parent_redefined;

    /** the methods defined in the mixin, that are also defined in
     *  the base class */
    protected WithMethods name_clashes = new WithMethods();

    /**
     * Store the original mixin this class was made from
     *
     * @param mixin the original mixin
     */
    protected final void set_original_mixin(Mixin mixin) {
        original_mixin = mixin;
    }

    /**
     * Store the original class this class was made from
     *
     * @param cl the original parent class
     */
    protected final void set_original_class(MoMiClass cl) {
        original_class = cl;
    }

    /**
     * Store the methods inherited from the parent class, with some
     * methods redefined according to the original_mixin
     *
     * @param parent DOCUMENT ME!
     */
    protected final void set_parent_redefined(MoMiClass parent) {
        parent_redefined = parent;
    }

    /**
     * Create a new instance from this class.  It is only meaningful when
     * this mixin actually represents a new derived class.  It also takes
     * care of performing all the correct method bindings and setting
     * the next pointer.
     *
     * @return the new instance with all the bindings
     */
    public final MoMiObject new_instance() {
        MoMiObject new_obj = _new_instance();
        new_obj.set_original_class(this);

        if (parent_redefined != null) {
            MoMiObject next = original_class.new_instance();
            new_obj.set_next(next);
            update_obj_methods(next, parent_redefined);
        }

        update_obj_methods(new_obj, this);
        update_self_table(new_obj);
        return new_obj;
    }

    protected void update_obj_methods(MoMiObject obj, WithMethods new_methods) {
        obj.set_methods(new_methods.get_methods());

        WithMethods internal_methods = new WithMethods(new_methods.clone_methods());

        Enumeration clashes = obj.original_class.name_clashes.get_methods().elements();
        MoMiMethod method;

        // overwrites the internal methods with the ones in name clash
        // (i.e. static binding)
        while (clashes.hasMoreElements()) {
            method = (MoMiMethod) clashes.nextElement();
            internal_methods.set_method(method.name, method);
        }

        obj.internal_methods = internal_methods;
    }

    protected void update_self_table(MoMiObject obj) {
        Enumeration meths = obj.get_methods().elements();
        MoMiMethod meth;

        while (meths.hasMoreElements()) {
            meth = (MoMiMethod) meths.nextElement();
            if (! was_expected(meth.name))
                obj.update_self(meth.name, obj, meth.kind == MoMiMethod.REDEFINED);
            else // we get the same of next
                obj.update_self(meth.name, obj.next.get_self(meth.name), false);
        }
    }

    protected final boolean was_expected(String meth) {
        try {
            if (original_mixin == null)
                return false;

            MoMiMethod orig = original_mixin.get_method(meth);
            return orig.kind == MoMiMethod.EXPECTED;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Internal factory method used to actually create a new
     * instance from a class inherited from MoMiObject.  The idea is that
     * every subclass inherited from Mixin should override this method
     * in order to create an object from the correct MoMiObject's subclass.
     *
     * @return the new instance without bindings
     */
    protected MoMiObject _new_instance() {
        return original_mixin._new_instance();
    }

    /**
     * Perform the application of a mixin to a (parent) class.
     * No check is performed on methods since all these checks are assumed
     * to be performed statically by the type system of the compiler.
     *
     * @param m the mixin
     * @param c the parent class
     *
     * @return a new class that is derived by the parent class
     */
    public static MoMiClass apply(Mixin m, MoMiClass c) throws NoSuchMethodException {
        Enumeration mixin_meths = m.get_methods().elements();
        MoMiClass new_class = new MoMiClass();
        new_class.set_methods(c.clone_methods());

        MoMiClass parent_redefined = c.clone_class();
        parent_redefined.set_methods(c.clone_methods());

        MoMiMethod method;

        while (mixin_meths.hasMoreElements()) {
            method = (MoMiMethod) mixin_meths.nextElement();

            switch (method.kind) {
            case MoMiMethod.REDEFINED:
                parent_redefined.set_method(method.name, method);
                new_class.set_method(method.name, method);
                break;
            case MoMiMethod.DEFINED:
                if (! new_class.contains_method(method.name))
                    new_class.set_method(method.name, method);
                // otherwise it is a run-time name clash, and this way
                // for this method dynamic binding is not used.
                else
                    new_class.name_clashes.set_method(method.name, method);
                break;
            }
        }

        new_class.set_original_mixin(m);
        new_class.set_parent_redefined(parent_redefined);

        // if there was a parent already we have to create a clone
        // of its (redefined) methods, and update them according to this
        // mixin
        if (c.parent_redefined == null)
            new_class.set_original_class(c);
        else {
            MoMiClass new_parent = c.clone_class();
            update_parent_redefined(new_parent.parent_redefined,
                                    m.get_methods().elements());
            new_class.set_original_class(new_parent);
        }

        return new_class;
    }

    protected static void update_parent_redefined(MoMiClass parent,
                                                  Enumeration mixin_meths) {
        MoMiMethod method;

        while (mixin_meths.hasMoreElements()) {
            method = (MoMiMethod) mixin_meths.nextElement();

            switch (method.kind) {
            case MoMiMethod.REDEFINED:
                parent.set_method(method.name, method, true);
            }
        }
    }

    public final void set_method(String name, MoMiMethod meth,
                                 boolean recursive) {
        set_method(name, meth);
        if (parent_redefined != null && recursive)
            parent_redefined.set_method(name, meth, true);
    }

    /**
     * Return a string denoting the kind of this method table: mixin
     *
     * @return the string denoting the kind of this method table
     */
    protected String _with_methods_string() {
        return "mixin";
    }
}