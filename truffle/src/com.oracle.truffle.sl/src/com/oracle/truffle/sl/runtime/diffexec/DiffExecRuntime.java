package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.nodes.controlflow.SLBreakException;
import com.oracle.truffle.sl.nodes.controlflow.SLContinueException;
import com.oracle.truffle.sl.nodes.controlflow.SLReturnException;
import com.oracle.truffle.sl.runtime.SLBigInteger;
import com.oracle.truffle.sl.runtime.SLContext;
import com.oracle.truffle.sl.runtime.SLFunction;
import com.oracle.truffle.sl.runtime.SLFunctionRegistry;
import com.oracle.truffle.sl.runtime.SLNull;
import com.oracle.truffle.sl.runtime.SLObject;
import com.oracle.truffle.sl.runtime.SLObjectBase;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiFunction;

import static com.oracle.truffle.sl.Util.assertNonNull;

public final class DiffExecRuntime<TIME extends Time<TIME>> extends ExecutionHistoryOperator<TIME> {
    private final SLFunctionRegistry functionRegistry;

    private final TIME zero;

    private final LocalVarOperatorHolder localVarOperatorHolder = new LocalVarOperatorHolder(0);
    private final ArrayDeque<boolean[]> parameterFlagStack = new ArrayDeque<>();
    private final ArrayDeque<BitSet> localVarFlagStack = new ArrayDeque<>();
    private final HashMap<TIME, HashSet<String>> objectFieldFlags = new HashMap<>();

    private final DiffExecHistory<TIME> rootHistory;
    private DiffExecHistory<TIME> currentHistory;
    private TIME currentTime;
    private boolean isInExec = false;
    private ExecutionContext lastCalcCtx = null;
    private CallContext currentContext = CallContext.EXECUTION_BASE;
    private final HashMap<TIME, WeakReference<SLObjectBase>> ctxToObj = new HashMap<>();
    private TIME firstHitAtField;
    private TIME firstHitAtFunctionCall;
    public int newExecCount = 0;

    // Metrics
    public int recalcNodeCount = 0;
    public int restoredFieldCount = 0;

    public DiffExecRuntime(DiffExecHistory<TIME> rootHistory, SLFunctionRegistry registry, TIME zero) {
        this.functionRegistry = registry;

        this.zero = zero;
        this.currentTime = zero;

        this.rootHistory = rootHistory;
        this.currentHistory = rootHistory;

        parameterFlagStack.push(new boolean[0]);
        localVarFlagStack.push(new BitSet());

        firstHitAtField = rootHistory.getInitialTime();
        firstHitAtFunctionCall = rootHistory.getInitialTime();
    }

    @Override
    public void onReadArgument(int index) {
        localVarOperatorHolder.onReadParam(currentTime, index);
    }

    @Override
    public void onReadLocalVariable(int slot) {
        localVarOperatorHolder.onReadVariable(currentTime, slot);
    }

    @Override
    public void onReadObjectField(Object receiver, String fieldName) {
        //noinspection unchecked
        currentHistory.onReadObjectField(currentTime, (TIME) ((SLObjectBase) receiver).getObjGenTime(), fieldName);
    }

    @Override
    public void onReadArrayElement(Object array, long index) {
        onReadObjectField(array, Long.toString(index)); // TODO
    }

    @Override
    public Object generateObject(Node node) {
        final var currentTime = this.currentTime;
        final var object = new SLObject(SLLanguage.get(node).getRootShape(), currentTime);
        ctxToObj.put(currentTime, new WeakReference<>(object));
        currentHistory.onCreateObject(currentTime);
        return object;
    }

    @Override
    public void onUpdateLocalVariable(int slot, Object value) {
        localVarOperatorHolder.onUpdateVariable(currentTime, slot, value);
        //noinspection DataFlowIssue
        localVarFlagStack.peek().set(slot);
    }

    @Override
    public void onUpdateObjectField(Object receiver, String fieldName, Object value) {
        @SuppressWarnings("unchecked")
        final var objGenTime = (TIME) ((SLObjectBase) receiver).getObjGenTime();
        currentHistory.onUpdateObjectWithHash(currentTime, objGenTime, fieldName, replaceToMock(value));
        final var newlySet = objectFieldFlags.computeIfAbsent(objGenTime, it -> new HashSet<>())
                .add(fieldName);
        if (newlySet) firstHitAtField = currentHistory.getInitialTime();
    }

