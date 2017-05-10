package momitest;

import momi.*;
import java.util.Vector;

public class testtypes {
    public static void main(String args[]) throws Exception {
        SimpleType string_type = new SimpleType("string");
        SimpleType int_type = new SimpleType("int");
        SimpleType bool_type = new SimpleType("bool");

        Vector<SimpleType> m_par = new Vector<SimpleType>();
        m_par.addElement(string_type);
        m_par.addElement(int_type);
        m_par.addElement(bool_type);

        Vector<SimpleType> msub_par = new Vector<SimpleType>();
        msub_par.addElement(string_type);
        msub_par.addElement(string_type);
        msub_par.addElement(string_type);

        Vector<SimpleType> n_par = new Vector<SimpleType>();
        n_par.addElement(string_type);
        n_par.addElement(int_type);

        Vector<SimpleType> nsub_par = new Vector<SimpleType>();
        nsub_par.addElement(string_type);
        nsub_par.addElement(string_type);

        Vector<SimpleType> p_par = new Vector<SimpleType>();
        p_par.addElement(string_type);
        p_par.addElement(bool_type);
        p_par.addElement(int_type);

        MethodType m = new MethodType("m", m_par, string_type);
        MethodType n = new MethodType("n", n_par, string_type);
        MethodType p = new MethodType("p", p_par, bool_type);

        // msub <: m
        MethodType msub = new MethodType("m", msub_par, int_type);

        // nsub <: n
        MethodType nsub = new MethodType("n", nsub_par, string_type);

        RecordType rect1 = new RecordType();
        rect1.addMethod(m);
        rect1.addMethod(n);

        RecordType rect2 = new RecordType();
        rect2.addMethod(p);
        rect2.addMethod(n);
        rect2.addMethod(m);

        RecordType rect3 = new RecordType();
        rect3.addMethod(m);
        rect3.addMethod(p);
        rect3.addMethod(n);

        System.out.println("rect1: " + rect1);
        System.out.println("rect2: " + rect2);
        System.out.println("rect3: " + rect2);

        dash();

        System.out.println("rect1 == rect2: " + rect1.equals(rect2));
        System.out.println("rect1 <= rect2: " + rect1.subtype(rect2));
        System.out.println("rect2 <= rect1: " + rect2.subtype(rect1));

        dash();

        System.out.println("rect2 == rect3: " + rect2.equals(rect3));
        System.out.println("rect2 <= rect3: " + rect2.subtype(rect3));
        System.out.println("rect3 <= rect2: " + rect3.subtype(rect2));

        dash();

        boolean msubres = msub.subtype(m);
        System.out.println(msub + " <: " + m + " is " + msubres);
        System.out.println(nsub + " <: " + n + " is " + nsub.subtype(n));

        dash();

        ClassType c1 = new ClassType(rect1);
        ClassType c2 = new ClassType(rect2);
        ClassType c3 = new ClassType(rect3);

        System.out.println("c1: " + c1);
        System.out.println("c2: " + c2);
        System.out.println("c3: " + c3);

        dash();

        System.out.println("c1 <: c2 is " + c1.subtype(c2));
        System.out.println("c3 <: c2 is " + c3.subtype(c2));
        System.out.println("c3 <: c1 is " + c3.subtype(c1));

        dash();

        RecordType m1def = new RecordType();
        m1def.addMethod(m);

        RecordType m1exp = new RecordType();
        m1exp.addMethod(n);
        m1exp.addMethod(p);

        MethodType t = new MethodType("t", n_par, string_type);
        RecordType m1old = new RecordType();
        m1old.addMethod(t);

        // tsub <: t
        MethodType tsub = new MethodType("t", n_par, int_type);
        RecordType m1red = new RecordType();
        m1red.addMethod(tsub);

        MethodType q = new MethodType("q", n_par, int_type);

        // m2def <: m1def
        RecordType m2def = new RecordType();
        m2def.addMethod(msub);
        m2def.addMethod(q);

        // m1exp <: m2exp
        RecordType m2exp = new RecordType();
        m2exp.addMethod(n);

        MixinType m1 = new MixinType(m1def, m1red, m1exp, m1old);
        MixinType m2 = new MixinType(m2def, m1red, m2exp, m1old);

        System.out.println("m1: " + m1);
        System.out.println("m2: " + m2);

        dash();

        System.out.println("m2 <: m1 is " + m2.subtype(m1));
        System.out.println("m1 <: m2 is " + m1.subtype(m2));
    }

    protected static void dash() {
        System.out.println("------------------------------");
    }
}