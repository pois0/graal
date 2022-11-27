package com.oracle.truffle.sl.runtime.cache;


import java.util.Arrays;

public abstract class CallContextElement {
    protected final NodeIdentifier nodeIdentifier;

    private CallContextElement(NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public NodeIdentifier getNodeIdentifier() {
        return nodeIdentifier;
    }

    public static final class FunctionCall extends CallContextElement {
        public FunctionCall(NodeIdentifier nodeIdentifier) {
            super(nodeIdentifier);
        }

        public boolean equals(FunctionCall c) {
            return nodeIdentifier.equals(c.nodeIdentifier);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof FunctionCall)) return false;

            return equals((FunctionCall) o);
        }

        @Override
        public int hashCode() {
            return nodeIdentifier != null ? nodeIdentifier.hashCode() : 0;
        }
    }

    public static final class Loop extends CallContextElement {
        private final int loopCount;

        public Loop(NodeIdentifier nodeIdentifier, int loopCount) {
            super(nodeIdentifier);
            this.loopCount = loopCount;
        }

        public int getLoopCount() {
            return loopCount;
        }

        public CallContextElement increment() {
            return new Loop(nodeIdentifier, loopCount + 1);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof Loop)) return false;

            final Loop loop = (Loop) o;

            if (loopCount != loop.loopCount) return false;
            return nodeIdentifier.equals(loop.nodeIdentifier);
        }

        @Override
        public int hashCode() {
            int result = nodeIdentifier.hashCode();
            result = 31 * result + loopCount;
            return result;
        }
    }

    public final static class FunctionCallArray {
        private final FunctionCall[] raws;

        public FunctionCallArray(FunctionCall[] raws) {
            this.raws = raws;
        }

        public FunctionCall[] getRaws() {
            return raws;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FunctionCallArray)) return false;

            FunctionCallArray that = (FunctionCallArray) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(raws, that.raws);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(raws);
        }
    }
}
