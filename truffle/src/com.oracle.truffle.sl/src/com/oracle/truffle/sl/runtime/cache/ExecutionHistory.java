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
    private final HashMap<ExecutionContext, HashMap<String, ArrayList<ItemWithTime<Object>>>> objectUpdateMap = new HashMap<>();
    private final ArrayList<ItemWithTime<ExecutionHistoryOperator.ObjectUpdate>> objectUpdateList = new ArrayList<>();
    private final HashMap<CallContextElement.FunctionCallArray, HashMap<String, ArrayList<ItemWithTime<Object>>>> localVariableUpdateMap = new HashMap<>();
    private final HashMap<CallContextElement.FunctionCallArray, ArrayList<ItemWithTime<LocalVariableUpdate>>> localVariableUpdateList = new HashMap<>();
    final ArrayList<ItemWithTime<String>> functionCalls = new ArrayList<>();
    final ArrayList<ItemWithTime<Object>> returnedValueOrException = new ArrayList<>();

    public void onReturnValue(Time startTime, Time endTime, ExecutionContext ctx, Object value) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.put(ctx, new TimePair(startTime, endTime));
        if (value != null) {
            returnedValueOrException.add(new ItemWithTime<>(endTime, value));
        }
    }

    public void onReturnExceptional(Time startTime, Time endTime, ExecutionContext ctx, RuntimeException throwable) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.put(ctx, new TimePair(startTime, endTime));
        returnedValueOrException.add(new ItemWithTime<>(endTime, throwable));
    }

    public void onReadArgument(Time time, CallContextElement.FunctionCallArray ctx, int argIndex) {
        readMap.add(new ItemWithTime<>(time, new ReadArgument(ctx, argIndex)));
    }

    public void onReadLocalVariable(Time time, CallContextElement[] ctx, Object variableName) {
        readMap.add(new ItemWithTime<>(time, new ReadLocalVariable(ctx, variableName)));
    }

    public void onReadObjectField(Time time, ExecutionContext objGenCtx, Object field) {
        readMap.add(new ItemWithTime<>(time, new ReadObjectField(objGenCtx, field)));
    }

    public void onUpdateObjectWithHash(Time time, ExecutionContext objGenCtx, String fieldName, Object newValue) {
        onUpdateObjectInner(time, objGenCtx, fieldName, newValue);
    }

    public void onUpdateLocalVariable(Time time, CallContextElement.FunctionCallArray ctx, String variableName, Object newValue) {
        localVariableUpdateMap.computeIfAbsent(ctx, it -> new HashMap<>())
                .computeIfAbsent(variableName, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, newValue));
        localVariableUpdateList.computeIfAbsent(ctx, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, new LocalVariableUpdate(variableName, newValue)));
    }

    public void onEnterFunction(Time time, String funcName) {
        functionCalls.add(new ItemWithTime<>(time, funcName));
    }

    public Object getReturnedValueOrThrow(Time time) {
        Object result = ItemWithTime.binarySearchJust(returnedValueOrException, time);

        if (result instanceof RuntimeException) throw (RuntimeException) result;

        return result;
    }

    private void onUpdateObjectInner(Time time, ExecutionContext objGenCtx, String fieldName, Object newValue) {
        objectUpdateMap.computeIfAbsent(objGenCtx, it -> new HashMap<>())
                .computeIfAbsent(fieldName, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, newValue));
        objectUpdateList.add(new ItemWithTime<>(time, new ExecutionHistoryOperator.ObjectUpdate(objGenCtx, fieldName, newValue)));
    }

    public Iterator<ItemWithTime<ReadContent>> getReadOperations(Time startTime, Time endTime) {
        return ItemsWithTimeIterator.create(readMap, startTime, endTime);
    }

    public Iterator<ItemWithTime<ReadContent>> getReadOperations(ExecutionContext ctx) {
        TimePair timePair = contextToTime.get(ctx);
        if (timePair == null) return null;
        return getReadOperations(timePair.start, timePair.end);
    }

    public Iterator<ItemWithTime<ExecutionHistoryOperator.ObjectUpdate>> getObjectUpdates(Time startTime, Time endTime) {
        return ItemsWithTimeIterator.create(objectUpdateList, startTime, endTime);
    }

    public Iterator<ItemWithTime<ExecutionHistoryOperator.ObjectUpdate>> getObjectUpdates(ExecutionContext ctx) {
        TimePair timePair = contextToTime.get(ctx);
        if (timePair == null) return null;
        return getObjectUpdates(timePair.start, timePair.end);
    }

    public Iterator<ItemWithTime<LocalVariableUpdate>> getLocalVariableUpdates(CallContextElement.FunctionCallArray callCtx, ExecutionContext execCtx) {
        TimePair timePair = contextToTime.get(execCtx);
        if (timePair == null) return noElementIterator();
        ArrayList<ItemWithTime<LocalVariableUpdate>> varHistory = localVariableUpdateList.get(callCtx);
        if (varHistory == null) return noElementIterator();
        return ItemsWithTimeIterator.create(varHistory, timePair.start, timePair.end);
    }

    public Iterator<ItemWithTime<String>> getFunctionEnters(Time startTime, Time endTime) {
        return ItemsWithTimeIterator.create(functionCalls, startTime, endTime);
    }

    public Iterator<ItemWithTime<String>> getFunctionEnters(ExecutionContext ctx) {
        TimePair timePair = contextToTime.get(ctx);
        if (timePair == null) return null;
        return getFunctionEnters(timePair.start, timePair.end);
    }

    public HashMap<String, ArrayList<ItemWithTime<Object>>> getLocalHistory(CallContextElement.FunctionCallArray fca) {
        return localVariableUpdateMap.get(fca);
    }

    public HashMap<String, ArrayList<ItemWithTime<Object>>> getObjectHistory(ExecutionContext objGenCtx) {
        return objectUpdateMap.get(objGenCtx);
    }

    public TimePair getTime(ExecutionContext ctx) {
        return contextToTime.get(ctx);
    }

    public void deleteRecords(ExecutionContext context) {
        final TimePair tp = contextToTime.get(context);
        Time endTime = tp.end;
        final int i = ItemWithTime.binarySearch(timeToContext, endTime);
        endTime = timeToContext.get(i + 1).getTime();
        deleteRecords(tp.start, endTime);
    }

    /**
     * @param startTime inclusive
     * @param endTime exclusive
     */
    public void deleteRecords(Time startTime, Time endTime) {
        ItemWithTime.subList(readMap, startTime, endTime).clear();
        List<ItemWithTime<ExecutionHistoryOperator.ObjectUpdate>> updates = ItemWithTime.subList(objectUpdateList, startTime, endTime);
        HashMap<ExecutionContext, HashSet<String>> updatedFields = new HashMap<>();
        for (ItemWithTime<ExecutionHistoryOperator.ObjectUpdate> update : updates) {
            ExecutionHistoryOperator.ObjectUpdate item = update.getItem();
            updatedFields.computeIfAbsent(item.getObjectGenCtx(), it -> new HashSet<>())
                    .add(item.getFieldName());
        }

        for (Map.Entry<ExecutionContext, HashSet<String>> entry : updatedFields.entrySet()) {
            ExecutionContext objectId = entry.getKey();
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

    public void merge(ExecutionHistory other) {
        if (other.timeToContext.size() == 0) return;

        final Time initialTime = other.timeToContext.get(0).getTime();
        final int timeToCtxPoint = ItemWithTime.binarySearchWhereInsertTo(timeToContext, initialTime);
        timeToContext.addAll(timeToCtxPoint, other.timeToContext);
        contextToTime.putAll(other.contextToTime);

        if (other.readMap.size() != 0) {
            final int readMapPoint = ItemWithTime.binarySearchWhereInsertTo(readMap, initialTime);
            readMap.addAll(readMapPoint, other.readMap);
        }

        for (Map.Entry<ExecutionContext, HashMap<String, ArrayList<ItemWithTime<Object>>>> entry : other.objectUpdateMap.entrySet()) {
            ExecutionContext objGenCtx = entry.getKey();
            HashMap<String, ArrayList<ItemWithTime<Object>>> objHistory = entry.getValue();

            objectUpdateMap.merge(objGenCtx, objHistory, (thisObjHistory, thatObjHistory) -> {
                for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> e : objHistory.entrySet()) {
                    String fldName = e.getKey();
                    ArrayList<ItemWithTime<Object>> fldHistory = e.getValue();

                    thisObjHistory.merge(fldName, fldHistory, (thisFldHistory, thatFldHistory) -> {
                        final int fhPoint = ItemWithTime.binarySearchWhereInsertTo(thisFldHistory, initialTime);
                        thisFldHistory.addAll(fhPoint, thatFldHistory);
                        return thisFldHistory;
                    });
                }
                return thisObjHistory;
            });
        }

        final int objUpPoint = ItemWithTime.binarySearchWhereInsertTo(objectUpdateList, initialTime);
        objectUpdateList.addAll(objUpPoint, other.objectUpdateList);

        for (Map.Entry<CallContextElement.FunctionCallArray, HashMap<String, ArrayList<ItemWithTime<Object>>>> e : localVariableUpdateMap.entrySet()) {
            CallContextElement.FunctionCallArray fcs = e.getKey();
            HashMap<String, ArrayList<ItemWithTime<Object>>> upMap = e.getValue();

            localVariableUpdateMap.merge(fcs, upMap, (thisUpMap, thatUpMap) -> {
                for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> entry : thatUpMap.entrySet()) {
                    String varName = entry.getKey();
                    ArrayList<ItemWithTime<Object>> varHistory = entry.getValue();

                    thisUpMap.merge(varName, varHistory, (thisVarHistory, thatVarHistory) -> {
                        final int vhPoint = ItemWithTime.binarySearchWhereInsertTo(thisVarHistory, initialTime);
                        thisVarHistory.addAll(vhPoint, thatVarHistory);
                        return thisVarHistory;
                    });
                }
                return thisUpMap;
            });
        }

        for (Map.Entry<CallContextElement.FunctionCallArray, ArrayList<ItemWithTime<LocalVariableUpdate>>> entry : localVariableUpdateList.entrySet()) {
            CallContextElement.FunctionCallArray fcs = entry.getKey();
            ArrayList<ItemWithTime<LocalVariableUpdate>> upList = entry.getValue();

            localVariableUpdateList.merge(fcs, upList, (thisUpList, thatUpList) -> {
                final int hPoint = ItemWithTime.binarySearchWhereInsertTo(thisUpList, initialTime);
                thisUpList.addAll(hPoint, thatUpList);
                return thisUpList;
            });
        }

        final int revPoint = ItemWithTime.binarySearchWhereInsertTo(returnedValueOrException, initialTime);
        returnedValueOrException.addAll(revPoint, other.returnedValueOrException);
    }

    public String toStringSize() {
        return "ExecutionHistory{" +
                "timeToContext=" + timeToContext.size() +
                ", contextToTime=" + contextToTime.size() +
                ", readMap=" + readMap.size() +
                ", objectUpdateMap=" + objectUpdateMap.size() +
                ", objectUpdateList=" + objectUpdateList.size() +
                ", localVariableUpdateMap=" + localVariableUpdateMap.size() +
                ", localVariableUpdateList=" + localVariableUpdateList.size() +
                ", functionCalls=" + functionCalls.size() +
                ", returnedValueOrException=" + returnedValueOrException.size() +
                '}';
    }

    @Override
    public String toString() {
        return "ExecutionHistory{" +
                "timeToContext=" + timeToContext +
                ", contextToTime=" + contextToTime +
                ", readMap=" + readMap +
                ", objectUpdateMap=" + objectUpdateMap +
                ", objectUpdateList=" + objectUpdateList +
                ", localVariableUpdateMap=" + localVariableUpdateMap +
                ", localVariableUpdateList=" + localVariableUpdateList +
                ", functionCalls=" + functionCalls +
                ", returnedValueOrException=" + returnedValueOrException +
                '}';
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
        private final CallContextElement.FunctionCallArray callContext;
        private final int argIndex;

        public ReadArgument(CallContextElement.FunctionCallArray callContext, int argIndex) {
            this.callContext = callContext;
            this.argIndex = argIndex;
        }

        public CallContextElement.FunctionCallArray getCallContext() {
            return callContext;
        }

        public int getArgIndex() {
            return argIndex;
        }

        @Override
        public String toString() {
            return "ReadArgument{" +
                    "callContext=" + callContext +
                    ", argIndex=" + argIndex +
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
        private final ExecutionContext objGenCtx;
        private final Object fieldName;

        public ReadObjectField(ExecutionContext objGenCtx, Object fieldName) {
            this.objGenCtx = objGenCtx;
            this.fieldName = fieldName;
        }

        public ExecutionContext getObjGenCtx() {
            return objGenCtx;
        }

        public Object getFieldName() {
            return fieldName;
        }

        @Override
        public String toString() {
            return "ReadObjectField{" +
                    "objGenCtx=" + objGenCtx +
                    ", fieldName=" + fieldName +
                    '}';
        }
    }

    public final static class LocalVariableUpdate {
        private final String varName;
        private final Object object;

        public LocalVariableUpdate(String varName, Object object) {
            this.varName = varName;
            this.object = object;
        }

        public String getVarName() {
            return varName;
        }

        public Object getObject() {
            return object;
        }
    }

    public static class ObjectReference {
        private final ExecutionContext objGenCtx;

        public ObjectReference(ExecutionContext objGenCtx) {
            this.objGenCtx = objGenCtx;
        }

        public ExecutionContext getObjGenCtx() {
            return objGenCtx;
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