    @Override
    public void onUpdateArrayElement(Object array, long index, Object value) {
        onUpdateObjectField(array, Long.toString(index), value); // TODO
    }

    @Override
    public void rewriteLocalVariable(int slot, Object value, NodeIdentifier identifier) {
        currentHistory.rewriteLocalVariable(getExecutionContext(identifier), slot, replaceToMock(value));
        //noinspection DataFlowIssue
        localVarFlagStack.peek().set(slot);
    }

    @Override
    public void rewriteObjectField(Object receiver, String fieldName, Object value, NodeIdentifier identifier, boolean fieldChanged) {
        @SuppressWarnings("unchecked")
        final var objGenTime = (TIME) ((SLObjectBase) receiver).getObjGenTime();
        final var execCtx = getExecutionContext(identifier);
        final var prevUpdate = currentHistory.rewriteObjectField(execCtx, objGenTime, fieldName, replaceToMock(value), fieldChanged);
        final var newlySet = objectFieldFlags.computeIfAbsent(objGenTime, it -> new HashSet<>())
                .add(fieldName);
        if (prevUpdate != null) {
            objectFieldFlags.computeIfAbsent(prevUpdate.objectGenCtx(), it -> new HashSet<>())
                    .add(prevUpdate.fieldName());
        }
        if (newlySet) firstHitAtField = currentHistory.getInitialTime();
    }

    @Override
    public void rewriteArrayElement(Object array, long index, Object value, NodeIdentifier identifier, boolean fieldChanged) {
        rewriteObjectField(array, Long.toString(index), value, identifier, fieldChanged); // TODO
    }

    @Override
    public void deleteHistory(NodeIdentifier identifier) {
        currentHistory.deleteRecords(getExecutionContext(identifier));
    }

    @Override
    public TIME onEnterExpression(NodeIdentifier identifier) {
        return currentTime;
    }

    @Override
    public void onReturnValue(NodeIdentifier identifier, TIME onEnterKey, Object result) {
        currentHistory.onReturnValue(onEnterKey, getAndIncrementTime(), getExecutionContext(identifier), replaceToMock(result));
    }

    @Override
    public void onReturnExceptional(NodeIdentifier identifier, TIME onEnterKey, Throwable exception) {
        if (exception instanceof SLReturnException) {
            final var forRecord = new SLReturnException(replaceToMock(((SLReturnException) exception).getResult()));
            currentHistory.onReturnExceptional(onEnterKey, getAndIncrementTime(), getExecutionContext(identifier), forRecord);
        } else if (exception instanceof ControlFlowException || exception instanceof AbstractTruffleException) {
            currentHistory.onReturnExceptional(onEnterKey, getAndIncrementTime(), getExecutionContext(identifier), (RuntimeException) exception);
        }
    }

    @Override
    public void onEnterFunctionDuringExec(NodeIdentifier callerIdentifier, TruffleString functionName, int argLen) {
        final var currentHistory = this.currentHistory;
        final var currentStack = CallContext.functionCall(this.currentContext, callerIdentifier);
        this.currentContext = currentStack;
        localVarFlagStack.push(new BitSet());
        localVarOperatorHolder.push(argLen, currentStack);
        currentHistory.onEnterFunction(currentTime, functionName, currentStack);
    }

    @Override
    public void onEnterFunctionDuringCalc(NodeIdentifier callerIdentifier, TruffleString functionName, boolean[] argFlags) {
        final DiffExecHistory<TIME> currentHistory = this.currentHistory;
        final var currentStack = CallContext.functionCall(this.currentContext, callerIdentifier);
        this.currentContext = currentStack;
        localVarFlagStack.push(new BitSet());
        localVarOperatorHolder.push(argFlags.length, currentStack);
        parameterFlagStack.push(argFlags);
        currentHistory.onEnterFunction(currentHistory.getTimeNN(lastCalcCtx).getEnd(), functionName, currentStack);
    }

