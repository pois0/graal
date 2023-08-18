package com.oracle.truffle.sl.runtime.diffexec;

public abstract class Time<T extends Time<?>> extends Hashable implements Comparable<T> {
    public abstract T inc();
    public abstract T mid(T next);
}
