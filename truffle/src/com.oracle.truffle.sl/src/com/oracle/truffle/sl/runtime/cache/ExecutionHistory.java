package com.oracle.truffle.sl.runtime.cache;

import org.graalvm.collections.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class ExecutionHistory {
    private final ArrayList<ItemWithTime<ExecutionContext>> timeToContext = new ArrayList<>(100_000);
    private final HashMap<NodeIdentifier, HashMap<CallContext, TimeInfo>> contextToTime = new HashMap<>();
    private final ArrayList<ItemWithTime<ReadObjectField>> objectReadList = new ArrayList<>(100_000);
    private final HashMap<Time, HashMap<String, ArrayList<ItemWithTime<Object>>>> objectUpdateMap = new HashMap<>();
    private final ArrayList<ItemWithTime<ObjectUpdate>> objectUpdateList = new ArrayList<>(100_000);
    private final HashMap<CallContext.ContextBase, LocalVarOperator> localVarInfo = new HashMap<>(10_000);
    private final ArrayList<ItemWithTime<Pair<CallContext.ContextBase, String>>> functionCalls = new ArrayList<>(2_500);

    public void onReturnValue(Time startTime, Time endTime, ExecutionContext ctx, Object value) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.computeIfAbsent(ctx.getCurrentNodeIdentifier(), it -> new HashMap<>())
                .put(ctx.getCallContext(), new TimeInfo(startTime, endTime, value));
    }

    public void onReturnExceptional(Time startTime, Time endTime, ExecutionContext ctx, RuntimeException throwable) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.computeIfAbsent(ctx.getCurrentNodeIdentifier(), it -> new HashMap<>())
                .put(ctx.getCallContext(), new TimeInfo(startTime, endTime, throwable));
    }

    public void replaceReturnedValueOrException(ExecutionContext ctx, Object value) {
        final TimeInfo info = getTime(ctx);
        assert info != null;
        info.setValue(value);
    }

    public void rewriteLocalVariable(ExecutionContext ctx, String varName, Object value) {
        final Time time = getTime(ctx).getEnd();
        final LocalVarOperator op = localVarInfo.get(ctx.getCallContext().getBase());
        final int i = ItemWithTime.binarySearchApproximately(op.writeVarList, time);
        assert i >= 0;
        final LocalVariableUpdate prevUpdate = op.writeVarList.get(i).getItem();
        op.writeVarList.set(i, new ItemWithTime<>(time, new LocalVariableUpdate(varName, value)));
        final ArrayList<ItemWithTime<Object>> varList = op.writeVarMap.get(prevUpdate.varName);
        final int i1 = ItemWithTime.binarySearchApproximately(varList, time);
        assert i1 >= 0;
        varList.set(i1, new ItemWithTime<>(time, value));
    }

    public ObjectUpdate rewriteObjectField(ExecutionContext ctx, Time objGenTime, String fieldName, Object value, boolean fieldChanged) {
        final Time time = getTime(ctx).getEnd();
        final int i = ItemWithTime.binarySearchApproximately(objectUpdateList, time);
        assert i >= 0;

        final ItemWithTime<ObjectUpdate> prev = objectUpdateList.set(i, new ItemWithTime<>(time, new ObjectUpdate(objGenTime, fieldName, value)));
        final ArrayList<ItemWithTime<Object>> fieldList = objectUpdateMap.get(objGenTime).get(fieldName);
        assert fieldList != null;

        if (fieldChanged) {
            final ObjectUpdate prevItem = prev.getItem();
            final ArrayList<ItemWithTime<Object>> prevHistory = objectUpdateMap.get(prevItem.objectGenCtx)
                    .get(prevItem.fieldName);
            final int removeIndex = ItemWithTime.binarySearchJustIndex(prevHistory, time);
            prevHistory.remove(removeIndex);
            final int i1 = ItemWithTime.binarySearchWhereInsertTo(fieldList, time);
            assert i1 >= 0;
            fieldList.add(i1, new ItemWithTime<>(time, value));
            return prevItem;
        } else {
            final int i1 = ItemWithTime.binarySearchApproximately(fieldList, time);
            assert i1 >= 0;
            fieldList.set(i1, new ItemWithTime<>(time, value));
            return null;
        }
    }

    public void onCreateObject(Time time) {
        objectUpdateMap.put(time, new HashMap<>());
    }

    public void onReadObjectField(Time time, Time objGenTime, Object field) {
        objectReadList.add(new ItemWithTime<>(time, new ReadObjectField(objGenTime, field)));
    }

    public void onUpdateObjectWithHash(Time time, Time objGenTime, String fieldName, Object newValue) {
        objectUpdateMap.computeIfAbsent(objGenTime, it -> new HashMap<>())
                .computeIfAbsent(fieldName, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, newValue));
        objectUpdateList.add(new ItemWithTime<>(time, new ObjectUpdate(objGenTime, fieldName, newValue)));
    }

    public void onEnterFunction(Time time, String funcName, CallContext.ContextBase ctx) {
        functionCalls.add(new ItemWithTime<>(time, Pair.create(ctx, funcName)));
    }

    public Object getReturnedValueOrThrow(ExecutionContext ctx) {
        Object result = getTime(ctx).value;

        if (result instanceof RuntimeException) throw (RuntimeException) result;

        return result;
    }

    public Time getNextTime(Time time) {
        final int i = ItemWithTime.binarySearchApproximately(timeToContext, time) + 1;
        assert i > 0;
        if (i == timeToContext.size()) return time.inc();
        return time.mid(timeToContext.get(i).getTime());
    }

    public List<ItemWithTime<ReadObjectField>> getReadOperations(Time startTime, Time endTime) {
        return ItemWithTime.subList(objectReadList, startTime, endTime);
    }

    public List<ItemWithTime<Pair<CallContext.ContextBase, String>>> getFunctionEnters(Time startTime, Time endTime) {
        return ItemWithTime.subList(functionCalls, startTime, endTime);
    }

    public LocalVarOperator getLocalVarOperator(CallContext.ContextBase ctx, int paramLen) {
        return localVarInfo.computeIfAbsent(ctx, it -> new LocalVarOperator(paramLen));
    }

    public HashMap<String, ArrayList<ItemWithTime<Object>>> getLocalHistory(CallContext.ContextBase fca) {
        final LocalVarOperator op = localVarInfo.get(fca);
        if (op == null) return null;
        return op.writeVarMap;
    }

    public HashMap<String, ArrayList<ItemWithTime<Object>>> getObjectHistory(Time objGenTime) {
        return objectUpdateMap.get(objGenTime);
    }

    public Time getInitialTime() {
        return timeToContext.isEmpty() ? Time.ZERO : timeToContext.get(0).getTime();
    }

    public TimeInfo getTime(ExecutionContext ctx) {
        final HashMap<CallContext, TimeInfo> map = contextToTime.get(ctx.getCurrentNodeIdentifier());
        if (map == null) return null;
        return map.get(ctx.getCallContext());
    }

    public void deleteRecords(ExecutionContext context) {
//        System.out.println("delete records!: " + context);
        final TimeInfo tp = getTime(context);
        if (tp == null) return;
        deleteRecords(tp.start, tp.end);
    }

    public void deleteRecords(ExecutionContext exclusiveStart, ExecutionContext exclusiveEnd) {
        final Time startNext = getTime(exclusiveStart).getEnd();
        final int startI = ItemWithTime.binarySearchNext(timeToContext, startNext);
        final Time endPrev = getTime(exclusiveEnd).getEnd();
        final int endI = ItemWithTime.binarySearchPrev(timeToContext, endPrev);

        if (startI >= endI) return;
        final Time startTime = timeToContext.get(startI).getTime();
        final Time endTime = timeToContext.get(endI).getTime();
        deleteRecords(startTime, endTime);
    }

    /**
     * @param startTime inclusive
     * @param endTime inclusive
     */
    public void deleteRecords(Time startTime, Time endTime) {
//        System.out.println("deleteRecordsBase: " + startTime + " ~ " + endTime);
        List<ItemWithTime<ExecutionContext>> contexts = ItemWithTime.subList(timeToContext, startTime, endTime);
        for (ItemWithTime<ExecutionContext> e : contexts) {
            final ExecutionContext ctx = e.getItem();
            contextToTime.get(ctx.getCurrentNodeIdentifier()).remove(ctx.getCallContext());
        }
        contexts.clear();

        // delete objectReadList
        ItemWithTime.subList(objectReadList, startTime, endTime).clear();

        // delete objectUpdateList and objectUpdateMap
        HashMap<Time, HashSet<String>> updatedFields = new HashMap<>();
        List<ItemWithTime<ObjectUpdate>> objectUpdateList = ItemWithTime.subList(this.objectUpdateList, startTime, endTime);
        for (ItemWithTime<ObjectUpdate> update : objectUpdateList) {
            ObjectUpdate item = update.getItem();
            updatedFields.computeIfAbsent(item.getObjectGenCtx(), it -> new HashSet<>())
                    .add(item.getFieldName());
        }
        for (Map.Entry<Time, HashSet<String>> entry : updatedFields.entrySet()) {
            HashMap<String, ArrayList<ItemWithTime<Object>>> map = objectUpdateMap.get(entry.getKey());
            for (String field : entry.getValue()) {
                ItemWithTime.subList(map.get(field), startTime, endTime).clear();
            }
        }
        objectUpdateList.clear();

        // delete localVarInfo
        int start = ItemWithTime.binarySearchApproximately(functionCalls, startTime);
        final int end = ItemWithTime.binarySearchNext(functionCalls, endTime);
        if (start < 0) {
            start = -start - 1;
        } else {
            deleteFromLocalVarOperator(functionCalls.get(start), startTime, endTime);
        }
        final List<ItemWithTime<Pair<CallContext.ContextBase, String>>> functionCalls = this.functionCalls.subList(start, end);
        for (ItemWithTime<Pair<CallContext.ContextBase, String>> entry : functionCalls) {
            deleteFromLocalVarOperator(entry, startTime, endTime);
        }

        // delete functionCalls
        functionCalls.clear();
    }

    private void deleteFromLocalVarOperator(ItemWithTime<Pair<CallContext.ContextBase, String>> entry, Time startTime, Time endTime) {
        final LocalVarOperator op = localVarInfo.get(entry.getItem().getLeft());
        if (op == null) return;
        final List<ItemWithTime<LocalVariableUpdate>> writeVarList = ItemWithTime.subList(op.writeVarList, startTime, endTime);
        final HashSet<String> vars = new HashSet<>();
        for (ItemWithTime<LocalVariableUpdate> e : writeVarList) {
            final LocalVariableUpdate item = e.getItem();
            vars.add(item.getVarName());
        }
        writeVarList.clear();
        for (String varName : vars) {
            ItemWithTime.subList(op.writeVarMap.get(varName), startTime, endTime).clear();
        }
        for (Map.Entry<String, ArrayList<Time>> e : op.readVariable.entrySet()) {
            Time.subList(e.getValue(), startTime, endTime).clear();
        }
        for (ArrayList<Time> times : op.readParam) {
            Time.subList(times, startTime, endTime).clear();
        }
    }

    public ExecutionHistory merge(ExecutionHistory other) {
        if (other.timeToContext.size() == 0) return this;
        final Time initialTime = other.timeToContext.get(0).getTime();
        final Time endTime = other.timeToContext.get(other.timeToContext.size() - 1).getTime();

        // merge timeToContext
        ItemWithTime.merge(timeToContext, other.timeToContext, initialTime);

        // merge contextToTime
        for (Map.Entry<NodeIdentifier, HashMap<CallContext, TimeInfo>> e : other.contextToTime.entrySet()) {
            contextToTime.merge(e.getKey(), e.getValue(), (base, v) -> {
                base.putAll(v);
                return base;
            });
        }

        // merge objectReadList
        ItemWithTime.merge(objectReadList, other.objectReadList, initialTime);

        // merge objectUpdateMap
        for (Map.Entry<Time, HashMap<String, ArrayList<ItemWithTime<Object>>>> entry : other.objectUpdateMap.entrySet()) {
            objectUpdateMap.merge(entry.getKey(), entry.getValue(), (base, otherValue) -> {
                for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> fieldEntry : otherValue.entrySet()) {
                    base.merge(fieldEntry.getKey(), fieldEntry.getValue(), (baseEntry, otherEntry) -> ItemWithTime.merge(baseEntry, otherEntry, initialTime));
                }
                return base;
            });
        }

        // merge objectUpdateList
        ItemWithTime.merge(objectUpdateList, other.objectUpdateList, initialTime);

        // merge localVarInfo
        for (Map.Entry<CallContext.ContextBase, LocalVarOperator> e : other.localVarInfo.entrySet()) {
            localVarInfo.merge(e.getKey(), e.getValue(), (final LocalVarOperator base, final LocalVarOperator newValue) -> {
                ItemWithTime.merge(base.writeVarList, newValue.writeVarList, initialTime);

                for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> e2 : newValue.writeVarMap.entrySet()) {
                    base.writeVarMap.merge(
                            e2.getKey(),
                            e2.getValue(),
                            (base2, newValue2) -> ItemWithTime.merge(base2, newValue2, initialTime)
                    );
                }

                // readParam
                for (int i = 0; i < newValue.readParam.length; i++) {
                    final ArrayList<Time> newTimes = newValue.readParam[i];
                    if (newTimes.isEmpty()) continue;
                    final ArrayList<Time> thisTimes = base.readParam[i];
                    final int j = Time.binarySearchWhereInsertTo(thisTimes, initialTime);
                    thisTimes.addAll(j, newTimes);
                }

                // readVariable
                for (Map.Entry<String, ArrayList<Time>> e2 : newValue.readVariable.entrySet()) {
                    base.readVariable.merge(
                            e2.getKey(),
                            e2.getValue(),
                            (base2, newValue2) -> Time.merge(base2, newValue2, initialTime)
                    );
                }

                return base;
            });
        }

        // merge functionCalls
        ItemWithTime.merge(functionCalls, other.functionCalls, initialTime);

        return this;
    }

    @Override
    public String toString() {
        return "ExecutionHistory{" +
                "timeToContext=" + timeToContext +
                ", contextToTime=" + contextToTime +
                ", readMap=" + objectReadList +
                ", objectUpdateMap=" + objectUpdateMap +
                ", objectUpdateList=" + objectUpdateList +
                ", localVariableInfo=" + localVarInfo +
                ", functionCalls=" + functionCalls +
                '}';
    }

    public final static class TimeInfo {
        private final Time start;
        private final Time end;
        private Object value;

        public TimeInfo(Time start, Time end, Object value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        /**
         * inclusive
         */
        public Time getStart() {
            return start;
        }

        /**
         * inclusive
         */
        public Time getEnd() {
            return end;
        }

        public Object getValue() {
            return value;
        }

        private void setValue(Object value) {
            this.value = value;
        }
    }

    public final static class ReadObjectField {
        private final Time objGenCtx;
        private final Object fieldName;

        public ReadObjectField(Time objGenCtx, Object fieldName) {
            this.objGenCtx = objGenCtx;
            this.fieldName = fieldName;
        }

        public Time getObjGenCtx() {
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

    public final static class ObjectReference {
        private final Time objGenCtx;

        public ObjectReference(Time objGenCtx) {
            if (objGenCtx == null) throw new RuntimeException();
            this.objGenCtx = objGenCtx;
        }

        public Time getObjGenCtx() {
            return objGenCtx;
        }
    }

    public final static class LocalVarOperator {
        private final HashMap<String, ArrayList<ItemWithTime<Object>>> writeVarMap = new HashMap<>();
        private final ArrayList<ItemWithTime<LocalVariableUpdate>> writeVarList = new ArrayList<>();
        private final ArrayList<Time>[] readParam;
        private final HashMap<String, ArrayList<Time>> readVariable = new HashMap<>();

        public LocalVarOperator(int paramLen) {
            @SuppressWarnings("unchecked")
            final ArrayList<Time>[] readParam = this.readParam = new ArrayList[paramLen];
            for (int i = 0; i < readParam.length; i++) {
                readParam[i] = new ArrayList<>();
            }
        }

        public void onUpdateVariable(Time time, String variableName, Object newValue) {
            writeVarMap.computeIfAbsent(variableName, it -> new ArrayList<>())
                    .add(new ItemWithTime<>(time, newValue));
            writeVarList.add(new ItemWithTime<>(time, new LocalVariableUpdate(variableName, newValue)));
        }

        public void onReadParam(Time time, int slotNum) {
            readParam[slotNum].add(time);
        }

        public void onReadVariable(Time time, String variableName) {
            readVariable.computeIfAbsent(variableName, it -> new ArrayList<>())
                    .add(time);
        }

        public ArrayList<ItemWithTime<Object>> getWriteVar(String varName) {
            return writeVarMap.get(varName);
        }

        public ArrayList<Time> getReadParam(int paramIndex) {
            return readParam[paramIndex];
        }

        public ArrayList<Time> getReadVar(String varName) {
            return readVariable.get(varName);
        }
    }

    public final static class ObjectUpdate {
        private final Time objectGenCtx;
        private final String fieldName;
        private final Object newValue;

        public ObjectUpdate(Time objectGenCtx, String fieldName, Object newValue) {
            this.objectGenCtx = objectGenCtx;
            this.fieldName = fieldName;
            this.newValue = newValue;
        }

        public Time getObjectGenCtx() {
            return objectGenCtx;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Object getNewValue() {
            return newValue;
        }

        @Override
        public String toString() {
            return "ObjectUpdate{" +
                    "objectGenCtx=" + objectGenCtx +
                    ", fieldName='" + fieldName + '\'' +
                    ", newValue=" + newValue +
                    '}';
        }
    }
}
