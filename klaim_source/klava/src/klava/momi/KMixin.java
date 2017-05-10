package klava.momi;

import momi.Mixin;
import momi.MoMiClass;
import momi.MoMiObject;
import momi.MoMiType;

public class KMixin extends MoMiTupleItem {
    /**
     * 
     */
    private static final long serialVersionUID = -4600777674304131030L;

    public KMixin(MoMiType type) {
        super(type);
    }

    public KMixin(MoMiType type, Mixin mixin) {
        super(type, mixin);
    }

    public final MoMiObject new_instance() {
        return ((Mixin)value).new_instance();
    }

    public Object duplicate() {
        return new KMixin(type, (Mixin)value);
    }

    public final Mixin getMixin() {
        return (Mixin) getValue();
    }

    public static MoMiClass apply(Mixin m, MoMiClass c)
            throws NoSuchMethodException {
        return Mixin.apply(m, c);
    }

    public static MoMiClass apply(KMixin m, KMoMiClass c)
            throws NoSuchMethodException {
        return Mixin.apply(m.getMixin(), c.getMoMiClass());
    }

    public static MoMiClass apply(Mixin m, KMoMiClass c)
            throws NoSuchMethodException {
        return Mixin.apply(m, c.getMoMiClass());
    }

    public static MoMiClass apply(KMixin m, MoMiClass c)
            throws NoSuchMethodException {
        return Mixin.apply(m.getMixin(), c);
    }
}