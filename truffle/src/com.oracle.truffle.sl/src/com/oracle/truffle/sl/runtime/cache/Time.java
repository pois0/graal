package com.oracle.truffle.sl.runtime.cache;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class Time implements Comparable<Time> {
    private final int[] raw;

    private Time(int[] raw) {
        this.raw = raw;
    }

    public static Time zero() {
        return new Time(new int[1]);
    }

    public Time inc() {
        int[] newRaw = Arrays.copyOf(raw, raw.length);
        newRaw[raw.length - 1]++;
        return new Time(newRaw);
    }

    public Time mid(Time next) {
        if (raw.length > next.raw.length) {
            final int newLength = next.raw.length + 1;
            final int[] newRaw = Arrays.copyOf(raw, newLength);
            newRaw[newLength - 1]++;
            return new Time(newRaw);
        } else if (raw.length < next.raw.length) {
            final int mismatch = Arrays.mismatch(raw, next.raw);
            final int[] newRaw = Arrays.copyOf(raw, mismatch + 2);
            if (mismatch == raw.length) {
                newRaw[mismatch] = next.raw[mismatch] - 1;
            } else {
                newRaw[mismatch + 1]++;
            }
            return new Time(newRaw);
        } else {
            final int mismatch = Arrays.mismatch(raw, next.raw);
            if (mismatch < 0) {
                return new Time(Arrays.copyOf(raw, raw.length + 1));
            } else {
                final int[] newRaw = Arrays.copyOf(raw, mismatch + 1);
                newRaw[mismatch]++;
                return new Time(newRaw);
            }
        }
    }

    @Override
    public int compareTo(Time o) {
        int[] thisRaw = this.raw;
        int[] thatRaw = o.raw;

        for (int i = 0; true; i++) {
            if (i == thisRaw.length) {
                if (i == thatRaw.length) return 0;
                return -1;
            }
            if (i == thatRaw.length) return 1;

            int cmp = Integer.compare(thisRaw[i], thatRaw[i]);
            if (cmp != 0) return cmp;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Time)) return false;

        Time time = (Time) o;

        return Arrays.equals(raw, time.raw);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(raw);
    }

    @Override
    public String toString() {
        return "Time{" +
                "raw=" + Arrays.toString(raw) +
                '}';
    }

    public static <T> int binarySearchWhereInsertTo(ArrayList<Time> list, Time time) {
        final int i = Collections.binarySearch(list, time);
        return i < 0 ? -i - 1 : i;
    }

    public static <T> int binarySearchNext(ArrayList<Time> list, Time time) {
        final int i = Collections.binarySearch(list, time);
        return i < 0 ? -i - 1 : i + 1;
    }

    public static ArrayList<Time> merge(ArrayList<Time> base, ArrayList<Time> newList, Time initialTime) {
        if (newList.isEmpty()) return base;
        final int i = binarySearchWhereInsertTo(newList, initialTime);
        base.addAll(i, newList);
        return base;
    }
}
