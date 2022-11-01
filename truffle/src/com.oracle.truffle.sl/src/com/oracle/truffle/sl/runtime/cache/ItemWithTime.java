package com.oracle.truffle.sl.runtime.cache;

import java.util.ArrayList;
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

    // TODO: Should be tested
    public static <T> int binarySearch(ArrayList<ItemWithTime<T>> list, Time time) {
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

    public static <T> List<ItemWithTime<T>> subList(ArrayList<ItemWithTime<T>> list, Time startTIme, Time endTime) {
        int startIndex = binarySearch(list, startTIme);
        int endIndex = binarySearch(list, endTime);
        return list.subList(startIndex, endIndex);
    }
}
