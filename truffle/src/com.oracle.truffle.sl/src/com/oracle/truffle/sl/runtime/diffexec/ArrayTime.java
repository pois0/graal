package com.oracle.truffle.sl.runtime.diffexec;

import com.google.common.hash.Hasher;

import java.util.Arrays;

public final class ArrayTime extends Time<ArrayTime> implements Comparable<ArrayTime> {
    private final int[] raw;

    public static final ArrayTime ZERO = new ArrayTime(new int[]{0});

    private ArrayTime(int[] raw) {
        this.raw = raw;
    }

    @Override
    public ArrayTime inc() {
        int[] newRaw = Arrays.copyOf(raw, raw.length);
        newRaw[raw.length - 1]++;
        return new ArrayTime(newRaw);
    }

    @Override
    public ArrayTime incAndSimplify() {
        return new ArrayTime(new int[]{raw[0] + 1});
    }

    @Override
    public ArrayTime mid(ArrayTime next) {
        if (raw.length > next.raw.length) {
            final int newLength = next.raw.length + 1;
            final int[] newRaw = Arrays.copyOf(raw, newLength);
            newRaw[newLength - 1]++;
            return new ArrayTime(newRaw);
        } else {
            final int mismatch = Arrays.mismatch(raw, next.raw);
            final int[] newRaw = Arrays.copyOf(raw, mismatch + 2);
            switch (raw.length - mismatch) {
                case 0 -> newRaw[mismatch] = next.raw[mismatch] - 1;
                case 1 -> newRaw[mismatch + 1]++;
            }
            return new ArrayTime(newRaw);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Hasher hash(Hasher hasher) {
        for (int e : raw) hasher.putInt(e);
        return hasher;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayTime t)) return false;
        return Arrays.equals(raw, t.raw);
    }

    @Override
    public int compareTo(ArrayTime o) {
        return Arrays.compare(raw, o.raw);
    }

    @Override
    public String toString() {
        return "ArrayTime{" +
                "raw=" + Arrays.toString(raw) +
                '}';
    }
}
