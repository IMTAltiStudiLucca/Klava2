package momi;

public interface MoMiType extends java.io.Serializable {
    boolean equals(MoMiType t);
    boolean subtype(MoMiType t);
    String toString();
}