    @Override
    public void onExitFunction(boolean duringCalc) {
        final var elem = currentContext;
        assert elem.isFunctionCall();
        currentContext = elem.getRoot();
        localVarOperatorHolder.pop();
        localVarFlagStack.pop();
        if (duringCalc) parameterFlagStack.pop();
    }

    @Override
    public void onEnterLoop(NodeIdentifier identifier) {
        this.currentContext = CallContext.loop(this.currentContext, identifier);
    }

    @Override
    public void onEnterNextIteration() {
        CallContext elem = currentContext;
        assert elem.isLoop();
        currentContext = CallContext.loopNextIter(elem);
    }

    @Override
    public void onExitLoop() {
        CallContext elem = currentContext;
        assert elem.isLoop();
        currentContext = elem.getRoot();
    }

    @Override
    public void calcVoid(VirtualFrame frame, SLStatementNode node) {
//        System.out.println("call calcVoid: " + node.getClass().getName() + " @ " + node.getSourceSection());
        final var unwrapped = node.unwrap();
        if (unwrapped != null) {
            calcVoid(frame, unwrapped);
            return;
        }

        try {
            switch (shouldReExecute(node)) {
                case USE_CACHE -> getReturnedValueOrThrow(node.getNodeIdentifier());
                case RE_EXECUTE -> {
                    try {
                        reExecuteVoid(node.getNodeIdentifier(), frame, node);
                    } catch (SLReturnException | SLBreakException | SLContinueException e) {
                        currentHistory.deleteRecords(lastCalcCtx, getExecutionContext(node.getNodeIdentifier()));
                        throw e;
                    }
                }
                case NEW_EXECUTE -> newExecutionVoid(node.getNodeIdentifier(), frame, node);
            }
        } finally {
            endCalc(node);
        }
    }

    @Override
    public CalcResult.Generic calcGeneric(VirtualFrame frame, SLExpressionNode calleeNode) {
//        System.out.println("call calcGeneric: " + calleeNode.getClass().getName() + " @ " + calleeNode.getSourceSection());
        final SLExpressionNode unwrapped = calleeNode.unwrap();
        if (unwrapped != null) return calcGeneric(frame, unwrapped);
        try {
            return switch (shouldReExecute(calleeNode)) {
                case USE_CACHE -> CalcResult.Generic.cached(getReturnedValueOrThrow(calleeNode.getNodeIdentifier()));
                case RE_EXECUTE -> reExecuteGeneric(calleeNode.getNodeIdentifier(), frame, calleeNode);
                case NEW_EXECUTE -> CalcResult.Generic.fresh(newExecutionGeneric(calleeNode.getNodeIdentifier(), frame, calleeNode));
            };
        } catch (SLReturnException | SLBreakException | SLContinueException e) {
            currentHistory.deleteRecords(lastCalcCtx, getExecutionContext(calleeNode.getNodeIdentifier()));
            throw e;
        } finally {
            endCalc(calleeNode);
        }
    }

    @Override
    public CalcResult.Boolean calcBoolean(VirtualFrame frame, SLStatementNode callerNode, SLExpressionNode calleeNode) {
//        System.out.println("call calcBoolean: " + calleeNode.getClass().getName() + " @ " + calleeNode.getSourceSection());
        final SLExpressionNode unwrapped = calleeNode.unwrap();
        if (unwrapped != null) return calcBoolean(frame, callerNode, unwrapped);
        try {
            return switch (shouldReExecute(calleeNode)) {
                case USE_CACHE -> {
                    final Object obj = getReturnedValueOrThrow(calleeNode.getNodeIdentifier());
                    if (obj instanceof Boolean) {
                        yield CalcResult.Boolean.cached((Boolean) obj);
                    } else {
                        throw new UnexpectedResultException(obj);
                    }
                }
                case RE_EXECUTE -> reExecuteBoolean(calleeNode.getNodeIdentifier(), frame, calleeNode);
                case NEW_EXECUTE -> newExecutionBoolean(calleeNode.getNodeIdentifier(), frame, calleeNode);
            };
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(callerNode, ex.getResult());
        } finally {
            endCalc(calleeNode);
        }
    }

