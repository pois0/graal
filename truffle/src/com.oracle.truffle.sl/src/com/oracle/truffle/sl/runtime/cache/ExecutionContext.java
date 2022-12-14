package com.oracle.truffle.sl.runtime.cache;

public final class ExecutionContext implements Comparable<ExecutionContext> {
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
        if (!(o instanceof ExecutionContext)) return false;

        final ExecutionContext that = (ExecutionContext) o;

        if (!currentNodeIdentifier.equals(that.currentNodeIdentifier)) return false;
        return CallContext.equals(callContext, that.callContext);
    }

    @Override
    public int hashCode() {
        return 31 * currentNodeIdentifier.hashCode() + this.callContext.hashCode();
    }

    @Override
    public String toString() {
        return "ExecutionContext{" +
                "callContext=" + callContext +
                ", currentNodeIdentifier=" + currentNodeIdentifier +
                '}';
    }
}
