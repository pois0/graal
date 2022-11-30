package com.oracle.truffle.sl.runtime.cache;

import java.util.Arrays;

public class ExecutionContext {
    private final CallContextElement[] callContext;
    private final NodeIdentifier currentNodeIdentifier;

    public ExecutionContext(CallContextElement[] callContext, NodeIdentifier currentNodeIdentifier) {
        this.callContext = callContext;
        this.currentNodeIdentifier = currentNodeIdentifier;
    }

    public CallContextElement[] getCallContext() {
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

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(callContext, that.callContext)) return false;
        return currentNodeIdentifier.equals(that.currentNodeIdentifier);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(callContext);
        result = 31 * result + currentNodeIdentifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ExecutionContext{" +
                "callContext=" + Arrays.toString(callContext) +
                ", currentNodeIdentifier=" + currentNodeIdentifier +
                '}';
    }
}
