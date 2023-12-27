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

    public static CallContext functionCall(CallContext root, NodeIdentifier calleeIdentifier) {
        return new CallContext(
                root,
                calleeIdentifier,
                -1
        );
    }

    public static CallContext loop(CallContext root, NodeIdentifier identifier, int loopCount) {
        return new CallContext(
                root,
                identifier,
                loopCount
        );
    }

    public static CallContext loop(CallContext root, NodeIdentifier identifier) {
        return new CallContext(
                root,
                identifier,
                0
        );
    }

    public static CallContext loopNextIter(CallContext currentIter) {
        assert currentIter.loopCount >= 0;
        return new CallContext(
                currentIter.root,
                currentIter.nodeIdentifier,
                currentIter.loopCount + 1
        );
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

    public int depth() {
        return root.depth() + 1;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CallContext)) return false;

        return equals(this, (CallContext) o);
    }

    @Override
    public int compareTo(CallContext o) {
        return compare(this, o);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Hasher hashCode(CallContext root, NodeIdentifier nodeIdentifier) {
        return nodeIdentifier.hash(Hashing.murmur3_32_fixed().newHasher())
                .putInt(root != null ? root.hashCode() : 0);
    }

    public static boolean equals(CallContext e1, CallContext e2) {
        return compare(e1, e2) == 0;
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