    @Override
    public CalcResult.Long calcLong(VirtualFrame frame, SLExpressionNode callerNode, SLExpressionNode calleeNode) {
//        System.out.println("call calcLong: " + calleeNode.getClass().getName() + " @ " + calleeNode.getSourceSection());
        final SLExpressionNode unwrapped = calleeNode.unwrap();
        if (unwrapped != null) return calcLong(frame, callerNode, unwrapped);
        try {
            return switch (shouldReExecute(calleeNode)) {
                case USE_CACHE -> {
                    final Object obj = getReturnedValueOrThrow(calleeNode.getNodeIdentifier());
                    if (obj instanceof Long) {
                        yield CalcResult.Long.cached((Long) obj);
                    } else {
                        throw new UnexpectedResultException(obj);
                    }
                }
                case RE_EXECUTE -> reExecuteLong(calleeNode.getNodeIdentifier(), frame, calleeNode);
                case NEW_EXECUTE -> newExecutionLong(calleeNode.getNodeIdentifier(), frame, calleeNode);
            };
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(callerNode, ex.getResult());
        } finally {
            endCalc(calleeNode);
        }
    }

    @Override
    public boolean isInitialExecution() {
        final var isInitialExecution = SLContext.isInitialExecution;
        if (isInitialExecution) {
            SLContext.isInitialExecution = false;
            isInExec = true;
        }
        return isInitialExecution;
    }

    public CalcResult.Generic newExecutionGeneric(NodeIdentifier identifier, VirtualFrame frame, SLExpressionNode node) {
        final var tickNode = getNewTickNode(identifier);
        startNewExecution(frame, identifier, node);
        tickNode.onEnter(frame);
        try {
            final Object result = node.executeGeneric(frame);
            tickNode.onReturnValue(frame, result);
            return CalcResult.Generic.fresh(result);
        } catch (Throwable e) {
            tickNode.onReturnExceptional(frame, e);
            throw e;
        } finally {
            endNewExecution();
        }
    }

    public CalcResult.Boolean newExecutionBoolean(NodeIdentifier identifier, VirtualFrame frame, SLExpressionNode node) {
        final var tickNode = getNewTickNode(identifier);
        startNewExecution(frame, identifier, node);
        tickNode.onEnter(frame);
        try {
            final boolean result = node.executeBoolean(frame);
            tickNode.onReturnValue(frame, result);
            return CalcResult.Boolean.fresh(result);
        } catch (UnexpectedResultException ex) {
            final SLException slEx = SLException.typeError(node, ex.getResult());
            tickNode.onReturnExceptional(frame, slEx);
            throw slEx;
        } catch (Throwable e) {
            tickNode.onReturnExceptional(frame, e);
            throw e;
        } finally {
            endNewExecution();
        }
    }

    public CalcResult.Long newExecutionLong(NodeIdentifier identifier, VirtualFrame frame, SLExpressionNode node) {
        final var tickNode = getNewTickNode(identifier);
        startNewExecution(frame, identifier, node);
        tickNode.onEnter(frame);
        try {
            final long result = node.executeLong(frame);
            tickNode.onReturnValue(frame, result);
            return CalcResult.Long.fresh(result);
        } catch (UnexpectedResultException ex) {
            final SLException slEx = SLException.typeError(node, ex.getResult());
            tickNode.onReturnExceptional(frame, slEx);
            throw slEx;
        } catch (Throwable e) {
            tickNode.onReturnExceptional(frame, e);
            throw e;
        } finally {
            endNewExecution();
        }
    }

    public void newExecutionVoid(NodeIdentifier identifier, VirtualFrame frame, SLStatementNode node) {
        final var tickNode = getNewTickNode(identifier);
        startNewExecution(frame, identifier, node);
        tickNode.onEnter(frame);
        try {
            node.executeVoid(frame);
            tickNode.onReturnValue(frame, null);
        } catch (Throwable e) {
            tickNode.onReturnExceptional(frame, e);
            throw e;
        } finally {
            endNewExecution();
        }
    }


    @Override
    public Object getVariableValue(int slot, NodeIdentifier identifier) {
        final var time = currentHistory.getTimeNN(getExecutionContext(identifier)).getEnd();
        //noinspection DataFlowIssue
        final var varHistory = currentHistory.getLocalHistory(currentContext.getBase()).get(slot);
        return revertObject(varHistory.get(ItemWithTime.binarySearchApply(varHistory, time)).item());
    }

