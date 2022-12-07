package com.oracle.truffle.sl.runtime.cache;

import java.util.Arrays;

public abstract class CallContext {
    protected final NodeIdentifier nodeIdentifier;
    protected final CallContext root;
    protected final FunctionCall calledFrom;
    protected final int hashCode;

    private CallContext(CallContext root, NodeIdentifier nodeIdentifier, FunctionCall calledFrom, int hashCode) {
        this.root = root;
        this.nodeIdentifier = nodeIdentifier;
        this.calledFrom = calledFrom;
        this.hashCode = hashCode;
    }

    public CallContext getRoot() {
        return root;
    }

    public NodeIdentifier getNodeIdentifier() {
        return nodeIdentifier;
    }

    public FunctionCall getCalledFrom() {
        return calledFrom;
    }

    public int depth() {
        return root == null ? 1 : (root.depth() + 1);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static FunctionCall latestFunctionCall(CallContext ctx) {
        if (ctx == null) return null;
        if (ctx instanceof FunctionCall) return (FunctionCall) ctx;
        return ctx.calledFrom;
    }

    protected boolean equalsBase(CallContext that) {
        if (this == that) return true;
        if (!nodeIdentifier.equals(that.nodeIdentifier)) return false;
        if (root == null) return that.root == null;
        if (that.root == null) return false;
        return root.equalsBase(that.root);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallContext)) return false;

        return equalsBase((CallContext) o);
    }

    protected static int hashCode(CallContext root, NodeIdentifier nodeIdentifier) {
        int result = nodeIdentifier.hashCode();
        result = 31 * result + (root != null ? root.hashCode() : 0);
        return result;
    }

    public static final class FunctionCall extends CallContext {
        public FunctionCall(CallContext root, NodeIdentifier nodeIdentifier) {
            super(root,
                    nodeIdentifier,
                    root instanceof FunctionCall ? (FunctionCall) root : root.calledFrom,
                    hashCode(root, nodeIdentifier));
        }

        @Override
        protected boolean equalsBase(CallContext that) {
            if (!super.equalsBase(that)) return false;
            return that instanceof FunctionCall;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("FunctionCall{");
            sb.append("nodeIdentifier=").append(nodeIdentifier);
            sb.append(", root=").append(root);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class Loop extends CallContext {
        private final int loopCount;

        public Loop(CallContext root, NodeIdentifier nodeIdentifier, FunctionCall calledFrom, int loopCount) {
            super(root,
                    nodeIdentifier,
                    calledFrom,
                    hashCode(root, nodeIdentifier, loopCount));
            this.loopCount = loopCount;
        }

        public Loop(CallContext root, NodeIdentifier nodeIdentifier) {
            this(root,
                    nodeIdentifier,
                    root instanceof FunctionCall ? (FunctionCall) root : root == null ? null : root.calledFrom,
                    0);
        }

        public int getLoopCount() {
            return loopCount;
        }

        public CallContext increment() {
            return new Loop(root, nodeIdentifier, calledFrom, loopCount + 1);
        }

        @Override
        protected boolean equalsBase(CallContext that) {
            if (!super.equalsBase(that)) return false;
            if (!(that instanceof Loop)) return false;
            return loopCount == ((Loop) that).loopCount;
        }

        private static int hashCode(CallContext root, NodeIdentifier nodeIdentifier, int loopCount) {
            return 31 * CallContext.hashCode(root, nodeIdentifier) + loopCount;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Loop{");
            sb.append("loopCount=").append(loopCount);
            sb.append(", nodeIdentifier=").append(nodeIdentifier);
            sb.append(", root=").append(root);
            sb.append('}');
            return sb.toString();
        }
    }

    public final static class FunctionCallArray {
        private final NodeIdentifier[] raws;

        public FunctionCallArray(NodeIdentifier[] raws) {
            this.raws = raws;
        }

        public NodeIdentifier[] getRaws() {
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
