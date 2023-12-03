package com.oracle.truffle.sl.runtime.diffexec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public record ItemWithTime<TIME extends Time<TIME>, E>(TIME time, E item) {

    public static <TIME extends Time<TIME>, E> int binarySearchWhereInsertTo(ArrayList<ItemWithTime<TIME, E>> list, TIME time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final TIME midVal = list.get(mid).time;
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

    public static <TIME extends Time<TIME>, E> int binarySearchNext(ArrayList<ItemWithTime<TIME, E>> list, TIME time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final TIME midVal = list.get(mid).time;
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

    public static <TIME extends Time<TIME>, E> int binarySearchPrev(ArrayList<ItemWithTime<TIME, E>> list, TIME time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final TIME midVal = list.get(mid).time;
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

    public static <TIME extends Time<TIME>, E> int binarySearchApproximately(ArrayList<ItemWithTime<TIME, E>> list, TIME time) {
        int low = 0;
        int high = list.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            ItemWithTime<TIME, E> midVal = list.get(mid);
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

    public static <TIME extends Time<TIME>, E> int binarySearchApply(ArrayList<ItemWithTime<TIME, E>> list, TIME time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final TIME midVal = list.get(mid).time;
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

    public static <TIME extends Time<TIME>, E> E binarySearchJust(ArrayList<ItemWithTime<TIME, E>> list, TIME time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final ItemWithTime<TIME, E> midEntry = list.get(mid);
            final TIME midVal = midEntry.time;
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

    public static <TIME extends Time<TIME>, E> int binarySearchJustIndex(ArrayList<ItemWithTime<TIME, E>> list, TIME time) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final ItemWithTime<TIME, E> midEntry = list.get(mid);
            final TIME midVal = midEntry.time;
            int cmp = midVal.compareTo(time);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        throw new NoSuchElementException();
    }

    /** both inclusive */
    public static <TIME extends Time<TIME>, E> List<ItemWithTime<TIME, E>> subList(ArrayList<ItemWithTime<TIME, E>> list, TIME startTime, TIME endTime) {
        final int start = binarySearchWhereInsertTo(list, startTime);
        final int end = binarySearchNext(list, endTime);
        return list.subList(start, end);
    }

    public static <TIME extends Time<TIME>, E> ArrayList<ItemWithTime<TIME, E>> merge(ArrayList<ItemWithTime<TIME, E>> base, ArrayList<ItemWithTime<TIME, E>> newList, TIME initialTime) {
        if (newList.isEmpty()) return base;
        if (base.isEmpty()) return newList;
        final int i = binarySearchWhereInsertTo(base, initialTime);
        base.addAll(i, newList);
        return base;
    }
}
