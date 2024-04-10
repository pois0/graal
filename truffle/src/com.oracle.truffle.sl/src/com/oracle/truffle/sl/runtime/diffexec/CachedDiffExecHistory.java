package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.collections.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.oracle.truffle.sl.Util.assertNonNull;

public final class CachedDiffExecHistory<TIME extends Time<TIME>> {
    private final TIME zero;
    private final CachedCallContext executionBase;
    private final ArrayList<ItemWithTime<TIME, CachedExecutionContext>> timeToContext = new ArrayList<>(1_000);
    private final HashMap<NodeIdentifier, HashMap<CachedCallContext, TimeInfo<TIME>>> contextToTime;
    private final boolean sharedContextToTime;
    private final HashMap<TIME, HashMap<String, ArrayList<TIME>>> objectReadMap = new HashMap<>(1_000);
    private final ArrayList<ItemWithTime<TIME, ReadObjectField<TIME>>> objectReadList = new ArrayList<>(1_000);
    private final HashMap<TIME, HashMap<String, ArrayList<ItemWithTime<TIME, Object>>>> objectUpdateMap = new HashMap<>(1_000);
    private final ArrayList<ItemWithTime<TIME, ObjectUpdate<TIME>>> objectUpdateList = new ArrayList<>(1_000);
    private final HashMap<CachedCallContext, LocalVarOperator<TIME>> localVarInfo = new HashMap<>(1_000);
    private final ArrayList<ItemWithTime<TIME, Pair<CachedCallContext, TruffleString>>> functionCalls = new ArrayList<>(250);

    private CachedDiffExecHistory(TIME zero, CachedCallContext executionBase, HashMap<NodeIdentifier, HashMap<CachedCallContext, TimeInfo<TIME>>> contextToTime, boolean sharedContextToTime) {
        this.zero = zero;
        this.executionBase = executionBase;
        this.contextToTime = contextToTime;
        this.sharedContextToTime = sharedContextToTime;
    }

    public CachedDiffExecHistory(TIME zero) {
        this(zero, CachedCallContext.executionBase(), new HashMap<>(0x40000), false);
    }

    public CachedDiffExecHistory(TIME zero, CachedDiffExecHistory<TIME> history) {
        this(zero, history.executionBase, history.contextToTime, true);
    }