    @Override
    public Object getObjectFieldValue(Node node, Object receiver, String fieldName, NodeIdentifier identifier) {
        @SuppressWarnings("unchecked")
        final var objGenTime = (TIME)((SLObjectBase) receiver).getObjGenTime();
        final var objectHistory = currentHistory.getObjectHistory(objGenTime);
        final var fieldHistory = objectHistory.get(fieldName);
        if (fieldHistory == null) throw SLUndefinedNameException.undefinedProperty(node, fieldName);
        final var time = currentHistory.getTimeNN(getExecutionContext(identifier)).getEnd();
        final var i = ItemWithTime.binarySearchApply(fieldHistory, time);
        if (i < 0) throw SLUndefinedNameException.undefinedProperty(node, fieldName);
        return revertObject(fieldHistory.get(i).item());
    }

    @Override
    public Object getArrayElementValue(Node node, Object array, long index, NodeIdentifier identifier) {
        return getObjectFieldValue(node, array, Long.toString(index), identifier);
    }

    public void startNewExecution(VirtualFrame frame, NodeIdentifier identifier, Node node) {
//        System.out.println("Start New Execution: " + node.getSourceSection() + " / isInExec" + isInExec);
        if (isInExec) return;
        isInExec = true;
        final var history = currentHistory;
        final var time = history.getTime(getExecutionContext(identifier));
        if (time != null) history.deleteRecords(time.getStart(), time.getEnd());
        final var lastCalcCtx = this.lastCalcCtx;
        if (lastCalcCtx != null) {
            final var tp = history.getTime(lastCalcCtx);
            currentTime = tp == null ? this.currentTime.inc() : history.getNextTime(tp.getEnd());
        }
        constructFrameAndObjects(frame);
        currentHistory = new DiffExecHistory<>(zero, currentHistory);
        localVarOperatorHolder.duplicate();
    }

    public void endNewExecution() {
        currentHistory = rootHistory.merge(currentHistory);
        localVarOperatorHolder.pop();
        isInExec = false;
        newExecCount++;
//        System.out.println("End New Execution");
    }

    private void endCalc(SLStatementNode node) {
        lastCalcCtx = getExecutionContext(node.getNodeIdentifier());
        recalcNodeCount++;
    }

    @SuppressWarnings("ConstantValue")
    private ShouldReExecuteResult shouldReExecute(SLStatementNode node) {
        final var logging = false;
        if (node.isNewNode()) {
            if (logging) System.out.println("New Execution: isNewNode");
            return ShouldReExecuteResult.NEW_EXECUTE;
        }
        if (node.hasNewNode()) {
            if (logging) System.out.println("Excuse: hasNewNode");
            return ShouldReExecuteResult.RE_EXECUTE;
        }

        final var currentHistory = this.currentHistory;
        final var nodeIdentifier = node.getNodeIdentifier();
        final var execCtx = getExecutionContext(nodeIdentifier);
        final var tp = currentHistory.getTime(execCtx);
        if (tp == null) {
            if (logging) System.out.println("New Execution: no prev exec");
            return ShouldReExecuteResult.NEW_EXECUTE;
        }

        var fcStart = this.firstHitAtFunctionCall;
        if (fcStart.compareTo(tp.getEnd()) < 0) {
            fcStart = Time.max(fcStart, tp.getStart());
            for (var entry : currentHistory.getFunctionEnters(fcStart, tp.getEnd())) {
                if (functionRegistry.containNewNode(entry.item().getRight())) {
                    firstHitAtFunctionCall = entry.time();
                    if (logging) System.out.println("Excuse: new function: " + entry.item().getRight());
                    return ShouldReExecuteResult.RE_EXECUTE;
                }
            }
            firstHitAtFunctionCall = currentHistory.getNextTime(tp.getEnd());
        }

        DiffExecHistory.LocalVarOperator<TIME> op = localVarOperatorHolder.peek();

        ShouldReExecuteResult reExecute2 = checkVarRead(node, op, tp, logging);
        if (reExecute2 != ShouldReExecuteResult.USE_CACHE) return reExecute2;

        ShouldReExecuteResult reExecute1 = checkParamRead(node, op, tp, logging);
        if (reExecute1 != ShouldReExecuteResult.USE_CACHE) return reExecute1;

        ShouldReExecuteResult reExecute = checkObjectRead(node, tp, logging);
        if (reExecute != ShouldReExecuteResult.USE_CACHE) return reExecute;

        if (logging) System.out.println("Using cache!: " + node.getSourceSection());
        return ShouldReExecuteResult.USE_CACHE;
    }

