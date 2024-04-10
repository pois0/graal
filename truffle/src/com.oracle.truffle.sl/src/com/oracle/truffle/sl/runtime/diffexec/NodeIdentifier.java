package com.oracle.truffle.sl.runtime.diffexec;

import java.util.HashMap;

public final class NodeIdentifier implements Comparable<NodeIdentifier> {
    private final int functionNumber;
    private final int number;
    private final boolean isNewNode;

    private static final HashMap<Integer, HashMap<Integer, NodeIdentifier>[]> nodeIdentifierCache = new HashMap<>();

    private NodeIdentifier(int functionNumber, int number, boolean isNewNode) {
        this.functionNumber = functionNumber;
        this.number = number;
        this.isNewNode = isNewNode;
    }

    public int getFunctionNumber() {
        return functionNumber;
    }

    public int getNumber() {
        return number;
    }

    public boolean isNewNode() {
        return isNewNode;
    }

    public static boolean equals(NodeIdentifier e1, NodeIdentifier e2) {
        return e1 == e2;
//        return e1.number == e2.number && e1.functionNumber == e2.functionNumber && e1.isNewNode == e2.isNewNode;
    }

    public static NodeIdentifier create(int functionNumber, int number, boolean isNewNode) {
        final var functionCache = nodeIdentifierCache.computeIfAbsent(functionNumber, it -> new HashMap[2]);
        int i = isNewNode ? 1 : 0;
        var cache = functionCache[i];
        if (cache == null) {
            cache = functionCache[i] = new HashMap<>();
        }
        return (NodeIdentifier) cache.computeIfAbsent(number, it -> new NodeIdentifier(functionNumber, number, isNewNode));
    }

    public static void clearCache() {
        nodeIdentifierCache.clear();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int compareTo(NodeIdentifier o) {
        return Integer.compare(System.identityHashCode(this), System.identityHashCode(o));
    }

    @Override
    public String toString() {
        return "NodeIdentifier{" +
                "functionName=" + functionNumber +
                ", number=" + number +
                ", isNewNode=" + isNewNode +
                '}';
    }
}
