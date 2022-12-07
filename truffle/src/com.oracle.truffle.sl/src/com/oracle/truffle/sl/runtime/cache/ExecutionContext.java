package com.oracle.truffle.sl.runtime.cache;

import java.util.Objects;

public class ExecutionContext {
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutionContext)) return false;

        final ExecutionContext that = (ExecutionContext) o;

        if (!Objects.equals(callContext, that.callContext)) return false;
        return currentNodeIdentifier.equals(that.currentNodeIdentifier);
    }

    @Override
    public int hashCode() {
        CallContext callContext = this.callContext;
        int result = callContext != null ? callContext.hashCode() : 0;
        result = 31 * result + currentNodeIdentifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ExecutionContext{" +
                "callContext=" + callContext +
                ", currentNodeIdentifier=" + currentNodeIdentifier +
                '}';
    }
}
