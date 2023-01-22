package com.oracle.truffle.sl.runtime.cache;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        } else {
            final int mismatch = mismatch(raw, next.raw);
            final int[] newRaw = Arrays.copyOf(raw, mismatch + 2);
            switch (raw.length - mismatch) {
            case 0:
                newRaw[mismatch] = next.raw[mismatch] - 1;
                break;
            case 1:
                newRaw[mismatch + 1]++;
                break;
            }
            return new Time(newRaw);
        }
    }

    @Override
    public int compareTo(Time o) {
        int[] thisRaw = this.raw;
        int[] thatRaw = o.raw;

        final int mismatch = mismatch(thisRaw, thatRaw);
        if (mismatch < 0) {
            return 0;
        } else if (mismatch == thisRaw.length) {
            return -1;
        } else if (mismatch == thatRaw.length) {
            return 1;
        } else {
            return Integer.compare(thisRaw[mismatch], thatRaw[mismatch]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Time)) return false;

        Time time = (Time) o;

        return Arrays.equals(raw, time.raw);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public int hashCode() {
        final Hasher hasher = Hashing.murmur3_32_fixed().newHasher();
        for (int e : raw) hasher.putInt(e);
        return hasher.hash().asInt();
    }

    @Override
    public String toString() {
        return "Time{" +
                "raw=" + Arrays.toString(raw) +
                '}';
    }

    public static int binarySearchWhereInsertTo(ArrayList<Time> list, Time time) {
        final int i = Collections.binarySearch(list, time);
        return i < 0 ? -i - 1 : i;
    }

    public static int binarySearchNext(ArrayList<Time> list, Time time) {
        final int i = Collections.binarySearch(list, time);
        return i < 0 ? -i - 1 : i + 1;
    }

    public static List<Time> subList(ArrayList<Time> list, Time startTime, Time endTime) {
        final int start = binarySearchWhereInsertTo(list, startTime);
        final int end = binarySearchNext(list, endTime);
        return list.subList(start, end);
    }

    public static ArrayList<Time> merge(ArrayList<Time> base, ArrayList<Time> newList, Time initialTime) {
        if (newList.isEmpty()) return base;
        final int i = binarySearchWhereInsertTo(base, initialTime);
        base.addAll(i, newList);
        return base;
    }

    private static int mismatch(int[] a, int[] b) {
        int minLength = Math.min(a.length, b.length);
        for (int i = 0; i < minLength; i++) if (a[i] != b[i]) return i;
        return a.length == b.length ? -1 : minLength;
    }
}
