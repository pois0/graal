package com.oracle.truffle.sl.runtime.diffexec;

public record ItemWithTime<T>(ArrayTime time, T item) {}
