package com.oracle.truffle.sl.runtime.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemWithTime<T> {
    private final Time time;
    private final T item;

    public ItemWithTime(Time time, T item) {
        this.time = time;
        this.item = item;
    }

    public Time getTime() {
        return time;
    }

    public T getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "ItemWithTime{" +
                "time=" + time +
                ", item=" + item +
                '}';
    }

    public static <T> int binarySearchWhereInsertTo(ArrayList<ItemWithTime<T>> list, Time time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final Time midVal = list.get(mid).time;
            int cmp = midVal.compareTo(time);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }

        return low;
    }

    public static <T> int binarySearchNext(ArrayList<ItemWithTime<T>> list, Time time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final Time midVal = list.get(mid).time;
            int cmp = midVal.compareTo(time);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid + 1; // key found
        }

        return low;
    }

    public static <T> int binarySearchPrev(ArrayList<ItemWithTime<T>> list, Time time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final Time midVal = list.get(mid).time;
            int cmp = midVal.compareTo(time);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid - 1; // key found
        }

        return low - 1;
    }

    public static <T> int binarySearchApproximately(ArrayList<ItemWithTime<T>> list, Time time) {
        int low = 0;
        int high = list.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            ItemWithTime<T> midVal = list.get(mid);
            int cmp = midVal.time.compareTo(time);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    public static <T> int binarySearchApply(ArrayList<ItemWithTime<T>> list, Time time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final Time midVal = list.get(mid).time;
            int cmp = midVal.compareTo(time);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return high;
    }

    public static <T> T binarySearchJust(ArrayList<ItemWithTime<T>> list, Time time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final ItemWithTime<T> midEntry = list.get(mid);
            final Time midVal = midEntry.time;
            int cmp = midVal.compareTo(time);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return midEntry.item; // key found
        }
        return null;
    }

    public static <T> int binarySearchJustIndex(ArrayList<ItemWithTime<T>> list, Time time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final ItemWithTime<T> midEntry = list.get(mid);
            final Time midVal = midEntry.time;
            int cmp = midVal.compareTo(time);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -1;
    }

    /** both inclusive */
    public static <T> List<ItemWithTime<T>> subList(ArrayList<ItemWithTime<T>> list, Time startTime, Time endTime) {
        final int start = binarySearchWhereInsertTo(list, startTime);
        final int end = binarySearchNext(list, endTime);
        return list.subList(start, end);
    }

    public static <T> ArrayList<ItemWithTime<T>> merge(ArrayList<ItemWithTime<T>> base, ArrayList<ItemWithTime<T>> newList, Time initialTime) {
        if (newList.isEmpty()) return base;
        final int i = binarySearchWhereInsertTo(base, initialTime);
        base.addAll(i, newList);
        return base;
    }
}
