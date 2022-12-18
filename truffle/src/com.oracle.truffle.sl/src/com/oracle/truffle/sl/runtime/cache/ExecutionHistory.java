package com.oracle.truffle.sl.runtime.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class ExecutionHistory {
    private final ArrayList<ItemWithTime<ExecutionContext>> timeToContext = new ArrayList<>(100_000);
    private final HashMap<ExecutionContext, TimeInfo> contextToTime = new HashMap<>(100_000);
    private final ArrayList<ItemWithTime<ReadObjectField>> objectReadList = new ArrayList<>(100_000);
    private final HashMap<ExecutionContext, HashMap<String, ArrayList<ItemWithTime<Object>>>> objectUpdateMap = new HashMap<>(100_000);
    private final ArrayList<ItemWithTime<ObjectUpdate>> objectUpdateList = new ArrayList<>(100_000);
    private final HashMap<CallContext.ContextBase, LocalVarOperator> localVarInfo = new HashMap<>(100_000);
    private final ArrayList<ItemWithTime<String>> functionCalls = new ArrayList<>(100_000);

    public void onReturnValue(Time startTime, Time endTime, ExecutionContext ctx, Object value) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.put(ctx, new TimeInfo(startTime, endTime, value));
    }

    public void onReturnExceptional(Time startTime, Time endTime, ExecutionContext ctx, RuntimeException throwable) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.put(ctx, new TimeInfo(startTime, endTime, throwable));
    }

    public void replaceReturnedValueOrException(ExecutionContext ctx, Object value) {
        final TimeInfo info = contextToTime.get(ctx);
        assert info != null;
        info.setValue(value);
    }

    public void rewriteLocalVariable(ExecutionContext ctx, String varName, Object value) {
        final Time time = contextToTime.get(ctx).getEnd();
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

    public void rewriteObjectField(ExecutionContext ctx, ExecutionContext objGenCtx, String fieldName, Object value) {
        final Time time = contextToTime.get(ctx).getEnd();
        final int i = ItemWithTime.binarySearchApproximately(objectUpdateList, time);
        assert i >= 0;
        objectUpdateList.set(i, new ItemWithTime<>(time, new ObjectUpdate(objGenCtx, fieldName, value)));
        final ArrayList<ItemWithTime<Object>> fieldList = objectUpdateMap.get(objGenCtx).get(fieldName);
        final int i1 = ItemWithTime.binarySearchApproximately(fieldList, time);
        assert i1 >= 0;
        fieldList.set(i1, new ItemWithTime<>(time, value));
    }

    public void onReadObjectField(Time time, ExecutionContext objGenCtx, Object field) {
        objectReadList.add(new ItemWithTime<>(time, new ReadObjectField(objGenCtx, field)));
    }

    public void onUpdateObjectWithHash(Time time, ExecutionContext objGenCtx, String fieldName, Object newValue) {
        onUpdateObjectInner(time, objGenCtx, fieldName, newValue);
    }

    public void onEnterFunction(Time time, String funcName) {
        functionCalls.add(new ItemWithTime<>(time, funcName));
    }

    public Object getReturnedValueOrThrow(ExecutionContext ctx) {
        Object result = contextToTime.get(ctx).value;

        if (result instanceof RuntimeException) throw (RuntimeException) result;

        return result;
    }

    public Time getNextTime(Time time) {
        final int i = ItemWithTime.binarySearchApproximately(timeToContext, time) + 1;
        assert i > 0;
        if (i == timeToContext.size()) return time.inc();
        return time.mid(timeToContext.get(i).getTime());
    }

    private void onUpdateObjectInner(Time time, ExecutionContext objGenCtx, String fieldName, Object newValue) {
        objectUpdateMap.computeIfAbsent(objGenCtx, it -> new HashMap<>())
                .computeIfAbsent(fieldName, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, newValue));
        objectUpdateList.add(new ItemWithTime<>(time, new ObjectUpdate(objGenCtx, fieldName, newValue)));
    }

    public List<ItemWithTime<ReadObjectField>> getReadOperations(Time startTime, Time endTime) {
        return ItemWithTime.subList(objectReadList, startTime, endTime);
    }

    public List<ItemWithTime<String>> getFunctionEnters(Time startTime, Time endTime) {
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

    public HashMap<String, ArrayList<ItemWithTime<Object>>> getObjectHistory(ExecutionContext objGenCtx) {
        return objectUpdateMap.get(objGenCtx);
    }

    public TimeInfo getTime(ExecutionContext ctx) {
        return contextToTime.get(ctx);
    }

    public void deleteRecords(ExecutionContext context) {
        final TimeInfo tp = contextToTime.get(context);
        deleteRecords(tp.start, tp.end);
    }

    /**
     * @param startTime inclusive
     * @param endTime inclusive
     */
    public void deleteRecords(Time startTime, Time endTime) {
        // delete timeToContext and contextToTime
        List<ItemWithTime<ExecutionContext>> contexts = ItemWithTime.subList(timeToContext, startTime, endTime);
        for (ItemWithTime<ExecutionContext> ctx : contexts) {
            contextToTime.remove(ctx.getItem());
        }
        contexts.clear();

        // delete objectReadList
        ItemWithTime.subList(objectReadList, startTime, endTime).clear();

        // delete objectUpdateList and objectUpdateMap
        HashMap<ExecutionContext, HashSet<String>> updatedFields = new HashMap<>();
        List<ItemWithTime<ObjectUpdate>> objectUpdateList = ItemWithTime.subList(this.objectUpdateList, startTime, endTime);
        for (ItemWithTime<ObjectUpdate> update : objectUpdateList) {
            ObjectUpdate item = update.getItem();
            updatedFields.computeIfAbsent(item.getObjectGenCtx(), it -> new HashSet<>())
                    .add(item.getFieldName());
        }
        for (Map.Entry<ExecutionContext, HashSet<String>> entry : updatedFields.entrySet()) {
            HashMap<String, ArrayList<ItemWithTime<Object>>> map = objectUpdateMap.get(entry.getKey());
            for (String field : entry.getValue()) {
                ItemWithTime.subList(map.get(field), startTime, endTime).clear();
            }
        }
        objectUpdateList.clear();

        // TODO: delete localVarInfo

        // delete functionCalls
        ItemWithTime.subList(functionCalls, startTime, endTime).clear();
    }

    public ExecutionHistory merge(ExecutionHistory other) {
        if (other.timeToContext.size() == 0) return this;
        final Time initialTime = other.timeToContext.get(0).getTime();

        // merge timeToContext
        ItemWithTime.merge(timeToContext, other.timeToContext, initialTime);

        // merge contextToTime
        contextToTime.putAll(other.contextToTime);

        // merge objectReadList
        ItemWithTime.merge(objectReadList, other.objectReadList, initialTime);

        // merge objectUpdateMap
        for (Map.Entry<ExecutionContext, HashMap<String, ArrayList<ItemWithTime<Object>>>> entry : other.objectUpdateMap.entrySet()) {
            ExecutionContext objGenCtx = entry.getKey();
            HashMap<String, ArrayList<ItemWithTime<Object>>> objHistory = entry.getValue();

            objectUpdateMap.merge(objGenCtx, objHistory, (thisObjHistory, thatObjHistory) -> {
                for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> e : objHistory.entrySet()) {
                    String fldName = e.getKey();
                    ArrayList<ItemWithTime<Object>> fldHistory = e.getValue();

                    thisObjHistory.merge(
                            fldName,
                            fldHistory,
                            (thisFldHistory, thatFldHistory) -> ItemWithTime.merge(thisFldHistory, thatFldHistory, initialTime)
                    );
                }
                return thisObjHistory;
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

    public String toStringSize() {
        return "ExecutionHistory{" +
                "timeToContext=" + timeToContext.size() +
                ", contextToTime=" + contextToTime.size() +
                ", readMap=" + objectReadList.size() +
                ", objectUpdateMap=" + objectUpdateMap.size() +
                ", objectUpdateList=" + objectUpdateList.size() +
                ", localVariableInfo=" + localVarInfo.size() +
                ", functionCalls=" + functionCalls.size() +
                '}';
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

    public static class TimeInfo {
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

    public final static class ObjectReference {
        private final ExecutionContext objGenCtx;

        public ObjectReference(ExecutionContext objGenCtx) {
            this.objGenCtx = objGenCtx;
        }

        public ExecutionContext getObjGenCtx() {
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

    private final static class ObjectUpdate {
        private final ExecutionContext objectGenCtx;
        private final String fieldName;
        private final Object newValue;

        public ObjectUpdate(ExecutionContext objectGenCtx, String fieldName, Object newValue) {
            this.objectGenCtx = objectGenCtx;
            this.fieldName = fieldName;
            this.newValue = newValue;
        }

        public ExecutionContext getObjectGenCtx() {
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