    public void onReturnValue(TIME startTime, TIME endTime, CachedExecutionContext ctx, Object value) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.computeIfAbsent(ctx.getCurrentNodeIdentifier(), it -> new HashMap<>())
                .put(ctx.getCallContext(), new TimeInfo<>(startTime, endTime, value));
    }

    public void onReturnExceptional(TIME startTime, TIME endTime, CachedExecutionContext ctx, RuntimeException e) {
        timeToContext.add(new ItemWithTime<>(endTime, ctx));
        contextToTime.computeIfAbsent(ctx.getCurrentNodeIdentifier(), it -> new HashMap<>())
                .put(ctx.getCallContext(), new TimeInfo<>(startTime, endTime, e));
    }

    public void replaceReturnedValueOrException(CachedExecutionContext ctx, Object value) {
        getTimeNN(ctx).setValue(value);
    }

    public LocalVarOperator<TIME> rewriteLocalVariable(CachedExecutionContext ctx, int slot, Object value) {
        final var time = getTimeNN(ctx).getEnd();
        final var op = localVarInfo.get(ctx.getCallContext().getBase());
        final var prevUpdate = op.writeVarList
                .set(ItemWithTime.binarySearchJustIndex(op.writeVarList, time), new ItemWithTime<>(time, new LocalVariableUpdate(slot, value)))
                .item();
        ArrayList<ItemWithTime<TIME, Object>> varList = op.writeVarMap.get(prevUpdate.varName());
        varList.set(ItemWithTime.binarySearchJustIndex(varList, time), new ItemWithTime<>(time, value));
        return op;
    }

    public ObjectUpdate<TIME> rewriteObjectField(TIME currentTime, TIME objGenTime, String fieldName, Object value, boolean fieldChanged) {
        final var prev = objectUpdateList
                .set(ItemWithTime.binarySearchJustIndex(objectUpdateList, currentTime), new ItemWithTime<>(currentTime, new ObjectUpdate<>(objGenTime, fieldName, value)));
        final var fieldList = assertNonNull(
                objectUpdateMap.computeIfAbsent(objGenTime, it -> new HashMap<>())
                        .computeIfAbsent(fieldName, it -> new ArrayList<>())
        );

        if (fieldChanged) {
            final var prevItem = prev.item();
            final var prevHistory = objectUpdateMap.get(prevItem.objectGenCtx()).get(prevItem.fieldName());
            prevHistory.remove(ItemWithTime.binarySearchJustIndex(prevHistory, currentTime));
            fieldList.add(ItemWithTime.binarySearchWhereInsertTo(fieldList, currentTime), new ItemWithTime<>(currentTime, value));
            return prevItem;
        } else {
            fieldList.set(ItemWithTime.binarySearchJustIndex(fieldList, currentTime), new ItemWithTime<>(currentTime, value));
            return null;
        }
    }

    public void onCreateObject(TIME time) {
        objectUpdateMap.putIfAbsent(time, new HashMap<>());
    }

    public void onReadObjectField(TIME time, TIME objGenTime, String field) {
        objectReadList.add(new ItemWithTime<>(time, new ReadObjectField<>(objGenTime, field)));
        objectReadMap.computeIfAbsent(objGenTime, it -> new HashMap<>())
                .computeIfAbsent(field, it -> new ArrayList<>())
                .add(time);
    }

    public void onUpdateObjectWithHash(TIME time, TIME objGenTime, String fieldName, Object newValue) {
        objectUpdateMap.computeIfAbsent(objGenTime, it -> new HashMap<>())
                .computeIfAbsent(fieldName, it -> new ArrayList<>())
                .add(new ItemWithTime<>(time, newValue));
        objectUpdateList.add(new ItemWithTime<>(time, new ObjectUpdate<>(objGenTime, fieldName, newValue)));
    }

    public void onEnterFunction(TIME time, TruffleString funcName, CachedCallContext ctx) {
        functionCalls.add(new ItemWithTime<>(time, Pair.create(ctx, funcName)));
    }

    public Object getReturnedValueOrThrow(CachedExecutionContext ctx) {
        final var result = getTimeNN(ctx).getValue();
        if (result instanceof RuntimeException) throw (RuntimeException) result;
        return result;
    }

    public TIME getNextTime(TIME time) {
        final var i = ItemWithTime.binarySearchJustIndex(timeToContext, time) + 1;
        if (i == timeToContext.size()) return time.incAndSimplify();
        return time.mid(timeToContext.get(i).time());
    }

    public List<TIME> getFieldReadHistory(TIME time, TIME objGenTime, String fieldName) {
        HashMap<String, ArrayList<TIME>> objHistory = objectReadMap.get(objGenTime);
        if (objHistory == null) return List.of();
        ArrayList<TIME> readList = objHistory.get(fieldName);
        if (readList == null) return List.of();
        return Time.subListSince(readList, time);
    }

    public List<ItemWithTime<TIME, Pair<CachedCallContext, TruffleString>>> getFunctionEnters(TIME startTime, TIME endTime) {
        return ItemWithTime.subList(functionCalls, startTime, endTime);
    }

    public LocalVarOperator<TIME> getLocalVarOperator(CachedCallContext ctx, int paramLen) {
        return localVarInfo.computeIfAbsent(ctx, it -> new LocalVarOperator<>(paramLen));
    }

    public HashMap<Integer, ArrayList<ItemWithTime<TIME, Object>>> getLocalHistory(CachedCallContext fca) {
        final var op = localVarInfo.get(fca);
        if (op == null) return null;
        return op.writeVarMap;
    }

    public HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> getObjectHistory(TIME objGenTime) {
        return objectUpdateMap.get(objGenTime);
    }

    public TIME getInitialTime() {
        return timeToContext.isEmpty() ? zero : timeToContext.get(0).time();
    }

    public void deleteRecords(CachedExecutionContext ctx) {
        final var tp = getTime(ctx);
        if (tp == null) return;
        deleteRecords(tp.getStart(), tp.getEnd());
    }

    public void deleteRecords(CachedExecutionContext exclusiveStart, CachedExecutionContext exclusiveEnd) {
        final var startI = ItemWithTime.binarySearchNext(timeToContext, getTimeNN(exclusiveStart).getEnd());
        final var endI = ItemWithTime.binarySearchPrev(timeToContext, getTimeNN(exclusiveEnd).getEnd());

        if (startI >= endI) return;
        final var startTime = timeToContext.get(startI).time();
        final var endTime = timeToContext.get(endI).time();
        deleteRecords(startTime, endTime);
    }

    /**
     * @param startTime inclusive
     * @param endTime inclusive
     */
    public void deleteRecords(TIME startTime, TIME endTime) {
        // delete from timeToContext and contextToTime
        final var contexts = ItemWithTime.subList(timeToContext, startTime, endTime);
        for (var e : contexts) {
            final var ctx = e.item();
            contextToTime.get(ctx.getCurrentNodeIdentifier()).remove(ctx.getCallContext());
        }
        contexts.clear();

        // delete from objectReadList and objectReadMap
        {
            final var readFields = new HashMap<TIME, HashSet<String>>();
            final var deleteObjectReadList = ItemWithTime.subList(objectReadList, startTime, endTime);
            for (var read : deleteObjectReadList) {
                final var item = read.item();
                readFields.computeIfAbsent(item.objGenCtx(), it -> new HashSet<>())
                        .add(item.fieldName());
            }
            for (var e : readFields.entrySet()) {
                final var map = objectReadMap.get(e.getKey());
                for (var field : e.getValue()) {
                    Time.subList(map.get(field), startTime, endTime).clear();
                }
            }
            deleteObjectReadList.clear();
        }

        // delete from objectUpdateList and objectUpdateMap
        {
            final var updatedFields = new HashMap<TIME, HashSet<String>>();
            final var objectUpdateList = ItemWithTime.subList(this.objectUpdateList, startTime, endTime);
            for (var update : objectUpdateList) {
                final var item = update.item();
                updatedFields.computeIfAbsent(item.objectGenCtx(), it -> new HashSet<>())
                        .add(item.fieldName());
            }
            for (var e : updatedFields.entrySet()) {
                final var map = objectUpdateMap.get(e.getKey());
                for (var field : e.getValue()) {
                    ItemWithTime.subList(map.get(field), startTime, endTime).clear();
                }
            }
            objectUpdateList.clear();
        }

        // delete localVarInfo
        int start = ItemWithTime.binarySearchApproximately(functionCalls, startTime);
        final var end = ItemWithTime.binarySearchNext(functionCalls, endTime);
        if (start < 0) {
            start = -start - 1;
        } else {
            deleteFromLocalVarOperator(functionCalls.get(start), startTime, endTime);
        }
        final var functionCalls = this.functionCalls.subList(start, end);
        for (var entry : functionCalls) {
            deleteFromLocalVarOperator(entry, startTime, endTime);
        }

        // delete functionCalls
        functionCalls.clear();
    }

    private void deleteFromLocalVarOperator(ItemWithTime<TIME, Pair<CachedCallContext, TruffleString>> entry, TIME startTime, TIME endTime) {
        final var op = localVarInfo.get(entry.item().getLeft());
        if (op == null) return;
        final var writeVarList = ItemWithTime.subList(op.writeVarList, startTime, endTime);
        final var vars = new BitSet();
        for (var e : writeVarList) {
            final LocalVariableUpdate item = e.item();
            vars.set(item.varName());
        }
        writeVarList.clear();
        {
            int i = -1;
            while ((i = vars.nextSetBit(++i)) >= 0) {
                ItemWithTime.subList(op.writeVarMap.get(i), startTime, endTime).clear();
            }
        }
        for (var e : op.readVariable.entrySet()) {
            Time.subList(e.getValue(), startTime, endTime).clear();
        }
        for (var times : op.readParam) {
            Time.subList(times, startTime, endTime).clear();
        }
    }

    public CachedDiffExecHistory<TIME> merge(CachedDiffExecHistory<TIME> other) {
        if (other.timeToContext.isEmpty()) return this;
        final var initialTime = other.timeToContext.get(0).time();
//        final TIME endTime = other.timeToContext.get(other.timeToContext.size() - 1).time();

        // merge timeToContext
        ItemWithTime.merge(timeToContext, other.timeToContext, initialTime);

        // merge contextToTime
        if (sharedContextToTime) {
            for (var e : other.contextToTime.entrySet()) {
                contextToTime.merge(e.getKey(), e.getValue(), (base, v) -> {
                    base.putAll(v);
                    return base;
                });
            }
        }

        // merge objectReadMap
        for (var entry : other.objectReadMap.entrySet()) {
            objectReadMap.merge(entry.getKey(), entry.getValue(), (base, otherValue) -> {
                for (var fieldEntry: otherValue.entrySet()) {
                    base.merge(fieldEntry.getKey(), fieldEntry.getValue(), (baseEntry, otherEntry) ->
                            Time.merge(baseEntry, otherEntry, initialTime)
                    );
                }
                return base;
            });
        }

        // merge objectReadList
        ItemWithTime.merge(objectReadList, other.objectReadList, initialTime);

        // merge objectUpdateMap
        for (var entry : other.objectUpdateMap.entrySet()) {
            objectUpdateMap.merge(entry.getKey(), entry.getValue(), (base, otherValue) -> {
                for (var fieldEntry : otherValue.entrySet()) {
                    base.merge(fieldEntry.getKey(), fieldEntry.getValue(), (baseEntry, otherEntry) ->
                            ItemWithTime.merge(baseEntry, otherEntry, initialTime)
                    );
                }
                return base;
            });
        }

        // merge objectUpdateList
        ItemWithTime.merge(objectUpdateList, other.objectUpdateList, initialTime);

        // merge localVarInfo
        for (var e : other.localVarInfo.entrySet()) {
            localVarInfo.merge(e.getKey(), e.getValue(), (base, newValue) -> {
                ItemWithTime.merge(base.writeVarList, newValue.writeVarList, initialTime);

                for (var e2 : newValue.writeVarMap.entrySet()) {
                    base.writeVarMap.merge(
                            e2.getKey(),
                            e2.getValue(),
                            (base2, newValue2) -> ItemWithTime.merge(base2, newValue2, initialTime)
                    );
                }

                // readParam
                for (int i = 0; i < newValue.readParam.length; i++) {
                    final var newTimes = newValue.readParam[i];
                    if (newTimes.isEmpty()) continue;
                    final var thisTimes = base.readParam[i];
                    thisTimes.addAll(Time.binarySearchWhereInsertTo(thisTimes, initialTime), newTimes);
                }

                // readVariable
                for (var e2 : newValue.readVariable.entrySet()) {
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

    public CachedCallContext getExecutionBase() {
        return executionBase;
    }

    public TimeInfo<TIME> getTime(CachedExecutionContext ctx) {
        final HashMap<CachedCallContext, TimeInfo<TIME>> map = contextToTime.get(ctx.getCurrentNodeIdentifier());
        if (map == null) return null;
        return map.get(ctx.getCallContext());
    }

    TimeInfo<TIME> getTimeNN(CachedExecutionContext ctx) {
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

    public record ReadObjectField<TIME extends Time<TIME>>(TIME objGenCtx, String fieldName) {}

    public final static class LocalVarOperator<TIME extends Time<TIME>> {
        private final HashMap<Integer, ArrayList<ItemWithTime<TIME, Object>>> writeVarMap = new HashMap<>();
        private final ArrayList<ItemWithTime<TIME, LocalVariableUpdate>> writeVarList = new ArrayList<>();
        private final ArrayList<TIME>[] readParam;
        private final HashMap<Integer, ArrayList<TIME>> readVariable = new HashMap<>();

        public LocalVarOperator(int paramLen) {
            @SuppressWarnings("unchecked")
            final var readParam = this.readParam = new ArrayList[paramLen];
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

        public List<TIME> getReadParam(int paramIndex) {
            return readParam[paramIndex];
        }

        public ArrayList<TIME> getReadVar(int slot) {
            ArrayList<TIME> reads = readVariable.get(slot);
            return reads != null ? reads : new ArrayList<>();
        }
    }
}
