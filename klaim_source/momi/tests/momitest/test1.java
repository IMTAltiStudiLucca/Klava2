package momitest;

import momi.*;

import java.util.*;

public class test1 {
    public static void main(String args[]) throws Exception {
        MoMiClass C = new Ctest1();
        MoMiObject ref = C.new_instance();

        System.out.println("Created object: " + ref);

        Mixin M = new Mtest1();

        MoMiMethod meth = ref.get_method("m");

        meth.pass_argument(new Integer(100));
        meth.invoke(ref);
        System.out.println("result of m: " + meth.get_result());

        meth.pass_argument(new Integer(200));
        meth.invoke(ref);
        System.out.println("result of m: " + meth.get_result());

        meth = ref.get_method("n");

        meth.invoke(ref);
        System.out.println("result of n: " + meth.get_result());

        System.out.println("Applying mixin M to class C");
        MoMiClass MC = Mixin.apply(M, C);
        System.out.println("MC: " + MC);

        ref = MC.new_instance();

        System.out.println("Created object: " + ref);

        meth = ref.get_method("m");

        meth.pass_argument(new Integer(300));
        meth.invoke(ref);
        System.out.println("result of m: " + meth.get_result());

        System.out.println("Applying mixin M to class MC");
        MoMiClass MMC = Mixin.apply(M, MC);
        System.out.println("MMC: " + MMC);

        ref = MMC.new_instance();

        System.out.println("Created object: " + ref);

        meth = ref.get_method("m");

        meth.pass_argument(new Integer(400));
        meth.invoke(ref);
        System.out.println("result of m: " + meth.get_result());

        /////////////////////////

        /**
         * there is a run-time name clash between M2 and MC,
         * since both M2 and C (and thus also MC) defines n.
         * n is "statically" bound to M2.n in any method of M2.
         * However, from objects created from Mixin.apply(M2, MC),
         * n has to be bound to the implementation of C.
         */

        Mixin M2 = new M2test1();

        System.out.println("Applying mixin M2 to class MC");
        MoMiClass M2MC = Mixin.apply(M2, MC);
        System.out.println("M2MC: " + M2MC);

        ref = M2MC.new_instance();

        System.out.println("Created object: " + ref);

        meth = ref.get_method("m");
        MoMiObject pass_self = ref.get_self("m");
        meth.pass_argument(new Integer(500));
        meth.invoke(pass_self);
        System.out.println("result of m: " + meth.get_result());

        meth = ref.get_method("n");
        pass_self = ref.get_self("n");
        meth.pass_argument("200");
        meth.pass_argument(new Integer(100));
        meth.invoke(pass_self);
        System.out.println("result of n: " + meth.get_result());
    }
}

class Ctest1 extends MoMiClass {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected static Hashtable<String,MoMiMethod> _methods = null;

    public Ctest1() {
        if (_methods == null) {
            _methods = new Hashtable<String,MoMiMethod>();
            set_methods(_methods);

            set_method("m", new Ctest1_m());
            set_method("n", new Ctest1_n());
            set_method("p", new Ctest1_p());
        } else
            set_methods(_methods);
    }

    protected MoMiObject _new_instance() {
        return new Ctest1Object();
    }
}

class Ctest1Object extends MoMiObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected Integer i = new Integer(10);
    protected String foo = "foo";

    public Integer get_i() {
        return i;
    }

    public void set_i(Integer i) {
        this.i = i;
    }

    public String get_foo() {
        return foo;
    }

    public void set_foo(String foo) {
        this.foo = foo;
    }
}

class Ctest1_m extends MoMiMethod {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Ctest1_m() {
        super("m", DEFINED);
    }

    public void invoke(MoMiObject _self) {
        Ctest1Object self = (Ctest1Object) _self;
        Integer j = (Integer) stack.pop();

        self.i = j;

        stack.push(self.i);
    }
}

class Ctest1_n extends MoMiMethod {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Ctest1_n() {
        super("n", DEFINED);
    }

    public void invoke(MoMiObject _self) {
        Ctest1Object self = (Ctest1Object) _self;

        System.out.println("Ctest1.n()");

        stack.push(self.i);
    }
}

// method p calls method n
class Ctest1_p extends MoMiMethod {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Ctest1_p() {
        super("p", DEFINED);
    }

    public void invoke(MoMiObject _self) throws NoSuchMethodException {
        Ctest1Object self = (Ctest1Object) _self;

        System.out.println("Ctest1.p()");

        MoMiMethod meth = self.get_method("n");
        MoMiObject pass_self = self.get_self("n");
        meth.invoke(pass_self);

        stack.push(meth.get_result());
    }
}

class Mtest1 extends Mixin {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Mtest1() {
        set_method("m", new Mtest1_m());
        set_method("n", new MoMiMethod("n", MoMiMethod.EXPECTED));
    }

    public MoMiObject _new_instance() {
        return new Mtest1Object();
    }
}

class Mtest1Object extends MoMiObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected Boolean b = new Boolean(false);

    public Boolean get_b() {
        return b;
    }

    public void set_Boolean(Boolean b) {
        this.b = b;
    }
}

class Mtest1_m extends MoMiMethod {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Mtest1_m() {
        super("m", REDEFINED);
    }

    public void invoke(MoMiObject _self) throws NoSuchMethodException {
        Mtest1Object self = (Mtest1Object) _self;
        Integer j = (Integer) stack.pop();

        MoMiObject next = self.get_next();
        MoMiMethod next_m = self.get_next_method("m");
        next_m.pass_argument(j);
        next_m.invoke(next);

        System.out.println("Mtest1.m()");

        MoMiMethod meth = self.get_method("n");
        MoMiObject pass_self = self.get_self("n");
        meth.invoke(pass_self);

        stack.push(meth.get_result());
    }
}

// another mixin, M2

class M2test1 extends Mixin {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public M2test1() {
        set_method("m", new MoMiMethod("m", MoMiMethod.EXPECTED)); // expected
        set_method("n", new M2test1_n()); // defined
    }

    public MoMiObject _new_instance() {
        return new M2test1Object();
    }
}

class M2test1Object extends MoMiObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected Integer i;
    protected Integer k;
}

class M2test1_n extends MoMiMethod {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public M2test1_n() {
        super("n", DEFINED);
    }

    /**
     * n(int j, String j2) {
     *      i = j;
     *      k = (int) j2;
     *      return (i + k);
     * }
     */
    public void invoke(MoMiObject _self) throws NoSuchMethodException {
        M2test1Object self = (M2test1Object) _self;
        Integer j = (Integer) stack.pop();
        String j2 = (String) stack.pop();

        System.out.println("M2test1.n()");

        self.i = j;
        self.k = new Integer(Integer.parseInt(j2));

        stack.push(new Integer(self.i.intValue() + self.k.intValue()));
    }
}