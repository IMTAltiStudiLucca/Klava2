package momitest;

import momi.*;

public class test2 {
    public static void main(String args[]) throws Exception {
        MoMiClass C = new Ctest1();

        MoMiObject ref = C.new_instance();
        MoMiObject pass_self;

        System.out.println("Created object of class C: " + ref);

        MoMiMethod meth = ref.get_method("p");

        meth.invoke(ref);
        System.out.println("result of p(): " + meth.get_result());

        Mixin M = new Mtest2();
        Mixin M2 = new M2test2();
        System.out.println("Applying: M2 <> (M <> C)");
        MoMiClass MC = Mixin.apply(M, C);
        MoMiClass MCC = Mixin.apply(M2, MC);
        System.out.println("MCC: " + MCC);

        ref = MCC.new_instance();

        System.out.println("Created object of class M2 <> (M <> C): " + ref);

        meth = ref.get_method("p");
        pass_self = ref.get_self("p");

        meth.invoke(pass_self);
        System.out.println("result of p(): " + meth.get_result());

        System.out.println("Applying: M <> (M2 <> (M <> C))");
        MoMiClass MCCC = Mixin.apply(M, MCC);
        System.out.println("MCCC: " + MCCC);

        ref = MCCC.new_instance();

        System.out.println("Created object of class M <> (M2 <> (M <> C)): " + ref);

        meth = ref.get_method("p");
        pass_self = ref.get_self("p");

        meth.invoke(pass_self);
        System.out.println("result of p(): " + meth.get_result());

        System.out.println("Applying: M2 <> (M2 <> (M <> C))");
        MoMiClass MCCC2 = Mixin.apply(M2, MCC);
        System.out.println("MCCC2: " + MCCC2);

        ref = MCCC2.new_instance();

        System.out.println("Created object of class M2 <> (M2 <> (M <> C)): " + ref);

        meth = ref.get_method("p");
        pass_self = ref.get_self("p");

        meth.invoke(pass_self);
        System.out.println("result of p(): " + meth.get_result());

        // now check that after all the original class hasn't changed

        ref = C.new_instance();

        System.out.println("Created object of class C: " + ref);

        meth = ref.get_method("p");

        meth.invoke(ref);
        System.out.println("result of p(): " + meth.get_result());
    }
}

class Mtest2 extends Mixin {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    static int counter = 0;

    int my_counter = ++counter;

    public Mtest2() {
        set_method("n", new Mtest2_n(my_counter)); // redefined
        set_method("p", new MoMiMethod("p", MoMiMethod.EXPECTED)); // expected
    }

    public MoMiObject _new_instance() {
        return new Mtest2Object();
    }
}

class Mtest2Object extends MoMiObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected Integer i = new Integer("1");
    protected Integer k = new Integer("1");
}

class Mtest2_n extends MoMiMethod {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    int counter;

    public Mtest2_n(int c) {
        super("n", REDEFINED);
        counter = c;
    }

    public void invoke(MoMiObject _self) throws NoSuchMethodException {
        Mtest2Object self = (Mtest2Object) _self;

        System.out.println("Mtest2.n()");

        stack.push(new Integer(self.i.intValue() + self.k.intValue()));
    }
}

class Mtest2_p extends MoMiMethod {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Mtest2_p() {
        super("p", EXPECTED);
    }

    public void invoke(MoMiObject _self) throws NoSuchMethodException {
        Mtest2Object self = (Mtest2Object) _self;

        MoMiObject next = self.get_next();
        MoMiMethod meth = next.get_method("p");
        meth.invoke(next);

        stack.push(meth.get_result());
    }
}

class M2test2 extends Mtest2 {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    M2test2() {
        set_method("n", new M2test2_n(my_counter)); // redefined
    }
}

class M2test2_n extends MoMiMethod {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    int counter;

    public M2test2_n(int c) {
        super("n", REDEFINED);
        counter = c;
    }

    public void invoke(MoMiObject _self) throws NoSuchMethodException {
        Mtest2Object self = (Mtest2Object) _self;

        System.out.println("M2test2.n()");

        MoMiObject next = self.get_next();
        MoMiMethod next_m = self.get_next_method("n");
        next_m.invoke(next);

        stack.push(new Integer(self.i.intValue() + self.k.intValue() * 2));
    }
}
