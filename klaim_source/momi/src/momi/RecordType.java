package momi;

import java.util.Hashtable;
import java.util.Enumeration;

public class RecordType implements MoMiType {
    /**
     * 
     */
    private static final long serialVersionUID = 2373095757194744963L;
    protected Hashtable<String,MethodType> methods = new Hashtable<String,MethodType>();

    public RecordType() {
    }

    public RecordType(Hashtable<String,MethodType> v) {
        methods = v;
    }

    public void addMethod(MethodType type) {
        methods.put(type.name, type);
    }

    public boolean equals(MoMiType type) {
        return compare(type, true);
    }

    public boolean subtype(MoMiType type) {
        return compare(type, false);
    }

    protected boolean compare(MoMiType type, boolean eq) {
        if (!(type instanceof RecordType))
            return false;

        Hashtable<String,MethodType> _methods = ((RecordType) type).methods;

        boolean result = false;

        boolean pre_condition = (eq ? (methods.size() == _methods.size())
                : (methods.size() >= _methods.size()));

        if (pre_condition) {
            Enumeration<MethodType> meths = _methods.elements();
            MethodType t1, t2;
            while (meths.hasMoreElements()) {
                t2 = meths.nextElement();
                if (!methods.containsKey(t2.name))
                    return false;
                t1 = methods.get(t2.name);
                if (!compare(t1, t2, eq))
                    return false;
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
     * Convert this record type into a string
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
        StringBuffer buffer = new StringBuffer();

        Enumeration meths = methods.elements();
        while (meths.hasMoreElements()) {
            buffer.append(((MethodType) meths.nextElement())._toString(indent));
            if (meths.hasMoreElements())
                buffer.append('\n');
        }

        return buffer.toString();
    }
}