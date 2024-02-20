package com.oracle.truffle.sl.runtime.diffexec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Time<T extends Time<?>> extends Hashable implements Comparable<T> {
    public abstract T inc();
    public abstract T incAndSimplify();
    public abstract T mid(T next);

    public static <T extends Time<T>> T max(T t1, T t2) {
        return t1.compareTo(t2) >= 0 ? t1 : t2;
    }

    public static <T extends Time<T>> int binarySearchWhereInsertTo(ArrayList<T> list, T time) {
        final int i = Collections.binarySearch(list, time);
        return i < 0 ? -i - 1 : i;
    }

    public static <T extends Time<T>> int binarySearchNext(ArrayList<T> list, T time) {
        final int i = Collections.binarySearch(list, time);
        return i < 0 ? -i - 1 : i + 1;
    }

    public static <T extends Time<T>> boolean existsRecord(ArrayList<T> list, T start, T end) {
        final int i = binarySearchWhereInsertTo(list, start);
        if (i == list.size()) return false;
        T time = list.get(i);
        return time.compareTo(end) <= 0;
    }

    public static <T extends Time<T>> List<T> subList(ArrayList<T> list, T startTime, T endTime) {
        final int start = binarySearchWhereInsertTo(list, startTime);
        final int end = binarySearchNext(list, endTime);
        return list.subList(start, end);
    }

    public static <T extends Time<T>> List<T> subListSince(ArrayList<T> list, T startTime) {
        final int start = binarySearchNext(list, startTime);
        return list.subList(start, list.size());
    }

    public static <T extends Time<T>> ArrayList<T> merge(ArrayList<T> base, ArrayList<T> newList, T initialTime) {
        if (newList.isEmpty()) return base;
        final int i = binarySearchWhereInsertTo(base, initialTime);
        base.addAll(i, newList);
        return base;
    }
}
