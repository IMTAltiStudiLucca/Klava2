package klava.momi;

import momi.MoMiClass;
import momi.MoMiObject;
import momi.MoMiType;

public class KMoMiClass extends MoMiTupleItem {
    /**
     * 
     */
    private static final long serialVersionUID = 8063236361557407303L;

    public KMoMiClass(MoMiType type) {
        super(type);
    }

    public KMoMiClass(MoMiType type, MoMiClass mixin) {
        super(type, mixin);
    }

    public final MoMiObject new_instance() {
        return getMoMiClass().new_instance();
    }

    public Object duplicate() {
        return new KMoMiClass(type, (MoMiClass)value);
    }

    public final MoMiClass getMoMiClass() {
        return (MoMiClass) getValue();
    }
}