package com.oracle.truffle.sl.runtime.diffexec;

import java.util.HashMap;

public final class CachedCallContext implements Comparable<CachedCallContext> {
    private final NodeIdentifier nodeIdentifier;
    private final CachedCallContext root;
    private final int loopCount;

    private final HashMap<NodeIdentifier, CachedCallContext> childFunctionsCalls = new HashMap<>();
    private final HashMap<NodeIdentifier, CachedCallContext> childLoops = new HashMap<>();
    private CachedCallContext nextLoopIter = null;

    private CachedCallContext(CachedCallContext root, NodeIdentifier nodeIdentifier, int loopCount) {
        this.root = root;
        this.nodeIdentifier = nodeIdentifier;
        this.loopCount = loopCount;
    }

    public static CachedCallContext executionBase() {
        return new CachedCallContext(null, null, -1);
    }

    public CachedCallContext functionCall(NodeIdentifier calleeIdentifier) {
        return childFunctionsCalls
                .computeIfAbsent(calleeIdentifier, ident -> new CachedCallContext(this, calleeIdentifier, -1));
    }

    public CachedCallContext loop(NodeIdentifier identifier) {
        return childLoops.computeIfAbsent(identifier, it -> new CachedCallContext(this, identifier, 0));
    }

    public CachedCallContext loopNextIter() {
        if (nextLoopIter != null) return nextLoopIter;

        return nextLoopIter = new CachedCallContext(root, nodeIdentifier, loopCount + 1);
    }

    public CachedCallContext getRoot() {
        return root;
    }

    public NodeIdentifier getNodeIdentifier() {
        return nodeIdentifier;
    }

    public CachedCallContext getBase() {
        return isExecutionBase() ? this : root.getBase();
    }

    public boolean isExecutionBase() {
        return loopCount < 0;
    }

    public boolean isLoop() {
        return loopCount >= 0;
    }

    public boolean isFunctionCall() {
        return isExecutionBase() && root != null;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int compareTo(CachedCallContext o) {
        return Integer.compare(System.identityHashCode(this), System.identityHashCode(o));
    }

    public static boolean equals(CachedCallContext e1, CachedCallContext e2) {
        return e1 == e2;
    }

}
