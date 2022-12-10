package com.oracle.truffle.sl.runtime.cache;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeIdentifier)) return false;

        NodeIdentifier that = (NodeIdentifier) o;

        if (number != that.number) return false;
        if (isNew != that.isNew) return false;
        return functionName.equals(that.functionName);
    }

    @Override
    public int hashCode() {
        int result = functionName.hashCode();
        result = 31 * result + number;
        return 2 * result + (isNew ? 1 : 0);
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
