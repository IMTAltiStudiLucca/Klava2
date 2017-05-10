package klava.momi;

import momi.MoMiMethod;
import momi.MoMiObject;
import momi.MoMiType;

public class KMoMiObject extends MoMiTupleItem {
    /**
     * 
     */
    private static final long serialVersionUID = -8885229480918166971L;

    public KMoMiObject(MoMiType type) {
        super(type);
    }

    public KMoMiObject(MoMiType type, MoMiObject obj) {
        super(type, (MoMiObjectWithProcess)obj);
    }

    public KMoMiObject(MoMiType type, MoMiObjectWithProcess obj) {
        super(type, obj);
    }

    /**
     * Retrieve the inherited part of this object
     *
     * @return next the inherited sub-object
     */
    public final MoMiObject get_next() {
        return ((MoMiObject)value).get_next();
    }

    public final MoMiObject get_self(String meth) {
        return ((MoMiObject)value).get_self(meth);
    }

    /**
     * Retrieve the original implementation of a method
     *
     * @param name the name of the method to retrieve
     *
     * @return the original implementation for this method
     *
     * @throws NoSuchMethodException
     */
    public final MoMiMethod get_next_method(String name)
                                     throws NoSuchMethodException {
        return ((MoMiObject)value).get_next_method(name);
    }

    public final MoMiObjectWithProcess getObject() {
        return (MoMiObjectWithProcess)value;
    }

    /**
     * Retrieve a method given its name
     *
     * @param name the name of the method to retrieve
     *
     * @return the MoMiMethod object representing the requested method
     *
     * @throws NoSuchMethodException if the requested method is not found
     */
    public final MoMiMethod get_method(String name) throws NoSuchMethodException {
        return ((MoMiObject)value).get_method(name);
    }

    public Object duplicate() {
        return new KMoMiObject(type, (MoMiObjectWithProcess)value);
    }
}