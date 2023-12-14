package com.oracle.truffle.sl.runtime.diffexec;

import com.google.common.hash.Hasher;
import com.oracle.truffle.api.strings.TruffleString;

public final class NodeIdentifier extends Hashable implements Comparable<NodeIdentifier> {
    private final String functionName;
    private final int number;
    private final boolean isNewNode;

    public NodeIdentifier(String functionName, int number, boolean isNewNode) {
        this.functionName = functionName;
        this.number = number;
        this.isNewNode = isNewNode;
    }

    public NodeIdentifier(TruffleString functionName, int number, boolean isNewNode) {
        this(functionName.toJavaStringUncached(), number, isNewNode);
    }

    public NodeIdentifier(TruffleString functionName, int number) {
        this(functionName, number, false);
    }

    public NodeIdentifier(String functionName, int number) {
        this(functionName, number, false);
    }

    public String getFunctionName() {
        return functionName;
    }

    public int getNumber() {
        return number;
    }

    public boolean isNewNode() {
        return isNewNode;
    }

    public static boolean equals(NodeIdentifier e1, NodeIdentifier e2) {
        if (e1.isNewNode != e2.isNewNode) return false;
        if (e1.number != e2.number) return false;
        return e1.functionName.equals(e2.functionName);
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
        return hasher.putUnencodedChars(functionName)
                .putInt(number)
                .putBoolean(isNewNode);
    }

    @Override
    public int compareTo(NodeIdentifier o) {
        if (this == o) return 0;
        final var compareNumber = Integer.compare(number, o.number);
        if (compareNumber != 0) return compareNumber;
        final var compareIsNew = Boolean.compare(isNewNode, o.isNewNode);
        if (compareIsNew != 0) return compareIsNew;
        return functionName.compareTo(o.functionName);
    }

    @Override
    public String toString() {
        return "NodeIdentifier{" +
                "functionName=" + functionName +
                ", number=" + number +
                ", isNewNode=" + isNewNode +
                '}';
    }
}
