package klava.momi;

import klava.topology.KlavaProcess;
import momi.MoMiObject;

public class MoMiObjectWithProcess extends MoMiObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected transient KlavaProcess proc;

    final public void setProcess(KlavaProcess proc) {
        this.proc = proc;
    }

    final public KlavaProcess getProcess() {
        return proc;
    }
}