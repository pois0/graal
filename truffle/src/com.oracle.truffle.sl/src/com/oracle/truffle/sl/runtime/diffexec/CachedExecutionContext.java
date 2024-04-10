package com.oracle.truffle.sl.runtime.diffexec;

public final class CachedExecutionContext implements Comparable<CachedExecutionContext> {
    private final CachedCallContext callContext;
    private final NodeIdentifier currentNodeIdentifier;

    public CachedExecutionContext(CachedCallContext callContext, NodeIdentifier currentNodeIdentifier) {
        this.callContext = callContext;
        this.currentNodeIdentifier = currentNodeIdentifier;
    }

    public CachedCallContext getCallContext() {
        return callContext;
    }

    public NodeIdentifier getCurrentNodeIdentifier() {
        return currentNodeIdentifier;
    }

    @Override
    public int compareTo(CachedExecutionContext o) {
        if (this == o) return 0;
        int niComp = currentNodeIdentifier.compareTo(o.currentNodeIdentifier);
        if (niComp != 0) return niComp;
        return callContext.compareTo(o.callContext);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CachedExecutionContext that)) return false;

        if (!NodeIdentifier.equals(currentNodeIdentifier, that.currentNodeIdentifier)) return false;
        return CachedCallContext.equals(callContext, that.callContext);
    }

    @Override
    public String toString() {
        return "ExecutionContext{" +
                "callContext=" + callContext +
                ", currentNodeIdentifier=" + currentNodeIdentifier +
                '}';

    }
}
