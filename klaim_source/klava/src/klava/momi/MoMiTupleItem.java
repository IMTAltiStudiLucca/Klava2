package klava.momi;

import klava.KlavaException;
import klava.TupleItem;
import momi.MoMiType;

public class MoMiTupleItem implements TupleItem {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected MoMiType type;
    protected Object value;

    public MoMiTupleItem(MoMiType type) {
        this(type, null);
    }

    public MoMiTupleItem(MoMiType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public final MoMiType getType() {
        return type;
    }

    public final Object getValue() {
        return value;
    }

    public Object duplicate() {
        return new MoMiTupleItem(type, value);
    }

    public void setValue(Object o) {
        if (o instanceof MoMiTupleItem)
            value = ((MoMiTupleItem)o).getValue();
    }

    public boolean isFormal() {
        return value == null;
    }

    public String toString() {
        if (value == null)
            return "!"+type;
        else
            return value + "\n  type = \n  " + type;
    }

    public void setValue(String o) throws KlavaException {
        // TODO implement it somehow?
        throw new KlavaException("MoMiTupleItem: conversion from string feature not implemented");
    }
}