package momi;

public class MoMiPrinter {
    static String build_indent(int indent) {
        if (indent <= 0)
            return "";

        StringBuffer indent_str = new StringBuffer(indent);

        for (int i = 0; i < indent; ++i)
            indent_str.append(' ');

        return indent_str.toString();
    }
}