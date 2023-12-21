package com.oracle.truffle.sl.runtime.diffexec;

import com.google.common.hash.Hasher;

public final class ExecutionContext extends Hashable implements Comparable<ExecutionContext> {
    private final CallContext callContext;
    private final NodeIdentifier currentNodeIdentifier;

    public ExecutionContext(CallContext callContext, NodeIdentifier currentNodeIdentifier) {
        this.callContext = callContext;
        this.currentNodeIdentifier = currentNodeIdentifier;
    }

    public CallContext getCallContext() {
        return callContext;
    }

    public NodeIdentifier getCurrentNodeIdentifier() {
        return currentNodeIdentifier;
    }

    @Override
    public int compareTo(ExecutionContext o) {
        if (this == o) return 0;
        int niComp = currentNodeIdentifier.compareTo(o.currentNodeIdentifier);
        if (niComp != 0) return niComp;
        return callContext.compareTo(o.callContext);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutionContext that)) return false;

        if (!NodeIdentifier.equals(currentNodeIdentifier, that.currentNodeIdentifier)) return false;
        return CallContext.equals(callContext, that.callContext);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Hasher hash(Hasher hasher) {
        return currentNodeIdentifier.hash(hasher)
                .putInt(callContext.hashCode());
    }

    @Override
    public String toString() {
        return "ExecutionContext{" +
                "callContext=" + callContext +
                ", currentNodeIdentifier=" + currentNodeIdentifier +
                '}';

    }
}
