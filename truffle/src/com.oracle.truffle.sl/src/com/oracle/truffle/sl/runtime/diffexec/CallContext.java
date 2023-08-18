package com.oracle.truffle.sl.runtime.diffexec;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public abstract sealed class CallContext implements Comparable<CallContext> {
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

    public final CallContext getRoot() {
        return root;
    }

    public final NodeIdentifier getNodeIdentifier() {
        return nodeIdentifier;
    }

    public abstract ContextBase getBase();

    public int depth() {
        return root.depth() + 1;
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallContext)) return false;

        return equals(this, (CallContext) o);
    }

    @Override
    public final int compareTo(CallContext o) {
        return compare(this, o);
    }

    @SuppressWarnings("UnstableApiUsage")
    protected static Hasher hashCode(CallContext root, NodeIdentifier nodeIdentifier) {
        return nodeIdentifier.hash(Hashing.murmur3_32_fixed().newHasher())
                .putInt(root != null ? root.hashCode() : 0);
    }

    public static boolean equals(CallContext e1, CallContext e2) {
        return compare(e1, e2) == 0;
    }

    private static int compare(CallContext e1, CallContext e2) {
        do {
            if (e1 == e2) return 0;
            int niComp = e1.nodeIdentifier.compareTo(e2.nodeIdentifier);
            if (niComp != 0) return niComp;
            if (e1 instanceof Loop l1) {
                if (!(e2 instanceof Loop l2)) return 1;
                int loopComp = Integer.compare(l1.loopCount, l2.loopCount);
                if (loopComp != 0) return loopComp;
            } else if (e2 instanceof Loop) {
                return -1;
            }

            e1 = e1.root;
            e2 = e2.root;
        } while (e1 != null && e2 != null);

        return e1 == null ? e2 == null ? 0 : 1 : -1;
    }

    public static abstract sealed class ContextBase extends CallContext {
        private ContextBase(CallContext root, NodeIdentifier nodeIdentifier, FunctionCall calledFrom, int hashCode) {
            super(root, nodeIdentifier, calledFrom, hashCode);
        }
    }

    public final static class ExecutionBase extends ContextBase {
        public static final ExecutionBase INSTANCE = new ExecutionBase();

        private ExecutionBase() {
            super(null, null, null, 0);
        }

        @Override
        public int depth() {
            return 0;
        }

        @Override
        public ContextBase getBase() {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            return o == this; // ExecutionBase is a singleton class
        }

        @Override
        public String toString() {
            return "ExecutionBase";
        }

    }

    public static final class FunctionCall extends ContextBase {
        public FunctionCall(CallContext root, NodeIdentifier nodeIdentifier) {
            super(root,
                    nodeIdentifier,
                    root instanceof FunctionCall ? (FunctionCall) root : root.calledFrom,
                    hashCode(root, nodeIdentifier).putInt(0).hash().asInt());
        }

        @Override
        public ContextBase getBase() {
            return this;
        }

        @Override
        public String toString() {
            return "FunctionCall{" +
                    "nodeIdentifier=" + nodeIdentifier +
                    ", root=" + root +
                    '}';
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
        public ContextBase getBase() {
            return calledFrom;
        }

        private static int hashCode(CallContext root, NodeIdentifier nodeIdentifier, int loopCount) {
            return hashCode(root, nodeIdentifier).putInt(loopCount + 1).hash().asInt();
        }

        @Override
        public String toString() {
            return "Loop{" + "loopCount=" + loopCount +
                    ", nodeIdentifier=" + nodeIdentifier +
                    ", root=" + root +
                    '}';
        }
    }
}