    private ShouldReExecuteResult checkVarRead(SLStatementNode node, DiffExecHistory.LocalVarOperator<TIME> op, DiffExecHistory.TimeInfo<TIME> tp, boolean logging) {
        final var localVarFlags = assertNonNull(localVarFlagStack.peek());
        int slot = -1;
        while ((slot = localVarFlags.nextSetBit(++slot)) >= 0) {
            final var readVarHistory = op.getReadVar(slot);
            if (readVarHistory == null || readVarHistory.isEmpty()) continue;
            if (Time.existsRecord(readVarHistory, tp.getStart(), tp.getEnd())) {
                if (logging) System.out.println("Excuse: flagged var / " + slot + " @ " + node.getSourceSection() + " in " + tp.getStart() + "~" + tp.getEnd());
                return ShouldReExecuteResult.RE_EXECUTE;
            }
        }
        return ShouldReExecuteResult.USE_CACHE;
    }

    private ShouldReExecuteResult checkParamRead(SLStatementNode node, DiffExecHistory.LocalVarOperator<TIME> op, DiffExecHistory.TimeInfo<TIME> tp, boolean logging) {
        final boolean[] paramFlags = assertNonNull(parameterFlagStack.peek());
        for (int i = 0; i < paramFlags.length; i++) {
            if (!paramFlags[i]) continue;
            ArrayList<TIME> readParamHistory = op.getReadParam(i);
            if (readParamHistory.isEmpty()) continue;
            if (Time.existsRecord(readParamHistory, tp.getStart(), tp.getEnd())) {
                if (logging) System.out.println("Excuse: flagged param / " + i + " @ " + node.getSourceSection() + " in " + tp.getStart() + "~" + tp.getEnd());
                return ShouldReExecuteResult.RE_EXECUTE;
            }
        }
        return ShouldReExecuteResult.USE_CACHE;
    }

    private ShouldReExecuteResult checkObjectRead(SLStatementNode node, DiffExecHistory.TimeInfo<TIME> tp, boolean logging) {
        TIME fldStart = firstHitAtField;
        if (!objectFieldFlags.isEmpty() && fldStart.compareTo(tp.getEnd()) < 0) {
            fldStart = tp.getStart();
            for (var entry : this.currentHistory.getReadOperations(fldStart, tp.getEnd())) {
                final var fields = objectFieldFlags.get(entry.item().objGenCtx());
                if (fields != null && fields.contains(entry.item().fieldName())) {
                    firstHitAtField = entry.time();
                    if (logging) System.out.println("Excuse: flagged Fld / " + entry.item().objGenCtx() + ", " + entry.item().fieldName() + " @ " + node.getSourceSection() + " in " + tp.getStart() + "~" + tp.getEnd());
                    return ShouldReExecuteResult.RE_EXECUTE;
                }
            }
            firstHitAtField = this.currentHistory.getNextTime(tp.getEnd());
        }
        return ShouldReExecuteResult.USE_CACHE;
    }

