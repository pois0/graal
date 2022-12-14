package com.oracle.truffle.sl.runtime.cache;

public abstract class CallContext implements Comparable<CallContext> {
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

    public int depth() {
        return root.depth() + 1;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static ContextBase latestFunctionCall(CallContext ctx) {
        return ctx.latestFunctionCallInner();
    }

    protected abstract ContextBase latestFunctionCallInner();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallContext)) return false;

        return equals(this, (CallContext) o);
    }

    @Override
    public int compareTo(CallContext o) {
        return compare(this, o);
    }

    protected static int hashCode(CallContext root, NodeIdentifier nodeIdentifier) {
        int result = nodeIdentifier.hashCode();
        result = 31 * result + (root != null ? root.hashCode() : 0);
        return result;
    }

    public static boolean equals(CallContext e1, CallContext e2) {
        do {
            if (e1 == e2) return true;
            if (!e1.nodeIdentifier.equals(e2.nodeIdentifier)) return false;
            if (e1 instanceof Loop) {
                if (e2 instanceof Loop) {
                    if (((Loop) e1).loopCount != ((Loop) e2).loopCount) return false;
                } else {
                    return false;
                }
            } else if (e2 instanceof Loop) {
                return false;
            }

            e1 = e1.root;
            e2 = e2.root;
        } while (e1 != null && e2 != null);

        return e1 == e2;
    }

    private static int compare(CallContext e1, CallContext e2) {
        do {
            if (e1 == e2) return 0;
            int niComp = e1.nodeIdentifier.compareTo(e2.nodeIdentifier);
            if (niComp != 0) return niComp;
            if (e1 instanceof Loop) {
                if (e2 instanceof Loop) {
                    int loopComp = Integer.compare(((Loop) e1).loopCount, ((Loop) e2).loopCount);
                    if (loopComp != 0) return loopComp;
                } else {
                    return 1;
                }
            } else if (e2 instanceof Loop) {
                return -1;
            }

            e1 = e1.root;
            e2 = e2.root;
        } while (e1 != null && e2 != null);

        return e1 == null ? e2 == null ? 0 : 1 : -1;
    }

    public static abstract class ContextBase extends CallContext {
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
        protected ContextBase latestFunctionCallInner() {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
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
                    31 * hashCode(root, nodeIdentifier));
        }

        @Override
        protected ContextBase latestFunctionCallInner() {
            return this;
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
        protected ContextBase latestFunctionCallInner() {
            return calledFrom;
        }

        private static int hashCode(CallContext root, NodeIdentifier nodeIdentifier, int loopCount) {
            return 31 * CallContext.hashCode(root, nodeIdentifier) + loopCount + 1;
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
}
