package momi;

public class ClassType implements MoMiType {
    /**
     * 
     */
    private static final long serialVersionUID = 2448518719822988642L;
    protected RecordType methods = new RecordType();

    public ClassType() {}

    public ClassType(RecordType type) {
        methods = type;
    }

    public void addMethod(MethodType meth) {
        methods.addMethod(meth);
    }

    public boolean equals(MoMiType type) {
        return (type instanceof ClassType &&
            methods.equals(((ClassType)type).methods));
    }

    public boolean subtype(MoMiType type) {
        return (type instanceof ClassType &&
            methods.subtype(((ClassType)type).methods));
    }

    /**
     * Convert this class type into a string
     *
     * @return the string representation of this class type
     */
    public String toString() {
        return _toString(0);
    }

    /**
     * Convert this class type to a string with indentation
     *
     * @param indent the indentation level
     *
     * @return the string representation of this class type
     */
    String _toString(int indent) {
        String indent_str = MoMiPrinter.build_indent(indent);
        String indent_str2 = MoMiPrinter.build_indent(indent + 2);

        return indent_str + "class<\n" +
            methods._toString(indent + 4) + "\n" + indent_str2 +">";
    }
}