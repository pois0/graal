package com.oracle.truffle.sl.runtime.cache;

import com.google.common.hash.Hasher;

public final class NodeIdentifier implements Comparable<NodeIdentifier> {
    private final String functionName;
    private final int number;
    private final boolean isNew;

    public NodeIdentifier(String functionName, int number, boolean isNew) {
        this.functionName = functionName;
        this.number = number;
        this.isNew = isNew;
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

    public boolean isNew() {
        return isNew;
    }

    public static boolean equals(NodeIdentifier e1, NodeIdentifier e2) {
        if (e1.number != e2.number) return false;
        if (e1.isNew != e2.isNew) return false;
        return e1.functionName.equals(e2.functionName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeIdentifier)) return false;

        return equals(this, (NodeIdentifier) o);
    }

    @Override
    public int hashCode() {
        int result = functionName.hashCode();
        result = 31 * result + number;
        return 2 * result + (isNew ? 1 : 0);
    }

    public Hasher hash(Hasher hasher) {
        return hasher.putInt(functionName.hashCode())
                .putInt(number)
                .putBoolean(isNew);
    }

    @Override
    public String toString() {
        return "NodeIdentifier{" + functionName + '/' + isNew + '/' + number + '}';
    }

    @Override
    public int compareTo(NodeIdentifier o) {
        if (this == o) return 0;
        final int compareNumber = Integer.compare(number, o.number);
        if (compareNumber != 0) return compareNumber;
        int compareIsNew = Boolean.compare(isNew, o.isNew);
        if (compareIsNew != 0) return compareIsNew;
        return functionName.compareTo(o.functionName);
    }
}
