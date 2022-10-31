package com.oracle.truffle.sl.runtime.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class ExecutionHistory {
    private final ArrayList<ExecutionContext> timeToContext = new ArrayList<>();
    private final HashMap<ExecutionContext, TimePair> contextToTime = new HashMap<>();
    private final ArrayList<ItemWithTime<ReadContent>> readMap = new ArrayList<>();
    private final HashMap<Integer, ExecutionContext> objectGenerationContext = new HashMap<>();
    private final HashMap<Integer, HashMap<String, ArrayList<ItemWithTime<Object>>>> objectUpdateMap = new HashMap<>();
    private final ArrayList<ItemWithTime<UpdateContent>> objectUpdateList = new ArrayList<>();

    public void onTick(Time startTime, Time endTime, ExecutionContext ctx) {
        timeToContext.add(ctx);
        contextToTime.put(ctx, new TimePair(startTime, endTime));
    }

    public void onReadArgument(Time time, CallContextElement[] ctx, Object argumentName) {
        readMap.add(new ItemWithTime<>(time, new ReadArgument(ctx, argumentName)));
    }

    public void onReadLocalVariable(Time time, CallContextElement[] ctx, Object variableName) {
        readMap.add(new ItemWithTime<>(time, new ReadLocalVariable(ctx, variableName)));
    }

    public void onReadObjectField(Time time, Object object, Object field) {
        readMap.add(new ItemWithTime<>(time, new ReadObjectField(System.identityHashCode(object), field)));
    }

    public void onGenerateObject(ExecutionContext ctx, Object object) {
        objectGenerationContext.put(System.identityHashCode(object), ctx);
    }

    public void onUpdateObject(Time time, Object object, String fieldName, Object newValue) {
        onUpdateObjectWithHash(time, System.identityHashCode(object), fieldName, newValue);
    }

    public void onUpdateObjectWithHash(Time time, int objectHash, String fieldName, Object newValue) {
        Object saveNewValue;
        if (newValue == null
                || newValue instanceof Byte
                || newValue instanceof Short
                || newValue instanceof Integer
                || newValue instanceof Long
                || newValue instanceof Float
                || newValue instanceof Double
                || newValue instanceof Character
                || newValue instanceof String) {
            saveNewValue = newValue;
        } else {
            saveNewValue = new ObjectReference(System.identityHashCode(newValue));
        }

        onUpdateObjectInner(time, objectHash, fieldName, saveNewValue);
    }

    private void onUpdateObjectInner(Time time, int objectHash, String fieldName, Object newValue) {
        objectUpdateMap.computeIfAbsent(objectHash, it -> new HashMap<>())
                .computeIfAbsent(fieldName, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, newValue));
        objectUpdateList.add(new ItemWithTime<>(time, new UpdateContent(objectHash, fieldName, newValue)));
    }

    public Iterator<ItemWithTime<ReadContent>> getReadOperations(Time startTime, Time endTime) {
        return new ItemsWithTimeIterator<>(readMap, startTime, endTime);
    }

    public Iterator<ItemWithTime<ReadContent>> getReadOperations(ExecutionContext ctx) {
        TimePair timePair = contextToTime.get(ctx);
        return getReadOperations(timePair.start, timePair.end);
    }

    public Iterator<ItemWithTime<UpdateContent>> getUpdateOperations(Time startTime, Time endTime) {
        return new ItemsWithTimeIterator<>(objectUpdateList, startTime, endTime);
    }

    public Iterator<ItemWithTime<UpdateContent>> getUpdateOperations(ExecutionContext ctx) {
        TimePair timePair = contextToTime.get(ctx);
        return getUpdateOperations(timePair.start, timePair.end);
    }

    public static class TimePair {
        private final Time  start;
        private final Time end;

        public TimePair(Time start, Time end) {
            this.start = start;
            this.end = end;
        }

        public Time getStart() {
            return start;
        }

        public Time getEnd() {
            return end;
        }
    }

    public static abstract class ReadContent {}

    public static class ReadArgument extends ReadContent {
        private final CallContextElement[] callContext;
        private final Object argumentName;

        public ReadArgument(CallContextElement[] callContext, Object argumentName) {
            this.callContext = callContext;
            this.argumentName = argumentName;
        }

        public CallContextElement[] getCallContext() {
            return callContext;
        }

        public Object getArgumentName() {
            return argumentName;
        }
    }

    public static class ReadLocalVariable extends ReadContent {
        private final CallContextElement[] callContext;
        private final Object variableName;

        public ReadLocalVariable(CallContextElement[] callContext, Object variableName) {
            this.callContext = callContext;
            this.variableName = variableName;
        }

        public CallContextElement[] getCallContext() {
            return callContext;
        }

        public Object getVariableName() {
            return variableName;
        }
    }

    public static class ReadObjectField extends ReadContent {
        private final int objectId;
        private final Object fieldName;

        public ReadObjectField(int objectId, Object fieldName) {
            this.objectId = objectId;
            this.fieldName = fieldName;
        }

        public int getObjectId() {
            return objectId;
        }

        public Object getFieldName() {
            return fieldName;
        }
    }

    public static class UpdateContent {
        private final int objectId;
        private final String fieldName;
        private final Object newValue;

        public UpdateContent(int objectId, String fieldName, Object newValue) {
            this.objectId = objectId;
            this.fieldName = fieldName;
            this.newValue = newValue;
        }

        public int getObjectId() {
            return objectId;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Object getNewValue() {
            return newValue;
        }
    }

    public static class ObjectReference {
        private final int referenceHash;

        public ObjectReference(int referenceHash) {
            this.referenceHash = referenceHash;
        }

        public int getReferenceHash() {
            return referenceHash;
        }
    }

    private static class ItemsWithTimeIterator<T> implements Iterator<ItemWithTime<T>> {
        private final ArrayList<ItemWithTime<T>> items;
        private int cursor;
        private ItemWithTime<T> currentItem;
        private final Time endTime;

        private ItemsWithTimeIterator(ArrayList<ItemWithTime<T>> items, Time startTime, Time endTime) {
            this.cursor = ItemWithTime.binarySearch(items, startTime);
            this.items = items;
            this.endTime = endTime;
        }

        @Override
        public boolean hasNext() {
            if (currentItem != null) return true;
            if (cursor == items.size()) return false;

            ItemWithTime<T> candidate = items.get(cursor++);
            if (endTime.compareTo(candidate.getTime()) < 0) return false;

            currentItem = candidate;
            return true;
        }

        @Override
        public ItemWithTime<T> next() {
            ItemWithTime<T> result = currentItem;
            if (result == null) {
                if (!hasNext()) throw new NoSuchElementException();

                result = currentItem;
            }

            currentItem = null;

            return result;
        }
    }
}
