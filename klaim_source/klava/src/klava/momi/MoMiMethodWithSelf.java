package klava.momi;

import klava.KlavaException;
import klava.LogicalLocality;
import momi.MoMiMethod;
import momi.MoMiObject;

public abstract class MoMiMethodWithSelf extends MoMiMethod {
    protected LogicalLocality self = new LogicalLocality( "self" ) ;

    public MoMiMethodWithSelf(String name, int kind) {
        super(name, kind);
    }

    public abstract void kinvoke(MoMiObject _self)
        throws KlavaException, NoSuchMethodException;
}