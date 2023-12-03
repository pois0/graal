package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.nodes.controlflow.SLBreakException;
import com.oracle.truffle.sl.nodes.controlflow.SLContinueException;
import com.oracle.truffle.sl.nodes.controlflow.SLReturnException;
import com.oracle.truffle.sl.runtime.SLBigInteger;
import com.oracle.truffle.sl.runtime.SLFunction;
import com.oracle.truffle.sl.runtime.SLFunctionRegistry;
import com.oracle.truffle.sl.runtime.SLNull;
import com.oracle.truffle.sl.runtime.SLObjectBase;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;
import org.graalvm.collections.Pair;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

import static com.oracle.truffle.sl.Util.assertNonNull;

public final class ExecutionHistoryOperatorImpl<TIME extends Time<TIME>> extends ExecutionHistoryOperator<TIME> {
    private static ExecutionHistory rootHistory = new ExecutionHistory(ArrayTime.ZERO);

    private final SLFunctionRegistry functionRegistry;

    private final TIME zero;

    private final LocalVarOperatorHolder localVarOperatorHolder = new LocalVarOperatorHolder(0);
    private final ArrayDeque<boolean[]> parameterFlagStack = new ArrayDeque<>();
    private final ArrayDeque<HashSet<Integer>> localVarFlagStack = new ArrayDeque<>(); // TODO use Bitset
    private final HashMap<TIME, HashSet<Object>> objectFieldFlags = new HashMap<>();

    private ExecutionHistory<TIME> currentHistory;
    private TIME currentTime;
    private boolean isInExec = false;
    private ExecutionContext lastCalcCtx = null;
    private CallContext currentContext = CallContext.ExecutionBase.INSTANCE;
    private WeakHashMap<SLObjectBase, TIME> objToCtx = new WeakHashMap<>();
    private HashMap<TIME, WeakReference<SLObjectBase>> ctxToObj = new HashMap<>();
    private TIME firstHitAtField;
    private TIME firstHitAtFunctionCall;
    private int newExecCount = 0;

    private static boolean rotate = true;
    private static boolean isInitialExecution = true;

    public ExecutionHistoryOperatorImpl(SLFunctionRegistry registry, TIME zero) {
        this.functionRegistry = registry;

        this.zero = zero;
        this.currentTime = zero;

        if (rotate) {
            System.out.println("reset!");
            rootHistory = new ExecutionHistory<>(zero);
            isInitialExecution = true;
        }
        rotate = !rotate;
        this.currentHistory = rootHistory;

        parameterFlagStack.push(new boolean[0]);
        localVarFlagStack.push(new HashSet<>());

        firstHitAtField = (TIME) rootHistory.getInitialTime();
        firstHitAtFunctionCall = (TIME) rootHistory.getInitialTime();
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
        currentHistory.onReadObjectField(currentTime, objToCtx.get((SLObjectBase) receiver), fieldName);
    }

    @Override
    public void onReadArrayElement(Object array, long index) {
        onReadObjectField(array, Long.toString(index)); // TODO
    }

    @Override
    public void onGenerateObject(SLObjectBase object) {
        final TIME currentTime = this.currentTime;
        objToCtx.put(object, currentTime);
        ctxToObj.put(currentTime, new WeakReference<>(object));
        currentHistory.onCreateObject(currentTime);
    }

    @Override
    public void onUpdateLocalVariable(int slot, Object value) {
        localVarOperatorHolder.onUpdateVariable(currentTime, slot, value);
        localVarFlagStack.peek().add(slot);
    }

    @Override
    public void onUpdateObjectField(Object receiver, String fieldName, Object value) {
        TIME objGenTime = objToCtx.get((SLObjectBase) receiver);
        currentHistory.onUpdateObjectWithHash(currentTime, objGenTime, fieldName, replaceToMock(value));
        boolean newlySet = objectFieldFlags.computeIfAbsent(objGenTime, it -> new HashSet<>())
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
        localVarFlagStack.peek().add(slot);
    }

