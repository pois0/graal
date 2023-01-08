package com.oracle.truffle.sl.runtime.cache;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory;
import com.oracle.truffle.api.instrumentation.StackListener;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.nodes.controlflow.SLBreakException;
import com.oracle.truffle.sl.nodes.controlflow.SLContinueException;
import com.oracle.truffle.sl.nodes.controlflow.SLReturnException;
import com.oracle.truffle.sl.nodes.util.SLUnboxNode;
import com.oracle.truffle.sl.runtime.SLBigNumber;
import com.oracle.truffle.sl.runtime.SLFunction;
import com.oracle.truffle.sl.runtime.SLFunctionRegistry;
import com.oracle.truffle.sl.runtime.SLNull;
import com.oracle.truffle.sl.runtime.SLObject;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;
import org.graalvm.collections.Pair;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public final class ExecutionHistoryOperator {
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);
    private static ExecutionHistory rootHistory = new ExecutionHistory();
    private static boolean isInitialExecution = true;
    private static boolean rotate = true;

    private final SLLanguage language;
    private final SLFunctionRegistry functionRegistry;

    private Time currentTime = Time.zero();
    private boolean isInExec = false;
    private ExecutionContext lastCalcCtx = null;
    private ExecutionHistory currentHistory;
    private final ArrayDeque<HashSet<String>> localVarFlagStack = new ArrayDeque<>();
    private final ArrayDeque<boolean[]> parameterFlagStack = new ArrayDeque<>();
    private final HashMap<Time, HashSet<Object>> objectFieldFlags = new HashMap<>();
    private CallContext currentContext = CallContext.ExecutionBase.INSTANCE;
    private HashMap<Time, WeakReference<SLObject>> ctxToObj = new HashMap<>();
    private WeakHashMap<SLObject, Time> objToCtx = new WeakHashMap<>();
    private final LocalVarOperatorHolder localVarOperatorHolder;

    private final StackListener stackListener = new StackListener() {
        @Override
        public void onObjectSet(FrameSlot slot, Object value) {
            onUpdateLocalVar(slot.getIdentifier().toString(), replaceReference(value));
        }

        @Override
        public void onBooleanSet(FrameSlot slot, boolean value) {
            onUpdateLocalVar(slot.getIdentifier().toString(), value);
        }

        @Override
        public void onByteSet(FrameSlot slot, byte value) {
            onUpdateLocalVar(slot.getIdentifier().toString(), value);
        }

        @Override
        public void onIntSet(FrameSlot slot, int value) {
            onUpdateLocalVar(slot.getIdentifier().toString(), value);
        }

        @Override
        public void onLongSet(FrameSlot slot, long value) {
            onUpdateLocalVar(slot.getIdentifier().toString(), value);
        }

        @Override
        public void onFloatSet(FrameSlot slot, float value) {
            onUpdateLocalVar(slot.getIdentifier().toString(), value);
        }

        @Override
        public void onDoubleSet(FrameSlot slot, double value) {
            onUpdateLocalVar(slot.getIdentifier().toString(), value);
        }
    };

    private final ExecutionEventNodeFactory tickFactory = context -> {
        SLStatementNode instrumentedNode = (SLStatementNode) context.getInstrumentedNode();
        if (instrumentedNode != null && instrumentedNode.getNodeIdentifier() != null) {
            return new TickNode(instrumentedNode.getNodeIdentifier());
        }
        return null;
    };

    public ExecutionHistoryOperator(SLLanguage language, SLFunctionRegistry functionRegistry) {
        this.language = language;
        this.functionRegistry = functionRegistry;
        if (rotate) {
            System.out.println("reset!");
            rootHistory = new ExecutionHistory();
            isInitialExecution = true;
        }
        rotate = !rotate;
        this.currentHistory = rootHistory;

        parameterFlagStack.push(new boolean[0]);
        localVarFlagStack.push(new HashSet<>());
        localVarOperatorHolder = new LocalVarOperatorHolder(0);
    }

    public StackListener getStackListener() {
        return stackListener;
    }

    public ExecutionEventNodeFactory getTickFactory() {
        return tickFactory;
    }

    public boolean checkInitialExecution() {
        final boolean isInitialExecution = ExecutionHistoryOperator.isInitialExecution;
        if (isInitialExecution) {
            ExecutionHistoryOperator.isInitialExecution = false;
            isInExec = true;
        }
        return isInitialExecution;
    }

    public boolean checkContainsNewNodeInFunctionCalls(NodeIdentifier identifier) {
        final ExecutionHistory.TimeInfo tp = currentHistory.getTime(lastCalcCtx);
        assert tp != null;
        final Time from = tp.getEnd();
        final ExecutionHistory.TimeInfo tp2 = currentHistory.getTime(getExecutionContext(identifier));
        assert tp2 != null;
        final Time end = tp2.getEnd();
        for (ItemWithTime<Pair<CallContext.ContextBase, String>> entry : currentHistory.getFunctionEnters(from, end)) {
            if (functionRegistry.containNewNode(entry.getItem().getRight())) return true;
        }
        return false;
    }

    @CompilerDirectives.TruffleBoundary
    public void onReadArgument(int argIndex) {
        localVarOperatorHolder.onReadParam(currentTime, argIndex);
    }

    @CompilerDirectives.TruffleBoundary
    public void onReadLocalVariable(Object variableName) {
        localVarOperatorHolder.onReadVariable(currentTime, (String) variableName);
    }

    @CompilerDirectives.TruffleBoundary
    public void onReadObjectField(Object object, Object field) {
        currentHistory.onReadObjectField(currentTime, objToCtx.get((SLObject) object), field);
    }

    @CompilerDirectives.TruffleBoundary
    public void onEnterFunction(NodeIdentifier identifier, String funcName, int paramLen, boolean isCalc) {
        final ExecutionHistory currentHistory = this.currentHistory;
        CallContext.FunctionCall currentStack = new CallContext.FunctionCall(this.currentContext, identifier);
        this.currentContext = currentStack;
        localVarFlagStack.push(new HashSet<>());
        localVarOperatorHolder.push(paramLen, currentStack);
        final Time time = isCalc ? currentHistory.getTime(lastCalcCtx).getEnd() : currentTime;
        currentHistory.onEnterFunction(time, funcName, currentStack);
    }

    @CompilerDirectives.TruffleBoundary
    public void pushArgumentFlags(boolean[] flags) {
        parameterFlagStack.push(flags);
    }

    @CompilerDirectives.TruffleBoundary
    public void onExitFunction(NodeIdentifier identifier) {
        CallContext elem = currentContext;
        assert elem instanceof CallContext.FunctionCall && elem.getNodeIdentifier() == identifier;
        currentContext = elem.getRoot();
        localVarOperatorHolder.pop();
        localVarFlagStack.pop();
    }

    @CompilerDirectives.TruffleBoundary
    public void popArgumentFlags() {
        parameterFlagStack.pop();
    }

    @CompilerDirectives.TruffleBoundary
    public void onEnterLoop(NodeIdentifier identifier) {
        this.currentContext = new CallContext.Loop(this.currentContext, identifier);
    }

    @CompilerDirectives.TruffleBoundary
    public void onGotoNextIteration(NodeIdentifier identifier) {
        CallContext elem = currentContext;
        assert elem instanceof CallContext.Loop && elem.getNodeIdentifier() == identifier;
        currentContext = ((CallContext.Loop) elem).increment();
    }

    @CompilerDirectives.TruffleBoundary
    public void onExitLoop(NodeIdentifier identifier) {
        CallContext elem = currentContext;
        assert elem instanceof CallContext.Loop && elem.getNodeIdentifier() == identifier;
        currentContext = elem.getRoot();
    }

    @CompilerDirectives.TruffleBoundary
    public void onGenerateObject(SLObject object) {
        objToCtx.put(object, currentTime);
        ctxToObj.put(currentTime, new WeakReference<>(object));
        currentHistory.onCreateObject(currentTime);
    }

    @CompilerDirectives.TruffleBoundary
    public void onObjectUpdated(Object object, String fldName, Object newValue) {
        final Time objGenTime = objToCtx.get((SLObject) object);
        currentHistory.onUpdateObjectWithHash(currentTime, objGenTime, fldName, replaceReference(newValue));
        objectFieldFlags.computeIfAbsent(objGenTime, it -> new HashSet<>())
                .add(fldName);
    }

    public ShouldReExecuteResult shouldReExecute(SLStatementNode node) {
        if (node.isNewNode()) return ShouldReExecuteResult.NEW_EXECUTE;
        if (node.hasNewNode()) return ShouldReExecuteResult.RE_EXECUTE;

        final ExecutionHistory currentHistory = this.currentHistory;
        NodeIdentifier nodeIdentifier = node.getNodeIdentifier();
        ExecutionContext execCtx = getExecutionContext(nodeIdentifier);
        ExecutionHistory.TimeInfo tp = currentHistory.getTime(execCtx);
        if (tp == null) return ShouldReExecuteResult.NEW_EXECUTE;

        if (checkFromList(currentHistory.getFunctionEnters(tp.getStart(), tp.getEnd()), it -> functionRegistry.containNewNode(it.getRight()))) {
            return ShouldReExecuteResult.RE_EXECUTE;
        }

        ExecutionHistory.LocalVarOperator op = localVarOperatorHolder.peek();

        final HashSet<String> localVarFlags = localVarFlagStack.peek();
        assert localVarFlags != null;
        for (String varName : localVarFlags) {
            ArrayList<Time> readVarHistory = op.getReadVar(varName);
            if (readVarHistory == null || readVarHistory.isEmpty()) continue;
            final int start = Time.binarySearchWhereInsertTo(readVarHistory, tp.getStart());
            final int end = Time.binarySearchNext(readVarHistory, tp.getEnd());
            if (start != end) return ShouldReExecuteResult.RE_EXECUTE;
        }

        final boolean[] paramFlags = parameterFlagStack.peek();
        assert paramFlags != null;
        for (int i = 0; i < paramFlags.length; i++) {
            if (!paramFlags[i]) continue;
            ArrayList<Time> readParamHistory = op.getReadParam(i);
            if (readParamHistory.isEmpty()) continue;
            final int start = Time.binarySearchWhereInsertTo(readParamHistory, tp.getStart());
            final int end = Time.binarySearchNext(readParamHistory, tp.getEnd());
            if (start != end) return ShouldReExecuteResult.RE_EXECUTE;
        }

        if (!objectFieldFlags.isEmpty()) {
            final boolean check = checkFromList(currentHistory.getReadOperations(tp.getStart(), tp.getEnd()), content -> {
                HashSet<Object> fields = objectFieldFlags.get(content.getObjGenCtx());
                return fields != null && fields.contains(content.getFieldName());
            });
            if (check) {
                return ShouldReExecuteResult.RE_EXECUTE;
            }
        }

        return ShouldReExecuteResult.USE_CACHE;
    }

    private final static int STREAM_THRESHOLD = 10_000;
    private <T> boolean checkFromList(List<ItemWithTime<T>> list, Predicate<T> pred) {
        if (list.size() > STREAM_THRESHOLD) {
            return list.parallelStream().map(ItemWithTime::getItem).anyMatch(pred);
        } else {
            for (ItemWithTime<T> entry : list) {
                if (pred.test(entry.getItem())) return true;
            }
            return false;
        }
    }

    public Object getReturnedValueOrThrow(NodeIdentifier identifier) {
        return getReturnedValueOrThrow(getExecutionContext(identifier));
    }

    public Object getReturnedValueOrThrow(ExecutionContext execCtx) {
//        System.out.println("Skipped " + execCtx);
        return revertObject(currentHistory.getReturnedValueOrThrow(execCtx));
    }

    public Object getVariableValue(Object varName, NodeIdentifier identifier) {
        final Time time = currentHistory.getTime(getExecutionContext(identifier)).getEnd();
        //noinspection DataFlowIssue
        final ArrayList<ItemWithTime<Object>> varHistory = currentHistory.getLocalHistory(currentContext.getBase()).get((String) varName);
        return varHistory.get(ItemWithTime.binarySearchApply(varHistory, time)).getItem();
    }

    public Object getFieldValue(Object obj, String fieldName, NodeIdentifier identifier) {
        final Time objGenTime = objToCtx.get((SLObject) obj);
        final Time time = currentHistory.getTime(getExecutionContext(identifier)).getEnd();
        final HashMap<String, ArrayList<ItemWithTime<Object>>> objectHistory = currentHistory.getObjectHistory(objGenTime);
        final ArrayList<ItemWithTime<Object>> fieldHistory = objectHistory.get(fieldName);
        final Object value = fieldHistory.get(ItemWithTime.binarySearchApply(fieldHistory, time)).getItem();
        return revertObject(value);
    }

    public void rewriteLocalVariable(FrameSlot slot, Object value, NodeIdentifier identifier) {
        final String varName = (String) slot.getIdentifier();
        currentHistory.rewriteLocalVariable(getExecutionContext(identifier), varName, replaceReference(value));
        //noinspection DataFlowIssue
        localVarFlagStack.peek().add(varName);
    }

    public void rewriteObjectField(Object receiver, String fieldName, Object value, NodeIdentifier identifier) {
        final Time objGenTime = objToCtx.get((SLObject) receiver);
        currentHistory.rewriteObjectField(getExecutionContext(identifier), objGenTime, fieldName, replaceReference(value));
        objectFieldFlags.computeIfAbsent(objGenTime, it -> new HashSet<>())
                .add(fieldName);
    }

    public void deleteHistory(NodeIdentifier identifier) {
        currentHistory.deleteRecords(getExecutionContext(identifier));
    }

    public Object calcGeneric(VirtualFrame frame, SLExpressionNode node) {
        if (node instanceof SLUnboxNode) return calcGeneric(frame, ((SLUnboxNode) node).getChild());
        try {
            switch (shouldReExecute(node)) {
                case USE_CACHE:
                    return getReturnedValueOrThrow(node.getNodeIdentifier());
                case RE_EXECUTE:
                    return reExecuteGeneric(node.getNodeIdentifier(), frame, node::calcGenericInner);
                case NEW_EXECUTE:
                    return newExecutionGeneric(node.getNodeIdentifier(), frame, node::executeGeneric);
            }
        } catch (SLReturnException | SLBreakException | SLContinueException e) {
            currentHistory.deleteRecords(lastCalcCtx, getExecutionContext(node.getNodeIdentifier()));
            throw e;
        } finally {
            finishCalc(node.getNodeIdentifier());
        }

        throw new RuntimeException("Never reach here");
    }

    public Pair<Object, Boolean> calcGenericParameter(VirtualFrame frame, SLExpressionNode node) {
        if (node instanceof SLUnboxNode) return calcGenericParameter(frame, ((SLUnboxNode) node).getChild());
        try {
            switch (shouldReExecute(node)) {
                case USE_CACHE:
                    return Pair.create(getReturnedValueOrThrow(node.getNodeIdentifier()), false);
                case RE_EXECUTE:
                    return Pair.create(reExecuteGeneric(node.getNodeIdentifier(), frame, node::calcGenericInner), true);
                case NEW_EXECUTE:
                    return Pair.create(newExecutionGeneric(node.getNodeIdentifier(), frame, node::executeGeneric), true);
            }
        } finally {
            finishCalc(node.getNodeIdentifier());
        }

        throw new RuntimeException("Never reach here");
    }

    public boolean calcBoolean(VirtualFrame frame, Node currentNode, SLExpressionNode node) {
        if (node instanceof SLUnboxNode) return calcBoolean(frame, node, ((SLUnboxNode) node).getChild());
        try {
            switch (shouldReExecute(node)) {
            case USE_CACHE:
                final Object obj = getReturnedValueOrThrow(node.getNodeIdentifier());
                if (obj instanceof Boolean) {
                    return (boolean) obj;
                } else {
                    throw new UnexpectedResultException(obj);
                }
            case RE_EXECUTE:
                return reExecuteBoolean(node.getNodeIdentifier(), frame, node::calcBooleanInner);
            case NEW_EXECUTE:
                return newExecutionBoolean(node.getNodeIdentifier(), frame, node::executeBoolean);
            }
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(currentNode, ex.getResult());
        } finally {
            finishCalc(node.getNodeIdentifier());
        }

        throw new RuntimeException("Never reach here");
    }

    public long calcLong(VirtualFrame frame, SLExpressionNode currentNode, SLExpressionNode node) {
        if (node instanceof SLUnboxNode) return calcLong(frame, node, ((SLUnboxNode) node).getChild());
        try {
            switch (shouldReExecute(node)) {
            case USE_CACHE:
                final Object obj = getReturnedValueOrThrow(node.getNodeIdentifier());
                if (obj instanceof Long) {
                    return (long) obj;
                } else {
                    throw new UnexpectedResultException(obj);
                }
            case RE_EXECUTE:
                return reExecuteLong(node.getNodeIdentifier(), frame, node::calcLongInner);
            case NEW_EXECUTE:
                return newExecutionLong(node.getNodeIdentifier(), frame, node::executeLong);
            }
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(currentNode, ex.getResult());
        } finally {
            finishCalc(node.getNodeIdentifier());
        }

        throw new RuntimeException("Never reach here");
    }

    public void calcVoid(VirtualFrame frame, SLStatementNode node) {
        try {
            switch (shouldReExecute(node)) {
            case USE_CACHE:
                getReturnedValueOrThrow(node.getNodeIdentifier());
                return;
            case RE_EXECUTE:
                reExecuteVoid(node.getNodeIdentifier(), frame, node::calcVoidInner);
                return;
            case NEW_EXECUTE:
                newExecutionVoid(node.getNodeIdentifier(), frame, node::executeVoid);
                return;
            }
        } catch (SLReturnException | SLBreakException | SLContinueException e) {
            currentHistory.deleteRecords(lastCalcCtx, getExecutionContext(node.getNodeIdentifier()));
            throw e;
        } finally {
            finishCalc(node.getNodeIdentifier());
        }

        throw new RuntimeException("Never reach here");
    }

    @CompilerDirectives.TruffleBoundary
    private void onUpdateLocalVar(String identifier, Object value) {
        localVarOperatorHolder.onUpdateVariable(currentTime, identifier, value);
        //noinspection DataFlowIssue
        localVarFlagStack.peek().add(identifier);
    }

    public void startNewExecution(VirtualFrame frame, NodeIdentifier identifier) {
        if (isInExec) return;
        isInExec = true;
        final ExecutionHistory history = currentHistory;
        final ExecutionHistory.TimeInfo time = history.getTime(getExecutionContext(identifier));
        if (time != null) history.deleteRecords(time.getStart(), time.getEnd());
        final ExecutionContext lastCalcCtx = this.lastCalcCtx;
        if (lastCalcCtx != null) {
            final ExecutionHistory.TimeInfo tp = history.getTime(lastCalcCtx);
            if (tp == null) {
                currentTime.inc();
            } else {
                currentTime = history.getNextTime(tp.getEnd());
            }
        }
        constructFrameAndObjects(currentTime, frame);
        currentHistory = new ExecutionHistory();
        localVarOperatorHolder.duplicate();
    }

    public void endNewExecution() {
        currentHistory = rootHistory.merge(currentHistory);
        localVarOperatorHolder.pop();
        isInExec = false;
    }

    public <T> T newExecutionGeneric(NodeIdentifier identifier, VirtualFrame frame, Task<T> task) {
        final TickNode tickNode = new TickNode(identifier);
        startNewExecution(frame, identifier);
        tickNode.onEnter(frame);
        final T result;
        try {
            result = task.invoke(frame);
        } catch (Throwable e) {
            tickNode.onReturnExceptional(frame, e);
            endNewExecution();
            throw e;
        }

        try {
            tickNode.onReturnValue(frame, result);
            return result;
        } finally {
            endNewExecution();
        }
    }

    public boolean newExecutionBoolean(NodeIdentifier identifier, VirtualFrame frame, BooleanTask task) throws UnexpectedResultException {
        final TickNode tickNode = new TickNode(identifier);
        startNewExecution(frame, identifier);
        tickNode.onEnter(frame);
        final boolean result;
        try {
            result = task.invokeBoolean(frame);
        } catch (Throwable e) {
            tickNode.onReturnExceptional(frame, e);
            endNewExecution();
            throw e;
        }

        try {
            tickNode.onReturnValue(frame, result);
            return result;
        } finally {
            endNewExecution();
        }
    }

    public long newExecutionLong(NodeIdentifier identifier, VirtualFrame frame, LongTask task) throws UnexpectedResultException {
        final TickNode tickNode = new TickNode(identifier);
        startNewExecution(frame, identifier);
        tickNode.onEnter(frame);
        final long result;
        try {
            result = task.invokeLong(frame);
        } catch (Throwable e) {
            tickNode.onReturnExceptional(frame, e);
            endNewExecution();
            throw e;
        }

        try {
            tickNode.onReturnValue(frame, result);
            return result;
        } finally {
            endNewExecution();
        }
    }

    public void newExecutionVoid(NodeIdentifier identifier, VirtualFrame frame, VoidTask task) {
        final TickNode tickNode = new TickNode(identifier);
        startNewExecution(frame, identifier);
        tickNode.onEnter(frame);
        try {
            task.invokeVoid(frame);
        } catch (Throwable e) {
            tickNode.onReturnExceptional(frame, e);
            endNewExecution();
            throw e;
        }

        try {
            tickNode.onReturnValue(frame, null);
        } finally {
            endNewExecution();
        }
    }

    public void reExecuteVoid(NodeIdentifier identifier, VirtualFrame frame, VoidTask task) {
        try {
            task.invokeVoid(frame);
        } catch (Throwable e) {
            currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), e);
            throw e;
        }
        currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), null);
    }

    public <T> T reExecuteGeneric(NodeIdentifier identifier, VirtualFrame frame, Task<T> task) {
        T value;
        try {
            value = task.invoke(frame);
        } catch (Throwable e) {
            currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), e);
            throw e;
        }
        currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), value);
        return value;
    }

    public boolean reExecuteBoolean(NodeIdentifier identifier, VirtualFrame frame, BooleanTask task) throws UnexpectedResultException {
        boolean value;
        try {
            value = task.invokeBoolean(frame);
        } catch (Throwable e) {
            currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), e);
            throw e;
        }
        currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), value);
        return value;
    }

    public long reExecuteLong(NodeIdentifier identifier, VirtualFrame frame, LongTask task) throws UnexpectedResultException {
        long value;
        try {
            value = task.invokeLong(frame);
        } catch (Throwable e) {
            currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), e);
            throw e;
        }
        currentHistory.replaceReturnedValueOrException(getExecutionContext(identifier), value);
        return value;
    }

    public void finishCalc(NodeIdentifier identifier) {
        lastCalcCtx = getExecutionContext(identifier);
    }

    private void constructFrameAndObjects(Time time, VirtualFrame frame) {
        ExecutionHistory history = currentHistory;
        HashMap<String, ArrayList<ItemWithTime<Object>>> local = history.getLocalHistory(currentContext.getBase());
        if (local == null) return;
        FrameDescriptor descriptor = frame.getFrameDescriptor();

        final HashMap<Time, WeakReference<SLObject>> ctxToObj = new HashMap<>(this.ctxToObj);
        final WeakHashMap<SLObject, Time> objToCtx = new WeakHashMap<>(this.objToCtx);

        for (Map.Entry<SLObject, Time> entry : this.objToCtx.entrySet()) {
            final SLObject obj = entry.getKey();
            final Time objGenCtx = entry.getValue();
            constructObjects(time, objGenCtx, obj, history, ctxToObj, objToCtx);
        }

        for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> entry : local.entrySet()) {
            String name = entry.getKey();
            ArrayList<ItemWithTime<Object>> list = entry.getValue();

            int i = ItemWithTime.binarySearchApply(list, time);
            if (i < 0) continue;
            Object value = list.get(i).getItem();
            FrameSlot slot = descriptor.findFrameSlot(name);
            if (value instanceof Long) {
                frame.setLong(slot, (Long) value);
            } else if (value instanceof Boolean) {
                frame.setBoolean(slot, (Boolean) value);
            } else if (value instanceof SLBigNumber || value instanceof String || value == SLNull.SINGLETON) {
                frame.setObject(slot, value);
            } else if (value instanceof ExecutionHistory.ObjectReference) {
                Object obj = constructObjects(time, ((ExecutionHistory.ObjectReference) value).getObjGenCtx(), history, ctxToObj, objToCtx);
                frame.setObject(slot, obj);
            } else if (value instanceof FunctionReference) {
                frame.setObject(slot, getFunction(((FunctionReference) value).getFunctionName()));
            } else {
                throw new RuntimeException("Unknown value type: " + value.getClass().getName());
            }
        }

        this.ctxToObj = ctxToObj;
        this.objToCtx = objToCtx;
    }

    private SLObject constructObjects(
            Time time,
            Time objGenTime,
            SLObject newObject,
            ExecutionHistory history,
            HashMap<Time, WeakReference<SLObject>> ctxToObj,
            WeakHashMap<SLObject, Time> objToCtx
    ) {
        HashMap<String, ArrayList<ItemWithTime<Object>>> currentObjectHistory = history.getObjectHistory(objGenTime);
        if (currentObjectHistory != null) {
            InteropLibrary library = INTEROP_LIBRARY_.create(newObject);
            for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> entry : currentObjectHistory.entrySet()) {
                String name = entry.getKey();
                ArrayList<ItemWithTime<Object>> list = entry.getValue();

                int i = ItemWithTime.binarySearchApply(list, time);
                if (i < 0) continue;
                Object value = list.get(i).getItem();

                try {
                    if (value instanceof Long
                            || value instanceof SLBigNumber
                            || value instanceof Boolean
                            || value instanceof String
                            || value == SLNull.SINGLETON) {
                        library.writeMember(newObject, name, value);
                    } else if (value instanceof ExecutionHistory.ObjectReference) {
                        Time valueGenTime = ((ExecutionHistory.ObjectReference) value).getObjGenCtx();
                        WeakReference<SLObject> objRef = ctxToObj.get(valueGenTime);
                        Object newValue;
                        if (objRef == null) {
                            newValue = constructObjects(time, valueGenTime, history, ctxToObj, objToCtx);
                        } else {
                            newValue = objRef.get();
                        }
                        library.writeMember(newObject, name, newValue);
                    } else if (value instanceof FunctionReference) {
                        library.writeMember(newObject, name, getFunction(((FunctionReference) value).getFunctionName()));
                    } else {
                        throw new RuntimeException("Unknown value type: " + value.getClass().getName());
                    }
                } catch (UnsupportedMessageException | UnknownIdentifierException | UnsupportedTypeException e) {
                    throw SLUndefinedNameException.undefinedProperty(null, name);
                }
            }
        }

        return newObject;
    }

    private SLObject constructObjects(
            Time time,
            Time objGenTime,
            ExecutionHistory history,
            HashMap<Time,WeakReference<SLObject>> ctxToObj,
            WeakHashMap<SLObject, Time> objToCtx
    ) {
        SLObject newObject = language.justCreateObject();
        ctxToObj.put(objGenTime, new WeakReference<>(newObject));
        objToCtx.put(newObject, objGenTime);

        return constructObjects(time, objGenTime, newObject, history, ctxToObj, objToCtx);
    }

    private Time getAndIncrementTime() {
        final Time tmp = currentTime;
        currentTime = tmp.inc();
        return tmp;
    }

    public ExecutionContext getExecutionContext(NodeIdentifier identifier) {
        return new ExecutionContext(currentContext, identifier);
    }

    private Object revertObject(Object value) {
        if (value instanceof Long
                || value instanceof Boolean
                || value instanceof SLBigNumber
                || value instanceof String
                || value == SLNull.SINGLETON) {
            return value;
        } else if (value instanceof ExecutionHistory.ObjectReference) {
            return ctxToObj.computeIfAbsent(
                            ((ExecutionHistory.ObjectReference) value).getObjGenCtx(),
                            objGenCtx -> {
                                final SLObject newObject = language.justCreateObject();
                                objToCtx.put(newObject, objGenCtx);
                                return new WeakReference<>(newObject);
                            }
                    )
                    .get();
        } else if (value instanceof FunctionReference) {
            final FunctionReference funcRef = (FunctionReference) value;
            final SLFunction function = getFunction(funcRef.getFunctionName());
            assert function != null : "Unknown function: " + funcRef.functionName;
            return function;
        } else if (value == null) {
            return null;
        } else {
            throw new RuntimeException("Invalid type: " + value.getClass().getName());
        }
    }

    private SLFunction getFunction(String functionName) {
        return functionRegistry.getFunction(functionName);
    }

    private Object replaceReference(Object value) {
        Object saveNewValue;
        if (value == null
                || value instanceof Long
                || value instanceof Boolean
                || value instanceof String
                || value instanceof SLBigNumber
                || value == SLNull.SINGLETON
        ) {
            saveNewValue = value;
        } else if (value instanceof SLFunction) {
            saveNewValue = new FunctionReference(((SLFunction) value).getName());
        } else if (value instanceof SLObject) {
            saveNewValue = new ExecutionHistory.ObjectReference(objToCtx.get((SLObject) value));
        } else {
            throw new RuntimeException("Unavailable object: " + value.getClass().getName());
        }

        return saveNewValue;
    }

    private final static class FunctionReference {
        private final String functionName;

        public FunctionReference(String functionName) {
            this.functionName = functionName;
        }

        public String getFunctionName() {
            return functionName;
        }
    }

    private final class TickNode extends ExecutionEventNode {
        private final ArrayDeque<Time> startTime = new ArrayDeque<>();
        private final NodeIdentifier identifier;

        private TickNode(NodeIdentifier identifier) {
            this.identifier = identifier;
        }

        @Override
        protected void onEnter(VirtualFrame frame) {
            startTime.push(currentTime);
        }

        @Override
        protected void onReturnValue(VirtualFrame frame, Object result) {
            currentHistory.onReturnValue(startTime.pop(), getAndIncrementTime(), getExecutionContext(identifier), replaceReference(result));
//            System.out.println("time: " + currentTime + "/ identifier: " + identifier + "/ object: " + result);
        }

        @Override
        protected void onReturnExceptional(VirtualFrame frame, Throwable exception) {
            if (exception instanceof ControlFlowException) {
                currentHistory.onReturnExceptional(startTime.pop(), getAndIncrementTime(), getExecutionContext(identifier), (RuntimeException) exception);
            }
        }
    }

    private final class LocalVarOperatorHolder {
        private ScopeInfo[] stack;
        private int pointer = 1;

        public LocalVarOperatorHolder(int executionParamLen) {
            final ScopeInfo[] stack = new ScopeInfo[32];
            stack[0] = new ScopeInfo(executionParamLen, CallContext.ExecutionBase.INSTANCE);
            this.stack = stack;
        }

        public void push(int paramLen, CallContext.ContextBase ctx) {
            ScopeInfo[] stack = this.stack;
            final int currentLen = stack.length;
            if (pointer == currentLen) this.stack = stack = Arrays.copyOf(stack, currentLen + (currentLen >> 1));
            stack[pointer++] = new ScopeInfo(paramLen, ctx);
        }

        public void duplicate() {
            ScopeInfo info = stack[pointer - 1];
            push(info.paramLen, info.cc);
        }

        public void pop() {
            assert pointer >= 0;
            stack[pointer--] = null;
        }

        public ExecutionHistory.LocalVarOperator peek() {
            ScopeInfo info = stack[pointer - 1];
            if (info.op != null) return info.op;
            return info.op = currentHistory.getLocalVarOperator(info.cc, info.paramLen);
        }

        public void onUpdateVariable(Time time, String varName, Object newValue) {
            peek().onUpdateVariable(time, varName, newValue);
        }

        public void onReadVariable(Time time, String varName) {
            peek().onReadVariable(time, varName);
        }

        public void onReadParam(Time time, int paramIndex) {
            peek().onReadParam(time, paramIndex);
        }
    }

    private static final class ScopeInfo {
        final int paramLen;
        final CallContext.ContextBase cc;
        ExecutionHistory.LocalVarOperator op = null;

        public ScopeInfo(int paramLen, CallContext.ContextBase cc) {
            this.paramLen = paramLen;
            this.cc = cc;
        }
    }

    public interface Task<T> {
        T invoke(VirtualFrame frame);
    }

    public interface BooleanTask {
        boolean invokeBoolean(VirtualFrame frame) throws UnexpectedResultException;
    }

    public interface LongTask {
        long invokeLong(VirtualFrame frame) throws UnexpectedResultException;
    }

    public interface VoidTask {
        void invokeVoid(VirtualFrame frame);
    }

    private enum ShouldReExecuteResult {
        USE_CACHE, RE_EXECUTE, NEW_EXECUTE
    }
}
