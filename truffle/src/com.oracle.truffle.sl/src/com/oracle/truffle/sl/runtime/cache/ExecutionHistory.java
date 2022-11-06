package com.oracle.truffle.sl.runtime.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class ExecutionHistory {
    private final ArrayList<ItemWithTime<ExecutionContext>> timeToContext = new ArrayList<>();
    private final HashMap<ExecutionContext, TimePair> contextToTime = new HashMap<>();
    private final ArrayList<ItemWithTime<ReadContent>> readMap = new ArrayList<>();
    private final HashMap<Integer, ExecutionContext> objectGenerationContext = new HashMap<>();
    private final HashMap<Integer, HashMap<String, ArrayList<ItemWithTime<Object>>>> objectUpdateMap = new HashMap<>();
    private final ArrayList<ItemWithTime<UpdateContent>> objectUpdateList = new ArrayList<>();
    private final ArrayList<ItemWithTime<Object>> returnedValueOrException = new ArrayList<>();

    public void onReturnValue(Time startTime, Time endTime, ExecutionContext ctx, Object value) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.put(ctx, new TimePair(startTime, endTime));
        returnedValueOrException.add(new ItemWithTime<>(endTime, replaceReference(value)));
    }

    public void onReturnExceptional(Time startTime, Time endTime, ExecutionContext ctx, RuntimeException throwable) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.put(ctx, new TimePair(startTime, endTime));
        returnedValueOrException.add(new ItemWithTime<>(endTime, throwable));
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
        onUpdateObjectInner(time, objectHash, fieldName, replaceReference(newValue));
    }

    public Time getFinishedTime(ExecutionContext ctx) {
        TimePair timePair = contextToTime.get(ctx);
        return timePair == null ? null : timePair.end;
    }

    public Object getReturnedValueOrThrow(Time time) {
        Object result = ItemWithTime.binarySearchJust(returnedValueOrException, time);

        if (result instanceof RuntimeException) throw (RuntimeException) result;

        return result;
    }

    public boolean didExecuted(ExecutionContext ctx) {
        return contextToTime.containsKey(ctx);
    }

    private void onUpdateObjectInner(Time time, int objectHash, String fieldName, Object newValue) {
        objectUpdateMap.computeIfAbsent(objectHash, it -> new HashMap<>())
                .computeIfAbsent(fieldName, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, newValue));
        objectUpdateList.add(new ItemWithTime<>(time, new UpdateContent(objectHash, fieldName, newValue)));
    }

    public Iterator<ItemWithTime<ReadContent>> getReadOperations(Time startTime, Time endTime) {
        return ItemsWithTimeIterator.create(readMap, startTime, endTime);
    }

    public Iterator<ItemWithTime<ReadContent>> getReadOperations(ExecutionContext ctx) {
        TimePair timePair = contextToTime.get(ctx);
        if (timePair == null) return null;
        return getReadOperations(timePair.start, timePair.end);
    }

    public Iterator<ItemWithTime<UpdateContent>> getUpdateOperations(Time startTime, Time endTime) {
        return ItemsWithTimeIterator.create(objectUpdateList, startTime, endTime);
    }

    public Iterator<ItemWithTime<UpdateContent>> getUpdateOperations(ExecutionContext ctx) {
        TimePair timePair = contextToTime.get(ctx);
        if (timePair == null) return null;
        return getUpdateOperations(timePair.start, timePair.end);
    }

    public void deleteRecords(Time startTime, Time endTime) {
        ItemWithTime.subList(readMap, startTime, endTime).clear();
        List<ItemWithTime<UpdateContent>> updates = ItemWithTime.subList(objectUpdateList, startTime, endTime);
        HashMap<Integer, HashSet<String>> updatedFields = new HashMap<>();
        for (ItemWithTime<UpdateContent> update : updates) {
            UpdateContent item = update.getItem();
            updatedFields.computeIfAbsent(item.getObjectId(), it -> new HashSet<>())
                    .add(item.getFieldName());
        }

        for (Map.Entry<Integer, HashSet<String>> entry : updatedFields.entrySet()) {
            Integer objectId = entry.getKey();
            HashSet<String> fields = entry.getValue();
            HashMap<String, ArrayList<ItemWithTime<Object>>> map = objectUpdateMap.get(objectId);
            for (String field : fields) {
                ArrayList<ItemWithTime<Object>> fieldHistory = map.get(field);
                ItemWithTime.subList(fieldHistory, startTime, endTime).clear();
            }
        }
        updates.clear();
        ItemWithTime.subList(returnedValueOrException, startTime, endTime).clear();
    }

    private static Object replaceReference(Object value) {
        Object saveNewValue;
        if (value == null
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Float
                || value instanceof Double
                || value instanceof Character
                || value instanceof String) {
            saveNewValue = value;
        } else {
            saveNewValue = new ObjectReference(System.identityHashCode(value));
        }

        return saveNewValue;
    }

    public static class TimePair {
        private final Time start;
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

        @Override
        public String toString() {
            return "ReadArgument{" +
                    "callContext=" + Arrays.toString(callContext) +
                    ", argumentName=" + argumentName +
                    '}';
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

        @Override
        public String toString() {
            return "ReadLocalVariable{" +
                    "callContext=" + Arrays.toString(callContext) +
                    ", variableName=" + variableName +
                    '}';
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

        @Override
        public String toString() {
            return "ReadObjectField{" +
                    "objectId=" + objectId +
                    ", fieldName=" + fieldName +
                    '}';
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

        private ItemsWithTimeIterator(ArrayList<ItemWithTime<T>> items, int initIndex, ItemWithTime<T> initItem, Time endTime) {
            this.items = items;
            this.cursor = initIndex;
            this.currentItem = initItem;
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

        private static <T> Iterator<ItemWithTime<T>> create(ArrayList<ItemWithTime<T>> items, Time startTime, Time endTime) {
            int initIndex = ItemWithTime.binarySearch(items, startTime);
            if (items.size() == initIndex) return noElementIterator();
            ItemWithTime<T> initItem = items.get(initIndex);
            if (initItem.getTime().compareTo(endTime) > 0) return noElementIterator();
            return new ItemsWithTimeIterator<>(items, initIndex, initItem, endTime);
        }
    }

    private static final Object noElementIteratorInner = new Iterator<ItemWithTime<Object>>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public ItemWithTime<Object> next() {
            throw new NoSuchElementException();
        }
    };

    @SuppressWarnings("unchecked")
    private static <T> Iterator<ItemWithTime<T>> noElementIterator() {
        return (Iterator<ItemWithTime<T>>) noElementIteratorInner;
    }
}
