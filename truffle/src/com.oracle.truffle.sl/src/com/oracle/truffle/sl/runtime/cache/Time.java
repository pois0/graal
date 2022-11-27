package com.oracle.truffle.sl.runtime.cache;

import java.util.Arrays;

public class Time implements Comparable<Time>{
    private final int[] raw;

    private Time(int[] raw) {
        this.raw = raw;
    }

    public static Time zero() {
        return new Time(new int[0]);
    }

    public Time inc() {
        int[] thisRaw = this.raw;
        int[] newRaw = Arrays.copyOf(thisRaw, thisRaw.length);
        newRaw[thisRaw.length - 1]++;
        return new Time(newRaw);
    }

    public Time carryAndInc() {
        int[] raw = this.raw;
        int[] newRaw = Arrays.copyOf(raw, raw.length - 1);
        newRaw[newRaw.length - 1]++;
        return new Time(newRaw);
    }

    public Time carryAllAndInc() {
        int[] raw = this.raw;
        return new Time(new int[]{raw[0] + 1});
    }

    public Time subdivide() {
        int[] thisRaw = this.raw;
        return new Time(Arrays.copyOf(thisRaw, thisRaw.length + 1));
    }

    @Override
    public int compareTo(Time o) {
        int[] thisRaw = this.raw;
        int[] thatRaw = o.raw;

        for (int i = 0; true; i++) {
            if (i == thisRaw.length) return -1;
            if (i == thatRaw.length) return 1;

            int cmp = Integer.compare(thisRaw[i], thatRaw[i]);
            if (cmp != 0) return cmp;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Time time = (Time) o;

        return Arrays.equals(raw, time.raw);
    }

    @Override
    public int hashCode() {
        if (raw.length == 0) return 0;
        return Arrays.hashCode(raw);
    }

    @Override
    public String toString() {
        return "Time{" +
                "raw=" + Arrays.toString(raw) +
                '}';
    }
}
