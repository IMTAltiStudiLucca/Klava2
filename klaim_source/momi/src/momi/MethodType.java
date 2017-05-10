package momi;

import java.util.Vector;

public class MethodType implements MoMiType {
    /**
     * 
     */
    private static final long serialVersionUID = 3048441628291200070L;
    protected String name;
    protected Vector parameters;
    protected MoMiType return_type;

    public MethodType(String name, Vector parameters, MoMiType return_type) {
        this.name = name;
        this.parameters = parameters;
        this.return_type = return_type;
    }

    public boolean equals(MoMiType type) {
        return compare(type, true);
    }

    public boolean subtype(MoMiType type) {
        return compare(type, false);
    }

    protected boolean compare(MoMiType type, boolean eq) {
        if (! (type instanceof MethodType))
            return false;

        MethodType method_type = (MethodType)type;

        boolean result = false;

        if (name.equals(method_type.name) &&
            compare(return_type, method_type.return_type, true) &&
            parameters.size() == method_type.parameters.size()) {
            // covariance on return type is not provided at the moment
            // when it is, replace the above with
            // compare(return_type, method_type.return_type, eq)

            MoMiType t1, t2;

            for (int i = 0; i < parameters.size(); ++i) {
                t1 = (MoMiType) method_type.parameters.elementAt(i);
                t2 = (MoMiType) parameters.elementAt(i);
                if (! compare(t1, t2, true))
                    return false;
                // contravariance on parameter types is not provided at the
                // moment; see the note above
            }

            result = true;
        }

        return result;
    }

    protected static boolean compare(MoMiType type1, MoMiType type2, boolean eq) {
        if (eq)
            return type1.equals(type2);
        else
            return type1.subtype(type2);
    }

    /**
     * Create a string representation of this method type
     *
     * @return the string representation of this method type
     */
    public String toString() {
        return _toString(0);
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

        return indent_str + name + ":(" + parameters + ")->" + return_type;
     }
}