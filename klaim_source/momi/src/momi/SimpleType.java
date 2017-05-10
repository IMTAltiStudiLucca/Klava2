package momi;

public class SimpleType implements MoMiType {
    /**
     * 
     */
    private static final long serialVersionUID = 8944398013496916179L;
    protected String typename;

    public SimpleType(String name) {
        typename = name;
    }

    public boolean equals(MoMiType type) {
        return (type instanceof SimpleType &&
            typename.equals(((SimpleType)type).typename));
    }

    public boolean subtype(MoMiType type) {
        if (! (type instanceof SimpleType))
            return false;

        SimpleType t = (SimpleType) type;

        // every simple type is subtype of string
        return (t.typename.equals("string") || equals(type));
    }

    public String toString() {
        return typename;
    }
}