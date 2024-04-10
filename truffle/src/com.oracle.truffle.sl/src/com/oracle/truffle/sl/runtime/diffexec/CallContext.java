package com.oracle.truffle.sl.runtime.diffexec;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public final class CallContext implements Comparable<CallContext> {
    private final NodeIdentifier nodeIdentifier;
    private final CallContext root;
    private final int loopCount;
    private final int hashCode;

    public static final CallContext EXECUTION_BASE = new CallContext(null, null, -1, 0);

    private CallContext(CallContext root, NodeIdentifier nodeIdentifier, int loopCount, int hashCode) {
        this.root = root;
        this.nodeIdentifier = nodeIdentifier;
        this.loopCount = loopCount;
        this.hashCode = hashCode;
    }

    @SuppressWarnings("UnstableApiUsage")
    private CallContext(CallContext root, NodeIdentifier nodeIdentifier, int loopCount) {
        this(root, nodeIdentifier, loopCount, hashCode(root, nodeIdentifier).putInt(loopCount).hash().asInt());
    }

    public CallContext functionCall(NodeIdentifier calleeIdentifier) {
        return new CallContext(
                this,
                calleeIdentifier,
                -1
        );
    }

    public CallContext loop(NodeIdentifier identifier, int loopCount) {
        return new CallContext(
                this,
                identifier,
                loopCount
        );
    }

    public CallContext loop(NodeIdentifier identifier) {
        return loop(identifier, 0);
    }

    public CallContext loopNextIter() {
        assert loopCount >= 0;
        return root.loop(nodeIdentifier, loopCount + 1);
    }

    public CallContext getRoot() {
        return root;
    }

    public NodeIdentifier getNodeIdentifier() {
        return nodeIdentifier;
    }

    public CallContext getBase() {
        return loopCount < 0 ? this : root.getBase();
    }

    public boolean isExecutionBase() {
        return loopCount < 0;
    }

    public boolean isLoop() {
        return loopCount >= 0;
    }

    public boolean isFunctionCall() {
        return loopCount < 0 && root != null;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CallContext && equals(this, (CallContext) o);
    }

    @Override
    public int compareTo(CallContext o) {
        return compare(this, o);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Hasher hashCode(CallContext root, NodeIdentifier nodeIdentifier) {
        return Hashing.murmur3_32_fixed().newHasher()
                .putInt(nodeIdentifier.hashCode())
                .putInt(root != null ? root.hashCode() : 0);
    }

    public static boolean equals(CallContext e1, CallContext e2) {
        while (true) {
            if (e1 == e2) return true;
            if (!NodeIdentifier.equals(e1.nodeIdentifier, e2.nodeIdentifier)) return false;
            if (e1.loopCount != e2.loopCount) return false;

            e1 = e1.root;
            e2 = e2.root;

            if (e1 == null) {
                return e2 == null;
            } else if (e2 == null) {
                return false;
            }
        }
    }

    private static int compare(CallContext e1, CallContext e2) {
        while (true) {
            if (e1 == e2) return 0;
            final var niComp = e1.nodeIdentifier.compareTo(e2.nodeIdentifier);
            if (niComp != 0) return niComp;
            final var lcComp = Integer.compare(e1.loopCount, e2.loopCount);
            if (lcComp != 0) return lcComp;

            e1 = e1.root;
            e2 = e2.root;

            if (e1 == null) {
                return e2 == null ? 0 : 1;
            } else if (e2 == null) {
                return -1;
            }
        }
    }
}
