package com.oracle.truffle.sl.runtime.diffexec;

public record ObjectReference<TIME extends Time<TIME>>(TIME objGenCtx) {
    public ObjectReference {
        if (objGenCtx == null) throw new NullPointerException();
    }
}