    @Override
    public void rewriteObjectField(Object receiver, String fieldName, Object value, NodeIdentifier identifier, boolean fieldChanged) {
        final TIME objGenTime = objToCtx.get((SLObjectBase) receiver);
        ExecutionHistory.ObjectUpdate<TIME> prevUpdate =
                currentHistory.rewriteObjectField(getExecutionContext(identifier), objGenTime, fieldName, replaceToMock(value), fieldChanged);
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
            final SLReturnException forRecord = new SLReturnException(replaceToMock(((SLReturnException) exception).getResult()));
            currentHistory.onReturnExceptional(onEnterKey, getAndIncrementTime(), getExecutionContext(identifier), forRecord);
        } else if (exception instanceof ControlFlowException || exception instanceof AbstractTruffleException) {
            currentHistory.onReturnExceptional(onEnterKey, getAndIncrementTime(), getExecutionContext(identifier), (RuntimeException) exception);
        }
    }

    @Override
    public void onEnterFunctionDuringExec(NodeIdentifier callerIdentifier, TruffleString functionName, int argLen) {
        final ExecutionHistory<TIME> currentHistory = this.currentHistory;
        CallContext.FunctionCall currentStack = new CallContext.FunctionCall(this.currentContext, callerIdentifier);
        this.currentContext = currentStack;
        localVarFlagStack.push(new HashSet<>());
        localVarOperatorHolder.push(argLen, currentStack);
        currentHistory.onEnterFunction(currentTime, functionName, currentStack);
    }

    @Override
    public void onEnterFunctionDuringCalc(NodeIdentifier callerIdentifier, TruffleString functionName, boolean[] argFlags) {
        final ExecutionHistory<TIME> currentHistory = this.currentHistory;
        CallContext.FunctionCall currentStack = new CallContext.FunctionCall(this.currentContext, callerIdentifier);
        this.currentContext = currentStack;
        localVarFlagStack.push(new HashSet<>());
        localVarOperatorHolder.push(argFlags.length, currentStack);
        parameterFlagStack.push(argFlags);
        currentHistory.onEnterFunction(currentHistory.getTimeNN(lastCalcCtx).getEnd(), functionName, currentStack);
    }

    @Override
    public void onExitFunction(boolean duringCalc) {
        CallContext elem = currentContext;
        assert elem instanceof CallContext.FunctionCall;
        currentContext = elem.getRoot();
        localVarOperatorHolder.pop();
        localVarFlagStack.pop();
        if (duringCalc) parameterFlagStack.pop();
    }

    @Override
    public void onEnterLoop(NodeIdentifier identifier) {
        this.currentContext = new CallContext.Loop(this.currentContext, identifier);
    }

    @Override
    public void onEnterNextIteration() {
        CallContext elem = currentContext;
        assert elem instanceof CallContext.Loop;
        currentContext = ((CallContext.Loop) elem).increment();
    }

    @Override
    public void onExitLoop() {
        CallContext elem = currentContext;
        assert elem instanceof CallContext.Loop;
        currentContext = elem.getRoot();
    }

    @Override
    public void calcVoid(VirtualFrame frame, SLStatementNode node) {
//        System.out.println("call calcVoid: " + node.getClass().getName() + " @ " + node.getSourceSection());
        final SLStatementNode unwrapped = node.unwrap();
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
            switch (shouldReExecute(calleeNode)) {
                case USE_CACHE:
                    final Object obj = getReturnedValueOrThrow(calleeNode.getNodeIdentifier());
                    if (obj instanceof Boolean) {
                        return CalcResult.Boolean.cached((Boolean) obj);
                    } else {
                        throw new UnexpectedResultException(obj);
                    }
                case RE_EXECUTE:
                    return reExecuteBoolean(calleeNode.getNodeIdentifier(), frame, calleeNode);
                case NEW_EXECUTE:
                    return newExecutionBoolean(calleeNode.getNodeIdentifier(), frame, calleeNode);
            }
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(callerNode, ex.getResult());
        } finally {
            endCalc(calleeNode);
        }

        throw new RuntimeException("Never reach here");
    }

    @Override
    public CalcResult.Long calcLong(VirtualFrame frame, SLExpressionNode callerNode, SLExpressionNode calleeNode) {
//        System.out.println("call calcLong: " + calleeNode.getClass().getName() + " @ " + calleeNode.getSourceSection());
        final SLExpressionNode unwrapped = calleeNode.unwrap();
        if (unwrapped != null) return calcLong(frame, callerNode, unwrapped);
        try {
            switch (shouldReExecute(calleeNode)) {
                case USE_CACHE:
                    final Object obj = getReturnedValueOrThrow(calleeNode.getNodeIdentifier());
                    if (obj instanceof Long) {
                        return CalcResult.Long.cached((Long) obj);
                    } else {
                        throw new UnexpectedResultException(obj);
                    }
                case RE_EXECUTE:
                    return reExecuteLong(calleeNode.getNodeIdentifier(), frame, calleeNode);
                case NEW_EXECUTE:
                    return newExecutionLong(calleeNode.getNodeIdentifier(), frame, calleeNode);
            }
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(callerNode, ex.getResult());
        } finally {
            endCalc(calleeNode);
        }

        throw new RuntimeException("Never reach here");
    }

    @Override
    public boolean isInitialExecution() {
        final boolean isInitialExecution = ExecutionHistoryOperatorImpl.isInitialExecution;
        if (isInitialExecution) {
            ExecutionHistoryOperatorImpl.isInitialExecution = false;
            isInExec = true;
        }
        return isInitialExecution;
    }

    public CalcResult.Generic newExecutionGeneric(NodeIdentifier identifier, VirtualFrame frame, SLExpressionNode node) {
        final TickNode tickNode = getNewTickNode(identifier);
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
        final TickNode tickNode = getNewTickNode(identifier);
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
        final TickNode tickNode = getNewTickNode(identifier);
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
        final TickNode tickNode = getNewTickNode(identifier);
        startNewExecution(frame, identifier, node);
        tickNode.onEnter(frame);
        try {
            node.executeVoid(frame);
            tickNode.onReturnValue(frame, null);
        } catch (Throwable e) {
            e.printStackTrace();
            tickNode.onReturnExceptional(frame, e);
            throw e;
        } finally {
            endNewExecution();
        }
    }


    @Override
    public Object getVariableValue(int slot, NodeIdentifier identifier) {
        final TIME time = currentHistory.getTimeNN(getExecutionContext(identifier)).getEnd();
        //noinspection DataFlowIssue
        final ArrayList<ItemWithTime<TIME, Object>> varHistory = currentHistory.getLocalHistory(currentContext.getBase()).get(slot);
        return revertObject(varHistory.get(ItemWithTime.binarySearchApply(varHistory, time)).item());
    }

    @Override
    public Object getObjectFieldValue(Node node, Object receiver, String fieldName, NodeIdentifier identifier) {
        TIME objGenTime = objToCtx.get((SLObjectBase) receiver);
        HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> objectHistory = currentHistory.getObjectHistory(objGenTime);
        ArrayList<ItemWithTime<TIME, Object>> fieldHistory = objectHistory.get(fieldName);
        if (fieldHistory == null) {
            System.out.println("fields: " + objectHistory.keySet());
            throw SLUndefinedNameException.undefinedProperty(node, fieldName);
        }
        TIME time = currentHistory.getTimeNN(getExecutionContext(identifier)).getEnd();
        int i = ItemWithTime.binarySearchApply(fieldHistory, time);
        if (i < 0) throw SLUndefinedNameException.undefinedProperty(node, fieldName);
        return revertObject(fieldHistory.get(i).item());
    }

    @Override
    public Object getArrayElementValue(Object array, long index, Object value) {
        return null;
    }

    public void startNewExecution(VirtualFrame frame, NodeIdentifier identifier, Node node) {
        System.out.println("Start New Execution: " + node.getSourceSection() + " / isInExec" + isInExec);
        if (isInExec) return;
//        AbstractPolyglotImpl.testCount++;
        isInExec = true;
        final ExecutionHistory<TIME> history = currentHistory;
        final ExecutionHistory.TimeInfo<TIME> time = history.getTime(getExecutionContext(identifier));
        if (time != null) history.deleteRecords(time.getStart(), time.getEnd());
        final ExecutionContext lastCalcCtx = this.lastCalcCtx;
        if (lastCalcCtx != null) {
            final ExecutionHistory.TimeInfo<TIME> tp = history.getTime(lastCalcCtx);
            currentTime = tp == null ? this.currentTime.inc() : history.getNextTime(tp.getEnd());
        }
        constructFrameAndObjects(frame);
        currentHistory = new ExecutionHistory<>(zero);
        localVarOperatorHolder.duplicate();
    }

    public void endNewExecution() {
        //noinspection unchecked
        currentHistory = rootHistory.merge(currentHistory);
        localVarOperatorHolder.pop();
        isInExec = false;
        newExecCount++;
        System.out.println("End New Execution");
    }

    private void endCalc(SLStatementNode node) {
        lastCalcCtx = getExecutionContext(node.getNodeIdentifier());
    }

    @SuppressWarnings("ConstantValue")
    private ShouldReExecuteResult shouldReExecute(SLStatementNode node) {
        final boolean logging = false;
        if (node.isNewNode()) {
            if (logging) System.out.println("New Execution: isNewNode");
            return ShouldReExecuteResult.NEW_EXECUTE;
        }
        if (node.hasNewNode()) {
            if (logging) System.out.println("Excuse: hasNewNode");
            return ShouldReExecuteResult.RE_EXECUTE;
        }

        final ExecutionHistory<TIME> currentHistory = this.currentHistory;
        NodeIdentifier nodeIdentifier = node.getNodeIdentifier();
        ExecutionContext execCtx = getExecutionContext(nodeIdentifier);
        ExecutionHistory.TimeInfo<TIME> tp = currentHistory.getTime(execCtx);
        if (tp == null) {
            if (logging) System.out.println("New Execution: no prev exec");
            return ShouldReExecuteResult.NEW_EXECUTE;
        }

        TIME fcStart = this.firstHitAtFunctionCall;
        if (fcStart.compareTo(tp.getEnd()) < 0) {
            fcStart = Time.max(fcStart, tp.getStart());
            for (ItemWithTime<TIME, Pair<CallContext.ContextBase, TruffleString>> entry : currentHistory.getFunctionEnters(fcStart, tp.getEnd())) {
                if (functionRegistry.containNewNode(entry.item().getRight())) {
                    firstHitAtFunctionCall = entry.time();
                    if (logging) System.out.println("Excuse: new function: " + entry.item().getRight());
                    return ShouldReExecuteResult.RE_EXECUTE;
                }
            }
            firstHitAtFunctionCall = currentHistory.getNextTime(tp.getEnd());
        }

        ExecutionHistory.LocalVarOperator<TIME> op = localVarOperatorHolder.peek();

        final HashSet<Integer> localVarFlags = assertNonNull(localVarFlagStack.peek());
        for (int slot : localVarFlags) {
            ArrayList<TIME> readVarHistory = op.getReadVar(slot);
            if (readVarHistory == null || readVarHistory.isEmpty()) continue;
            final int start = Time.binarySearchWhereInsertTo(readVarHistory, tp.getStart());
            final int end = Time.binarySearchNext(readVarHistory, tp.getEnd());
            if (start != end) {
                if (logging) System.out.println("Excuse: flagged var / " + slot + " @ " + node.getSourceSection() + " in " + tp.getStart() + "~" + tp.getEnd());
                return ShouldReExecuteResult.RE_EXECUTE;
            }
        }

        final boolean[] paramFlags = assertNonNull(parameterFlagStack.peek());
        for (int i = 0; i < paramFlags.length; i++) {
            if (!paramFlags[i]) continue;
            ArrayList<TIME> readParamHistory = op.getReadParam(i);
            if (readParamHistory.isEmpty()) continue;
            final int start = Time.binarySearchWhereInsertTo(readParamHistory, tp.getStart());
            final int end = Time.binarySearchNext(readParamHistory, tp.getEnd());
            if (start != end) {
                if (logging) System.out.println("Excuse: flagged param / " + i + " @ " + node.getSourceSection() + " in " + tp.getStart() + "~" + tp.getEnd());
                return ShouldReExecuteResult.RE_EXECUTE;
            }
        }


        TIME fldStart = firstHitAtField;
        if (!objectFieldFlags.isEmpty() && fldStart.compareTo(tp.getEnd()) < 0) {
            fldStart = tp.getStart();
            for (ItemWithTime<TIME, ExecutionHistory.ReadObjectField<TIME>> entry : this.currentHistory.getReadOperations(fldStart, tp.getEnd())) {
                HashSet<Object> fields = objectFieldFlags.get(entry.item().objGenCtx());
                if (fields != null && fields.contains(entry.item().fieldName())) {
                    firstHitAtField = entry.time();
                    if (logging) System.out.println("Excuse: flagged Fld / " + entry.item().objGenCtx() + ", " + entry.item().fieldName() + " @ " + node.getSourceSection() + " in " + tp.getStart() + "~" + tp.getEnd());
                    return ShouldReExecuteResult.RE_EXECUTE;
                }
            }
            firstHitAtField = this.currentHistory.getNextTime(tp.getEnd());
        }

        if (logging) System.out.println("Using cache!: " + node.getSourceSection());
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
        ExecutionHistory<TIME> history = currentHistory;
        HashMap<Integer, ArrayList<ItemWithTime<TIME, Object>>> local = history.getLocalHistory(currentContext.getBase());
        if (local == null) return;

        final HashMap<TIME, WeakReference<SLObjectBase>> ctxToObj = new HashMap<>(this.ctxToObj);
        final WeakHashMap<SLObjectBase, TIME> objToCtx = new WeakHashMap<>(this.objToCtx);

        for (Map.Entry<Integer, ArrayList<ItemWithTime<TIME, Object>>> entry : local.entrySet()) {
            int slot = entry.getKey();
            ArrayList<ItemWithTime<TIME, Object>> list = entry.getValue();

            int i = ItemWithTime.binarySearchApply(list, time);
            if (i < 0) continue;
            Object value = list.get(i).item();
            if (value instanceof Long) {
                frame.setLong(slot, (Long) value);
            } else if (value instanceof Boolean) {
                frame.setBoolean(slot, (Boolean) value);
            } else if (value instanceof SLBigInteger || value instanceof String || value instanceof TruffleString || value == SLNull.SINGLETON) {
                frame.setObject(slot, value);
            } else if (value instanceof ExecutionHistory.ObjectReference) {
                //noinspection unchecked
                Object obj = generateObject(((ExecutionHistory.ObjectReference<TIME>) value).objGenCtx(), history, ctxToObj);
                frame.setObject(slot, obj);
            } else if (value instanceof FunctionReference) {
                frame.setObject(slot, getFunction(((FunctionReference) value).functionName()));
            } else {
                throw new RuntimeException("Unknown value type: " + value.getClass().getName());
            }
        }

        this.ctxToObj = ctxToObj;
        this.objToCtx = objToCtx;
    }

    private SLObjectBase generateObject(TIME objGenCtx, ExecutionHistory<TIME> history, HashMap<TIME, WeakReference<SLObjectBase>> ctxToObj) {
        return ctxToObj.computeIfAbsent(objGenCtx,
                it -> {
                    final HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> objectHistory = rootHistory.getObjectHistory(it);
                    SLObjectBase object = new SLDEObject(new ObjectHistory(objectHistory));
                    objToCtx.put(object, it);
                    currentHistory.onCreateObject(it);
                    return new WeakReference<>(object);
                })
                .get();
    }

    private Object replaceToMock(Object value) {
        Object saveNewValue;
        if (value == null
                || value instanceof Long
                || value instanceof Boolean
                || value instanceof String
                || value instanceof SLBigInteger
                || value instanceof TruffleString
                || value == SLNull.SINGLETON) {
            saveNewValue = value;
        } else if (value instanceof SLFunction) {
            saveNewValue = new FunctionReference(((SLFunction) value).getName());
        } else if (value instanceof SLObjectBase) {
            saveNewValue = new ExecutionHistory.ObjectReference<>(objToCtx.get((SLObjectBase) value));
        } else {
            throw new RuntimeException("Unavailable object: " + value.getClass().getName());
        }

        return saveNewValue;
    }
    
    private Object revertObject(Object value) {
        if (value instanceof Long
                || value instanceof Boolean
                || value instanceof SLBigInteger
                || value instanceof String
                || value instanceof TruffleString
                || value == SLNull.SINGLETON) {
            return value;
        } else if (value instanceof ExecutionHistory.ObjectReference) {
            //noinspection unchecked
            TIME objGenTime = ((ExecutionHistory.ObjectReference<TIME>) value).objGenCtx();
            return generateObject(objGenTime, currentHistory, ctxToObj);
        } else if (value instanceof FunctionReference funcRef) {
            final SLFunction function = getFunction(funcRef.functionName());
            assert function != null : "Unknown function: " + funcRef.functionName;
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

    private class ObjectHistory implements SLDEObject.ObjectHistory {
        private final HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> objectHistory;
        private final HashMap<String, Integer> lastUpdateNewExecCount = new HashMap<>();

        private ObjectHistory(HashMap<String, ArrayList<ItemWithTime<TIME, Object>>> objectHistory) {
            this.objectHistory = objectHistory;
        }

        @Override
        public boolean existsField(String fieldName) {
            ArrayList<ItemWithTime<TIME, Object>> fieldHistory = objectHistory.get(fieldName);
            return fieldHistory != null && !fieldHistory.isEmpty()
                    && fieldHistory.get(0).time().compareTo(currentTime) <= 0;
        }

        @Override
        public Object getObjectFieldValue(String fieldName) {
            ArrayList<ItemWithTime<TIME, Object>> fieldHistory = objectHistory.get(fieldName);
            if (fieldHistory == null) {
                System.out.println("Fields: " + objectHistory.keySet());
                System.out.println(fieldName);
                return null;
            }
            int i = ItemWithTime.binarySearchApply(fieldHistory, currentTime);
            if (i < 0) {
                System.out.println("History: " + fieldHistory);
                System.out.println(currentTime);
                return null;
            }
            return revertObject(fieldHistory.get(i).item());
        }

        @Override
        public boolean canUseCache(String member) {
            Integer last = lastUpdateNewExecCount.put(member, newExecCount);
            System.out.println("canUseCache: " + last);
            System.out.println(newExecCount);
            return last != null && last == newExecCount;
        }

        @Override
        public void onWrite(String member) {
            System.out.println("onWrite: " + currentTime + " / " + newExecCount);
            lastUpdateNewExecCount.put(member, newExecCount);
        }
    }

    private final class LocalVarOperatorHolder {
        private ScopeInfo<TIME>[] stack;
        private int pointer = 1;

        public LocalVarOperatorHolder(int executionParamLen) {
            @SuppressWarnings("unchecked")
            final ScopeInfo<TIME>[] stack = new ScopeInfo[32];
            stack[0] = new ScopeInfo<>(executionParamLen, CallContext.ExecutionBase.INSTANCE);
            this.stack = stack;
        }

        public void push(int paramLen, CallContext.ContextBase ctx) {
            ScopeInfo<TIME>[] stack = this.stack;
            final int currentLen = stack.length;
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

        public ExecutionHistory.LocalVarOperator<TIME> peek() {
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
    }

    private static final class ScopeInfo<TIME extends Time<TIME>> {
        final int paramLen;
        final CallContext.ContextBase cc;
        ExecutionHistory.LocalVarOperator<TIME> op = null;

        public ScopeInfo(int paramLen, CallContext.ContextBase cc) {
            this.paramLen = paramLen;
            this.cc = cc;
        }
    }

    private enum ShouldReExecuteResult {
        USE_CACHE, RE_EXECUTE, NEW_EXECUTE
    }

    private record FunctionReference(TruffleString functionName) {}
}
