package momi;

/**
 * Class representing a MoMi class.  It basically provides no further
 * functionality, but useful to statically ensure that a mixin is not
 * applied to another mixin.
 *
 * @version $Revision: 1.1 $
 * @author $author$
 */
public class MoMiClass extends Mixin {
    /**
     * 
     */
    private static final long serialVersionUID = -1841499995396916287L;

    MoMiClass clone_class() {
        MoMiClass newm = new MoMiClass();
        newm.methods = methods;
        newm.original_class = original_class;
        newm.original_mixin = original_mixin;
        newm.parent_redefined =
                (parent_redefined == null ? null :
                parent_redefined.clone_class());
        newm.name_clashes = name_clashes;

        return newm;
    }

    /**
     * Return a string denoting the kind of this method table: class
     *
     * @return the string denoting the kind of this method table
     */
    protected String _with_methods_string() {
        return "class";
    }
}