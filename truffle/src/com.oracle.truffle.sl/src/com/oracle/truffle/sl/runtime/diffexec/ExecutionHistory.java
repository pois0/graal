package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.collections.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.oracle.truffle.sl.Util.assertNonNull;

public final class ExecutionHistory<TIME extends Time<TIME>> {
    private final TIME zero;
    private final ArrayList<ItemWithTime<TIME, ExecutionContext>> timeToContext = new ArrayList<>(100_000);
    private final HashMap<NodeIdentifier, HashMap<CallContext, TimeInfo<TIME>>> contextToTime = new HashMap<>();
    private final ArrayList<ItemWithTime<TIME, ReadObjectField<TIME>>> objectReadList = new ArrayList<>(100_000);
    private final HashMap<TIME, HashMap<String, ArrayList<ItemWithTime<TIME, Object>>>> objectUpdateMap = new HashMap<>();
    private final ArrayList<ItemWithTime<TIME, ObjectUpdate<TIME>>> objectUpdateList = new ArrayList<>(100_000);
    private final HashMap<CallContext.ContextBase, LocalVarOperator<TIME>> localVarInfo = new HashMap<>(10_000);
    private final ArrayList<ItemWithTime<TIME, Pair<CallContext.ContextBase, TruffleString>>> functionCalls = new ArrayList<>(2_500);

    public ExecutionHistory(TIME zero) {
        this.zero = zero;
    }

