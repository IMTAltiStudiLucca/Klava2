package momi;

public class MixinType implements MoMiType {
    /**
     * 
     */
    private static final long serialVersionUID = 3975969279460127605L;
    protected RecordType defined;
    protected RecordType redefined;
    protected RecordType expected;
    protected RecordType old;

    public MixinType() {
        defined = new RecordType();
        expected = new RecordType();
        old = new RecordType();
        redefined = new RecordType();
    }

    public MixinType(RecordType def, RecordType red, RecordType exp, RecordType ol) {
        defined = (def == null ? new RecordType() : def);
        expected = (exp == null ? new RecordType() : exp);
        old = (ol == null ? new RecordType() : ol);
        redefined = (red == null ? new RecordType() : red);
    }

    public void addMethod(MethodType meth, int kind) {
        switch(kind) {
            case MoMiMethod.DEFINED:
                defined.addMethod(meth);
                break;
            case MoMiMethod.REDEFINED:
                redefined.addMethod(meth);
                break;
            case MoMiMethod.EXPECTED:
                expected.addMethod(meth);
                break;
            case MoMiMethod.OLD:
                old.addMethod(meth);
                break;
        }
    }

    public boolean equals(MoMiType type) {
        return compare(type, true);
    }

    public boolean subtype(MoMiType type) {
        return compare(type, false);
    }

    protected boolean compare(MoMiType type, boolean eq) {
        if (! (type instanceof MixinType))
            return false;

        MixinType t2 = (MixinType) type;

        return compare(defined, t2.defined, eq) &&
            compare(t2.expected, expected, eq) &&
            compare(redefined, t2.redefined, true) &&
            compare(old, t2.old, true);
    }

    protected static boolean compare(MoMiType type1, MoMiType type2, boolean eq) {
        if (eq)
            return type1.equals(type2);
        else
            return type1.subtype(type2);
    }

    /**
     * Convert this mixin type into a string
     *
     * @return the string representation of this mixin type
     */
    public String toString() {
        return _toString(0);
    }

    /**
     * Convert this mixin type to a string with indentation
     *
     * @param indent the indentation level
     *
     * @return the string representation of this mixin type
     */
    String _toString(int indent) {
        String indent_str = MoMiPrinter.build_indent(indent);
        String indent_str2 = MoMiPrinter.build_indent(indent + 4);

        return indent_str + "mixin<\n" +
            indent_str2 + "def=\n" +
            defined._toString(indent + 6) + "\n" +
            indent_str2 + "red=\n" +
            redefined._toString(indent + 6) + "\n" +
            indent_str2 + "exp=\n" +
            expected._toString(indent + 6) + "\n" +
            indent_str2 +">";
    }
}