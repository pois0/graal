package com.oracle.truffle.sl.runtime.diffexec;

import com.google.common.hash.Hasher;

public final class NodeIdentifier extends Hashable implements Comparable<NodeIdentifier> {
    private final int functionNumber;
    private final int number;
    private final boolean isNewNode;

    public NodeIdentifier(int functionNumber, int number, boolean isNewNode) {
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
        return e1.number == e2.number && e1.functionNumber == e2.functionNumber && e1.isNewNode == e2.isNewNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeIdentifier ni)) return false;
        return equals(this, ni);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Hasher hash(Hasher hasher) {
        return hasher.putInt(functionNumber)
                .putInt(number)
                .putBoolean(isNewNode);
    }

    @Override
    public int compareTo(NodeIdentifier o) {
        final var nComp = Integer.compare(number, o.number);
        if (nComp != 0) return nComp;
        final var fnComp = Integer.compare(functionNumber, o.functionNumber);
        if (fnComp != 0) return fnComp;
        return Boolean.compare(isNewNode, o.isNewNode);
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