    public void onReturnValue(TIME startTime, TIME endTime, ExecutionContext ctx, Object value) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.computeIfAbsent(ctx.getCurrentNodeIdentifier(), it -> new HashMap<>())
                .put(ctx.getCallContext(), new TimeInfo<>(startTime, endTime, value));
    }

    public void onReturnExceptional(TIME startTime, TIME endTime, ExecutionContext ctx, RuntimeException e) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.computeIfAbsent(ctx.getCurrentNodeIdentifier(), it -> new HashMap<>())
                .put(ctx.getCallContext(), new TimeInfo<>(startTime, endTime, e));
    }

    public void replaceReturnedValueOrException(ExecutionContext ctx, Object value) {
        final TimeInfo<TIME> info = getTimeNN(ctx);
        info.setValue(value);
    }

    public void rewriteLocalVariable(ExecutionContext ctx, int slot, Object value) {
        final TIME time = getTimeNN(ctx).getEnd();
        final LocalVarOperator<TIME> op = localVarInfo.get(ctx.getCallContext().getBase());
        final LocalVariableUpdate prevUpdate = op.writeVarList
                .set(ItemWithTime.binarySearchJustIndex(op.writeVarList, time), new ItemWithTime<>(time, new LocalVariableUpdate(slot, value)))
                .item();
        ArrayList<ItemWithTime<TIME, Object>> varList = op.writeVarMap.get(prevUpdate.varName());
        varList.set(ItemWithTime.binarySearchJustIndex(varList, time), new ItemWithTime<>(time, value));
    }

    public ObjectUpdate<TIME> rewriteObjectField(ExecutionContext ctx, TIME objGenTime, String fieldName, Object value, boolean fieldChanged) {
        final TIME time = getTimeNN(ctx).getEnd();
        final ItemWithTime<TIME, ObjectUpdate<TIME>> prev = objectUpdateList
                .set(ItemWithTime.binarySearchJustIndex(objectUpdateList, time), new ItemWithTime<>(time, new ObjectUpdate<>(objGenTime, fieldName, value)));
        final ArrayList<ItemWithTime<TIME, Object>> fieldList = assertNonNull(
                objectUpdateMap.computeIfAbsent(objGenTime, it -> new HashMap<>()).computeIfAbsent(fieldName, it -> new ArrayList<>()));

        if (fieldChanged) {
            final ObjectUpdate<TIME> prevItem = prev.item();
            HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> objectHistory = objectUpdateMap.get(prevItem.objectGenCtx());
            final ArrayList<ItemWithTime<TIME, Object>> prevHistory = objectHistory.get(prevItem.fieldName());
            prevHistory.remove(ItemWithTime.binarySearchJustIndex(prevHistory, time));
            fieldList.add(ItemWithTime.binarySearchWhereInsertTo(fieldList, time), new ItemWithTime<>(time, value));
            return prevItem;
        } else {
            fieldList.set(ItemWithTime.binarySearchJustIndex(fieldList, time), new ItemWithTime<>(time, value));
            return null;
        }
    }

    public void onCreateObject(TIME time) {
        objectUpdateMap.putIfAbsent(time, new HashMap<>());
    }

    public void onReadObjectField(TIME time, TIME objGenTime, Object field) {
        objectReadList.add(new ItemWithTime<>(time, new ReadObjectField<>(objGenTime, field)));
    }

    public void onUpdateObjectWithHash(TIME time, TIME objGenTime, String fieldName, Object newValue) {
        objectUpdateMap.computeIfAbsent(objGenTime, it -> new HashMap<>())
                .computeIfAbsent(fieldName, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, newValue));
        objectUpdateList.add(new ItemWithTime<>(time, new ObjectUpdate<>(objGenTime, fieldName, newValue)));
    }

    public void onEnterFunction(TIME time, TruffleString funcName, CallContext.ContextBase ctx) {
        functionCalls.add(new ItemWithTime<>(time, Pair.create(ctx, funcName)));
    }

    public Object getReturnedValueOrThrow(ExecutionContext ctx) {
        final Object result = getTimeNN(ctx).getValue();
        if (result instanceof RuntimeException) throw (RuntimeException) result;
        return result;
    }

    public TIME getNextTime(TIME time) {
        final int i = ItemWithTime.binarySearchJustIndex(timeToContext, time) + 1;
        if (i == timeToContext.size()) return time.incAndSimplify();
        return time.mid(timeToContext.get(i).time());
    }

    public List<ItemWithTime<TIME, ReadObjectField<TIME>>> getReadOperations(TIME startTime, TIME endTime) {
        return ItemWithTime.subList(objectReadList, startTime, endTime);
    }

    public List<ItemWithTime<TIME, Pair<CallContext.ContextBase, TruffleString>>> getFunctionEnters(TIME startTime, TIME endTime) {
        return ItemWithTime.subList(functionCalls, startTime, endTime);
    }

    public LocalVarOperator<TIME> getLocalVarOperator(CallContext.ContextBase ctx, int paramLen) {
        return localVarInfo.computeIfAbsent(ctx, it -> new LocalVarOperator<>(paramLen));
    }

    public HashMap<Integer, ArrayList<ItemWithTime<TIME, Object>>> getLocalHistory(CallContext.ContextBase fca) {
        final LocalVarOperator<TIME> op = localVarInfo.get(fca);
        if (op == null) return null;
        return op.writeVarMap;
    }

    public HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> getObjectHistory(TIME objGenTime) {
        return objectUpdateMap.get(objGenTime);
    }

    public TIME getInitialTime() {
        return timeToContext.isEmpty() ? zero : timeToContext.get(0).time();
    }

    public void deleteRecords(ExecutionContext ctx) {
        final TimeInfo<TIME> tp = getTime(ctx);
        if (tp == null) return;
        deleteRecords(tp.getStart(), tp.getEnd());
    }

    public void deleteRecords(ExecutionContext exclusiveStart, ExecutionContext exclusiveEnd) {
        final TIME startNext = getTimeNN(exclusiveStart).getEnd();
        final int startI = ItemWithTime.binarySearchNext(timeToContext, startNext);
        final TIME endPrev = getTimeNN(exclusiveEnd).getEnd();
        final int endI = ItemWithTime.binarySearchPrev(timeToContext, endPrev);

        if (startI >= endI) return;
        final TIME startTime = timeToContext.get(startI).time();
        final TIME endTime = timeToContext.get(endI).time();
        deleteRecords(startTime, endTime);
    }

    /**
     * @param startTime inclusive
     * @param endTime inclusive
     */
    public void deleteRecords(TIME startTime, TIME endTime) {
        // delete from timeToContext and contextToTime
        final List<ItemWithTime<TIME, ExecutionContext>> contexts = ItemWithTime.subList(timeToContext, startTime, endTime);
        for (ItemWithTime<TIME, ExecutionContext> e : contexts) {
            final ExecutionContext ctx = e.item();
            contextToTime.get(ctx.getCurrentNodeIdentifier()).remove(ctx.getCallContext());
        }
        contexts.clear();

        // delete from objectReadList
        ItemWithTime.subList(objectReadList, startTime, endTime).clear();

        // delete from objectUpdateList and objectUpdateMap
        final HashMap<TIME, HashSet<String>> updatedFields = new HashMap<>();
        final List<ItemWithTime<TIME, ObjectUpdate<TIME>>> objectUpdateList = ItemWithTime.subList(this.objectUpdateList, startTime, endTime);
        for (ItemWithTime<TIME, ObjectUpdate<TIME>> update : objectUpdateList) {
            final ObjectUpdate<TIME> item = update.item();
            updatedFields.computeIfAbsent(item.objectGenCtx(), it -> new HashSet<>())
                    .add(item.fieldName());
        }
        for (Map.Entry<TIME, HashSet<String>> e : updatedFields.entrySet()) {
            final TIME objGenTime = e.getKey();
            final HashSet<String> fields = e.getValue();
            final HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> map = objectUpdateMap.get(objGenTime);
            for (String field : fields) {
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
        final List<ItemWithTime<TIME, Pair<CallContext.ContextBase, TruffleString>>> functionCalls = this.functionCalls.subList(start, end);
        for (ItemWithTime<TIME, Pair<CallContext.ContextBase, TruffleString>> entry : functionCalls) {
            deleteFromLocalVarOperator(entry, startTime, endTime);
        }

        // delete functionCalls
        functionCalls.clear();
    }

    private void deleteFromLocalVarOperator(ItemWithTime<TIME, Pair<CallContext.ContextBase, TruffleString>> entry, TIME startTime, TIME endTime) {
        final LocalVarOperator<TIME> op = localVarInfo.get(entry.item().getLeft());
        if (op == null) return;
        final List<ItemWithTime<TIME, LocalVariableUpdate>> writeVarList = ItemWithTime.subList(op.writeVarList, startTime, endTime);
        final HashSet<Integer> vars = new HashSet<>(); // TODO use BitSet
        for (ItemWithTime<TIME, LocalVariableUpdate> e : writeVarList) {
            final LocalVariableUpdate item = e.item();
            vars.add(item.varName());
        }
        writeVarList.clear();
        for (Integer slot : vars) {
            ItemWithTime.subList(op.writeVarMap.get(slot), startTime, endTime).clear();
        }
        for (Map.Entry<Integer, ArrayList<TIME>> e : op.readVariable.entrySet()) {
            Time.subList(e.getValue(), startTime, endTime).clear();
        }
        for (ArrayList<TIME> times : op.readParam) {
            Time.subList(times, startTime, endTime).clear();
        }
    }

    public ExecutionHistory<TIME> merge(ExecutionHistory<TIME> other) {
        if (other.timeToContext.isEmpty()) return this;
        final TIME initialTime = other.timeToContext.get(0).time();
//        final TIME endTime = other.timeToContext.get(other.timeToContext.size() - 1).time();

        // merge timeToContext
        ItemWithTime.merge(timeToContext, other.timeToContext, initialTime);

        // merge contextToTime
        for (Map.Entry<NodeIdentifier, HashMap<CallContext, TimeInfo<TIME>>> e : other.contextToTime.entrySet()) {
            contextToTime.merge(e.getKey(), e.getValue(), (base, v) -> {
                base.putAll(v);
                return base;
            });
        }

        // merge objectReadList
        ItemWithTime.merge(objectReadList, other.objectReadList, initialTime);

        // merge objectUpdateMap
        for (Map.Entry<TIME, HashMap<String, ArrayList<ItemWithTime<TIME, Object>>>> entry : other.objectUpdateMap.entrySet()) {
            objectUpdateMap.merge(entry.getKey(), entry.getValue(), (base, otherValue) -> {
                for (Map.Entry<String, ArrayList<ItemWithTime<TIME, Object>>> fieldEntry : otherValue.entrySet()) {
                    base.merge(fieldEntry.getKey(), fieldEntry.getValue(), (baseEntry, otherEntry) -> ItemWithTime.merge(baseEntry, otherEntry, initialTime));
                }
                return base;
            });
        }

        // merge objectUpdateList
        ItemWithTime.merge(objectUpdateList, other.objectUpdateList, initialTime);

        // merge localVarInfo
        for (Map.Entry<CallContext.ContextBase, LocalVarOperator<TIME>> e : other.localVarInfo.entrySet()) {
            localVarInfo.merge(e.getKey(), e.getValue(), (final LocalVarOperator<TIME> base, final LocalVarOperator<TIME> newValue) -> {
                ItemWithTime.merge(base.writeVarList, newValue.writeVarList, initialTime);

                for (Map.Entry<Integer, ArrayList<ItemWithTime<TIME, Object>>> e2 : newValue.writeVarMap.entrySet()) {
                    base.writeVarMap.merge(
                            e2.getKey(),
                            e2.getValue(),
                            (base2, newValue2) -> ItemWithTime.merge(base2, newValue2, initialTime)
                    );
                }

                // readParam
                for (int i = 0; i < newValue.readParam.length; i++) {
                    final ArrayList<TIME> newTimes = newValue.readParam[i];
                    if (newTimes.isEmpty()) continue;
                    final ArrayList<TIME> thisTimes = base.readParam[i];
                    final int j = Time.binarySearchWhereInsertTo(thisTimes, initialTime);
                    thisTimes.addAll(j, newTimes);
                }

                // readVariable
                for (Map.Entry<Integer, ArrayList<TIME>> e2 : newValue.readVariable.entrySet()) {
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


    public TimeInfo<TIME> getTime(ExecutionContext ctx) {
        final HashMap<CallContext, TimeInfo<TIME>> map = contextToTime.get(ctx.getCurrentNodeIdentifier());
        if (map == null) return null;
        return map.get(ctx.getCallContext());
    }

    TimeInfo<TIME> getTimeNN(ExecutionContext ctx) {
        return assertNonNull(getTime(ctx));
    }

    public final static class TimeInfo<TIME extends Time<TIME>> {
        private final TIME start;
        private final TIME end;
        private Object value;

        public TimeInfo(TIME start, TIME end, Object value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        /**
         * inclusive
         */
        public TIME getStart() {
            return start;
        }

        /**
         * inclusive
         */
        public TIME getEnd() {
            return end;
        }

        public Object getValue() {
            return value;
        }

        private void setValue(Object value) {
            this.value = value;
        }
    }

    public record ReadObjectField<TIME extends Time<TIME>>(TIME objGenCtx, Object fieldName) {}

    public record LocalVariableUpdate(int varName, Object object) {}

    public record ObjectReference<TIME extends Time<TIME>>(TIME objGenCtx) {
        public ObjectReference {
            if (objGenCtx == null) throw new NullPointerException();
        }
    }

    public final static class LocalVarOperator<TIME extends Time<TIME>> {
        private final HashMap<Integer, ArrayList<ItemWithTime<TIME, Object>>> writeVarMap = new HashMap<>();
        private final ArrayList<ItemWithTime<TIME, LocalVariableUpdate>> writeVarList = new ArrayList<>();
        private final ArrayList<TIME>[] readParam;
        private final HashMap<Integer, ArrayList<TIME>> readVariable = new HashMap<>();

        public LocalVarOperator(int paramLen) {
            @SuppressWarnings("unchecked")
            final ArrayList<TIME>[] readParam = this.readParam = new ArrayList[paramLen];
            for (int i = 0; i < readParam.length; i++) {
                readParam[i] = new ArrayList<>();
            }
        }

        public void onUpdateVariable(TIME time, int slot, Object newValue) {
            writeVarMap.computeIfAbsent(slot, it -> new ArrayList<>())
                    .add(new ItemWithTime<>(time, newValue));
            writeVarList.add(new ItemWithTime<>(time, new LocalVariableUpdate(slot, newValue)));
        }

        public void onReadParam(TIME time, int slotNum) {
            readParam[slotNum].add(time);
        }

        public void onReadVariable(TIME time, int slot) {
            readVariable.computeIfAbsent(slot, it -> new ArrayList<>())
                    .add(time);
        }

        public ArrayList<TIME> getReadParam(int paramIndex) {
            return readParam[paramIndex];
        }

        public ArrayList<TIME> getReadVar(int slot) {
            return readVariable.get(slot);
        }
    }

    public record ObjectUpdate<TIME extends Time<TIME>>(TIME objectGenCtx, String fieldName, Object newValue) {}
}