    private void reExecuteVoid(NodeIdentifier identifier, VirtualFrame frame, SLStatementNode node) {
        try {
            node.calcVoidInner(frame);
        } catch (Throwable e) {
            currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), e);
            throw e;
        }
        currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), null);
    }

    private CalcResult.Generic reExecuteGeneric(NodeIdentifier identifier, VirtualFrame frame, SLExpressionNode node) {
        CalcResult.Generic value;
        try {
            value = node.calcGenericInner(frame);
        } catch (Throwable e) {
            currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), e);
            throw e;
        }
        currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), replaceToMock(value.getResult()));
        return value;
    }

    public CalcResult.Boolean reExecuteBoolean(NodeIdentifier identifier, VirtualFrame frame, SLExpressionNode node) throws UnexpectedResultException {
        CalcResult.Boolean value;
        try {
            value = node.calcBooleanInner(frame);
        } catch (Throwable e) {
            currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), e);
            throw e;
        }
        currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), replaceToMock(value.getResult()));
        return value;
    }

    public CalcResult.Long reExecuteLong(NodeIdentifier identifier, VirtualFrame frame, SLExpressionNode node) throws UnexpectedResultException {
        CalcResult.Long value;
        try {
            value = node.calcLongInner(frame);
        } catch (Throwable e) {
            currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), e);
            throw e;
        }
        currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), replaceToMock(value.getResult()));
        return value;
    }

    private TIME getAndIncrementTime() {
        TIME tmp = currentTime;
        currentTime = tmp.inc();
        return tmp;
    }

    private ExecutionContext getExecutionContext(NodeIdentifier identifier) {
        return new ExecutionContext(currentContext, identifier);
    }

    private Object getReturnedValueOrThrow(NodeIdentifier identifier) {
        return getReturnedValueOrThrow(getExecutionContext(identifier));
    }

    private Object getReturnedValueOrThrow(ExecutionContext execCtx) {
        try {
            //noinspection UnnecessaryLocalVariable
            final Object o = revertObject(currentHistory.getReturnedValueOrThrow(execCtx));
//            System.out.println("Skipped " + execCtx + "/ Returned value: " + o);
            return o;
        } catch (SLReturnException e) {
            throw new SLReturnException(revertObject(e.getResult()));
        }
    }

    private void constructFrameAndObjects(VirtualFrame frame) {
        final TIME time = currentTime;
        final var history = currentHistory;
        final var local = history.getLocalHistory(currentContext.getBase());
        if (local == null) return;

        for (Map.Entry<Integer, ArrayList<ItemWithTime<TIME, Object>>> entry : local.entrySet()) {
            final int slot = entry.getKey();
            final var list = entry.getValue();

            int i = ItemWithTime.binarySearchApply(list, time);
            if (i < 0) continue;
            Object value = list.get(i).item();
            if (value instanceof Long) {
                frame.setLong(slot, (Long) value);
            } else if (value instanceof Boolean) {
                frame.setBoolean(slot, (Boolean) value);
            } else if (value instanceof SLBigInteger
                    || value instanceof String
                    || value instanceof TruffleString
                    || value == SLNull.SINGLETON) {
                frame.setObject(slot, value);
            } else if (value instanceof ObjectReference) {
                //noinspection unchecked
                Object obj = generateObject(((ObjectReference<TIME>) value).objGenCtx());
                frame.setObject(slot, obj);
            } else if (value instanceof FunctionReference) {
                frame.setObject(slot, getFunction(((FunctionReference) value).functionName()));
            } else {
                throw new RuntimeException("Unknown value type: " + value.getClass().getName());
            }
        }

    }

    private SLObjectBase generateObject(TIME objGenCtx) {
        final var remappingFunction = new BiFunction<TIME, WeakReference<SLObjectBase>, WeakReference<SLObjectBase>>() {
            private SLObjectBase obj;

            @Override
            public WeakReference<SLObjectBase> apply(TIME time, WeakReference<SLObjectBase> ref) {
                if (ref != null) {
                    final var tmp = ref.get();
                    if (tmp != null) {
                        obj = tmp;
                        return ref;
                    }
                }

                final var tmp = obj = new SLDEObject(new ObjectHistory(rootHistory.getObjectHistory(objGenCtx)), time);
                return new WeakReference<>(tmp);
            }
        };
        ctxToObj.compute(objGenCtx, remappingFunction);
        return remappingFunction.obj;
    }

    private Object replaceToMock(Object value) {
        if (value == null
                || value instanceof Long
                || value instanceof Boolean
                || value instanceof String
                || value instanceof SLBigInteger
                || value instanceof TruffleString
                || value == SLNull.SINGLETON) {
            return value;
        } else if (value instanceof SLFunction) {
            return new FunctionReference(((SLFunction) value).getName());
        } else if (value instanceof SLObjectBase) {
            //noinspection unchecked
            return new ObjectReference<>((TIME) ((SLObjectBase) value).getObjGenTime());
        } else {
            throw new RuntimeException("Unavailable object: " + value.getClass().getName());
        }
    }
    
    private Object revertObject(Object value) {
        if (value instanceof Long
                || value instanceof Boolean
                || value instanceof SLBigInteger
                || value instanceof String
                || value instanceof TruffleString
                || value == SLNull.SINGLETON) {
            return value;
        } else if (value instanceof ObjectReference) {
            //noinspection unchecked
            TIME objGenTime = ((ObjectReference<TIME>) value).objGenCtx();
            return generateObject(objGenTime);
        } else if (value instanceof FunctionReference funcRef) {
            final SLFunction function = getFunction(funcRef.functionName());
            assert function != null : "Unknown function: " + funcRef.functionName();
            return function;
        } else if (value == null) {
            return null;
        } else {
            throw new RuntimeException("Invalid type: " + value.getClass().getName());
        }
    }

    private SLFunction getFunction(TruffleString functionName) {
        return functionRegistry.getFunction(functionName);
    }

    private final class ObjectHistory implements SLDEObject.ObjectHistory {
        private final HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> objectHistory;
        private final HashMap<String, Integer> lastUpdateNewExecCount = new HashMap<>();

        private ObjectHistory(HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> objectHistory) {
            this.objectHistory = objectHistory;
        }

        @Override
        public boolean existsField(String fieldName) {
            final var fieldHistory = objectHistory.get(fieldName);
            return fieldHistory != null
                    && !fieldHistory.isEmpty()
                    && fieldHistory.get(0).time().compareTo(currentTime) <= 0;
        }

        @Override
        public Object getObjectFieldValue(String fieldName) {
            restoredFieldCount++;
            final var fieldHistory = objectHistory.get(fieldName);
            if (fieldHistory == null) return null;
            final var i = ItemWithTime.binarySearchApply(fieldHistory, currentTime);
            if (i < 0) return null;
            return revertObject(fieldHistory.get(i).item());
        }

        @Override
        public boolean canUseCache(String member) {
            final Integer last = lastUpdateNewExecCount.get(member);
            return last != null && last == newExecCount;
        }

        @Override
        public void onWrite(String member) {
            lastUpdateNewExecCount.put(member, newExecCount);
        }
    }

    private final class LocalVarOperatorHolder {
        private ScopeInfo<TIME>[] stack;
        private int pointer = 1;

        public LocalVarOperatorHolder(int executionParamLen) {
            @SuppressWarnings("unchecked")
            final ScopeInfo<TIME>[] stack = new ScopeInfo[32];
            stack[0] = new ScopeInfo<>(executionParamLen, CallContext.EXECUTION_BASE);
            this.stack = stack;
        }

        public void push(int paramLen, CallContext ctx) {
            var stack = this.stack;
            final var currentLen = stack.length;
            if (pointer == currentLen) this.stack = stack = Arrays.copyOf(stack, currentLen + (currentLen >> 1));
            stack[pointer++] = new ScopeInfo<>(paramLen, ctx);
        }

        public void duplicate() {
            ScopeInfo<TIME> info = stack[pointer - 1];
            push(info.paramLen, info.cc);
        }

        public void pop() {
            assert pointer >= 0;
            stack[--pointer] = null;
        }

        public DiffExecHistory.LocalVarOperator<TIME> peek() {
            ScopeInfo<TIME> info = stack[pointer - 1];
            if (info.op != null) return info.op;
            return info.op = currentHistory.getLocalVarOperator(info.cc, info.paramLen);
        }

        public void onUpdateVariable(TIME time, int slot, Object newValue) {
            peek().onUpdateVariable(time, slot, replaceToMock(newValue));
        }

        public void onReadVariable(TIME time, int slot) {
            peek().onReadVariable(time, slot);
        }

        public void onReadParam(TIME time, int paramIndex) {
            peek().onReadParam(time, paramIndex);
        }

        private static final class ScopeInfo<TIME extends Time<TIME>> {
            final int paramLen;
            final CallContext cc;
            DiffExecHistory.LocalVarOperator<TIME> op = null;

            public ScopeInfo(int paramLen, CallContext cc) {
                this.paramLen = paramLen;
                this.cc = cc;
            }
        }
    }

    private enum ShouldReExecuteResult {
        USE_CACHE, RE_EXECUTE, NEW_EXECUTE
    }
}
