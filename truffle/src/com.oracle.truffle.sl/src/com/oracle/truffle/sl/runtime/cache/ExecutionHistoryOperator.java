package com.oracle.truffle.sl.runtime.cache;

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
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.SLBigNumber;
import com.oracle.truffle.sl.runtime.SLFunction;
import com.oracle.truffle.sl.runtime.SLFunctionRegistry;
import com.oracle.truffle.sl.runtime.SLObject;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;
import org.graalvm.collections.Pair;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public final class ExecutionHistoryOperator {
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);
    private static ExecutionHistory rootHistory = new ExecutionHistory();
    private static boolean isInitialExecution = true;
    private static boolean rotate = true;

    private final SLLanguage language;
    private final SLFunctionRegistry functionRegistry;

    private Time currentTime = Time.zero().subdivide();
    private boolean isInExec = false;
    private ExecutionContext lastCalcCtx = null;
    private ExecutionHistory currentHistory;
    private final ArrayDeque<HashSet<Object>> localVarFlagStack = new ArrayDeque<>();
    private final ArrayDeque<boolean[]> parameterFlagStack = new ArrayDeque<>();
    private final HashMap<ExecutionContext, HashSet<Object>> objectFieldFlags = new HashMap<>();
    private final ArrayDeque<CallContextElement> currentStack = new ArrayDeque<>();
    private HashMap<ExecutionContext, WeakReference<Object>> ctxToObj = new HashMap<>(); // hash -> weak reference of object
    private WeakHashMap<Object, ExecutionContext> objToCtx = new WeakHashMap<>();

    // Caches
    private CallContextElement[] callContextCache = null;
    private CallContextElement.FunctionCallArray callArrayCache = null;
    private HashSet<Object> localVarFlagCache = null;

    private final StackListener stackListener = new StackListener() {
        @Override
        public void onObjectSet(FrameSlot slot, Object value) {
            currentHistory.onUpdateLocalVariable(currentTime, getFunctionCallArray(), (String) slot.getIdentifier(), replaceReference(value));
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onBooleanSet(FrameSlot slot, boolean value) {
            currentHistory.onUpdateLocalVariable(currentTime, getFunctionCallArray(), (String) slot.getIdentifier(), value);
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onByteSet(FrameSlot slot, byte value) {
            currentHistory.onUpdateLocalVariable(currentTime, getFunctionCallArray(), (String) slot.getIdentifier(), value);
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onIntSet(FrameSlot slot, int value) {
            currentHistory.onUpdateLocalVariable(currentTime, getFunctionCallArray(), (String) slot.getIdentifier(), value);
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onLongSet(FrameSlot slot, long value) {
            currentHistory.onUpdateLocalVariable(currentTime, getFunctionCallArray(), (String) slot.getIdentifier(), value);
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onFloatSet(FrameSlot slot, float value) {
            currentHistory.onUpdateLocalVariable(currentTime, getFunctionCallArray(), (String) slot.getIdentifier(), value);
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onDoubleSet(FrameSlot slot, double value) {
            currentHistory.onUpdateLocalVariable(currentTime, getFunctionCallArray(), (String) slot.getIdentifier(), value);
            getLocalVarFlagCache().add(slot.getIdentifier());
        }
    };

    private final ExecutionEventNodeFactory tickFactory = context -> {
        SLStatementNode instrumentedNode = (SLStatementNode) context.getInstrumentedNode();
        if (instrumentedNode != null) {
            final SourceSection ss = instrumentedNode.getSourceSection();
            if (ss != null && ss.isAvailable()) return new TickNode(instrumentedNode.getNodeIdentifier());
        }
        return null;
    };

    public ExecutionHistoryOperator(SLLanguage language, SLFunctionRegistry functionRegistry) {
        this.language = language;
        this.functionRegistry = functionRegistry;
        if (rotate) {
            rootHistory = new ExecutionHistory();
        }
        rotate = !rotate;
        this.currentHistory = rootHistory;

        localVarFlagStack.add(new HashSet<>());
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
        final ExecutionHistory.TimePair tp = currentHistory.getTime(lastCalcCtx);
        assert tp != null;
        final Time from = tp.getEnd();
        final ExecutionHistory.TimePair tp2 = currentHistory.getTime(getExecutionContext(identifier));
        assert tp2 != null;
        final Time end = tp2.getEnd();
        final Iterator<ItemWithTime<String>> iter = currentHistory.getFunctionEnters(from, end);
        while (iter.hasNext()) {
            final String funcName = iter.next().getItem();
            if (functionRegistry.containNewNode(funcName)) return true;
        }
        return false;
    }

    public void onReadArgument(int argIndex) {
        currentHistory.onReadArgument(currentTime, getFunctionCallArray(), argIndex);
    }

    public void onReadLocalVariable(Object variableName) {
        currentHistory.onReadLocalVariable(currentTime, getCallContext(), variableName);
    }

    public void onReadObjectField(Object object, Object field) {
        currentHistory.onReadObjectField(currentTime, objToCtx.get(object), field);
    }

    public void onEnterFunction(NodeIdentifier identifier, String funcName, boolean isCalc) {
        invalidateCache();
        currentStack.push(new CallContextElement.FunctionCall(identifier));
        localVarFlagStack.push(new HashSet<>());
        final Time time = isCalc ? currentHistory.getTime(lastCalcCtx).getEnd() : currentTime;
        currentHistory.onEnterFunction(time, funcName);
    }

    public void pushArgumentFlags(boolean[] flags) {
        parameterFlagStack.push(flags);
    }

    public void onExitFunction(NodeIdentifier identifier) {
        invalidateCache();
        CallContextElement elem = currentStack.pop();
        assert elem instanceof CallContextElement.FunctionCall && elem.getNodeIdentifier() == identifier;
        localVarFlagStack.pop();
    }

    public void popArgumentFlags() {
        parameterFlagStack.pop();
    }

    public void onEnterLoop(NodeIdentifier identifier) {
        invalidateCallContextCache();
        currentStack.push(new CallContextElement.Loop(identifier, 0));
    }

    public void onGotoNextIteration(NodeIdentifier identifier) {
        invalidateCallContextCache();
        CallContextElement elem = currentStack.pop();
        assert elem instanceof CallContextElement.Loop && elem.getNodeIdentifier() == identifier;
        currentStack.push(((CallContextElement.Loop) elem).increment());
    }

    public void onExitLoop(NodeIdentifier identifier) {
        invalidateCallContextCache();
        CallContextElement elem = currentStack.pop();
        assert elem instanceof CallContextElement.Loop && elem.getNodeIdentifier() == identifier;
    }

    public void onGenerateObject(SLObject object, ExecutionContext execCtx) {
        objToCtx.put(object, execCtx);
        ctxToObj.put(execCtx, new WeakReference<>(object));
    }

    public void onObjectUpdated(Object object, String fldName, Object newValue) {
        final ExecutionContext objGenCtx = objToCtx.get(object);
        currentHistory.onUpdateObjectWithHash(currentTime, objGenCtx, fldName, replaceReference(newValue));
        objectFieldFlags.computeIfAbsent(objGenCtx, it -> new HashSet<>())
                .add(fldName);
    }

    public ShouldReExecuteResult shouldReExecute(SLStatementNode node) {
        if (node.hasNewNode()) return ShouldReExecuteResult.RE_EXECUTE;
        final NodeIdentifier identifier = node.getNodeIdentifier();
        Iterator<ItemWithTime<ExecutionHistory.ReadContent>> iter = getReadContentIterator(identifier);

        if (iter == null) return ShouldReExecuteResult.NEW_EXECUTE;

        while (iter.hasNext()) {
            ExecutionHistory.ReadContent readContent = iter.next().getItem();
            if (readContent instanceof ExecutionHistory.ReadArgument) {
                ExecutionHistory.ReadArgument content = (ExecutionHistory.ReadArgument) readContent;
                if (!content.getCallContext().equals(getFunctionCallArray())) continue;
                final int argIndex = content.getArgIndex();
                //noinspection DataFlowIssue
                if (parameterFlagStack.peek()[argIndex]) return ShouldReExecuteResult.RE_EXECUTE;
            } else if (readContent instanceof ExecutionHistory.ReadLocalVariable) {
                ExecutionHistory.ReadLocalVariable content = (ExecutionHistory.ReadLocalVariable) readContent;
                if (Arrays.equals(content.getCallContext(), getCallContext()) && getLocalVarFlagCache().contains(content.getVariableName())) {
                    return ShouldReExecuteResult.RE_EXECUTE;
                }
            } else if (readContent instanceof ExecutionHistory.ReadObjectField) {
                ExecutionHistory.ReadObjectField content = (ExecutionHistory.ReadObjectField) readContent;
                HashSet<Object> fields = objectFieldFlags.get(content.getObjGenCtx());
                if (fields != null && fields.contains(content.getFieldName())) {
                    return ShouldReExecuteResult.RE_EXECUTE;
                }
            }
        }

        return ShouldReExecuteResult.USE_CACHE;
    }

    public Object getReturnedValueOrThrow(NodeIdentifier identifier) {
        final ExecutionContext execCtx = getExecutionContext(identifier);
        return getReturnedValueOrThrow(execCtx);
    }

    public Object getReturnedValueOrThrow(ExecutionContext execCtx) {
        final ExecutionHistory.TimePair tp = currentHistory.getTime(execCtx);
        assert tp != null;
        System.out.println("Skipped from " + tp.getStart() + " to " + tp.getEnd());
        final Object value = currentHistory.getReturnedValueOrThrow(tp.getEnd());
        return revertObject(value);
    }

    public Object getVariableValue(Object varName, NodeIdentifier identifier) {
        final Time time = currentHistory.getTime(getExecutionContext(identifier)).getEnd();
        final ArrayList<ItemWithTime<Object>> varHistory = currentHistory.getLocalHistory(getFunctionCallArray()).get((String) varName);
        final int i = ItemWithTime.binarySearch(varHistory, time);
        return varHistory.get(i);
    }

    public Object getFieldValue(Object obj, String fieldName, NodeIdentifier identifier) {
        final ExecutionContext objGenCtx = objToCtx.get(obj);
        final Time time = currentHistory.getTime(getExecutionContext(identifier)).getEnd();
        final HashMap<String, ArrayList<ItemWithTime<Object>>> objectHistory = currentHistory.getObjectHistory(objGenCtx);
        final ArrayList<ItemWithTime<Object>> fieldHistory = objectHistory.get(fieldName);
        final int i = ItemWithTime.binarySearch(fieldHistory, time);
        final Object value = fieldHistory.get(i).getItem();
        return revertObject(value);
    }

    public void deleteHistory(NodeIdentifier identifier) {
        currentHistory.deleteRecords(getExecutionContext(identifier));
    }

    public Object calcGeneric(VirtualFrame frame, SLExpressionNode node) {
        try {
            switch (shouldReExecute(node)) {
                case USE_CACHE:
                    return getReturnedValueOrThrow(node.getNodeIdentifier());
                case RE_EXECUTE:
                    return node.calcGenericInner(frame);
                case NEW_EXECUTE:
                    return newExecutionGeneric(node.getNodeIdentifier(), frame, node::executeGeneric);
            }
        } finally {
            finishCalc(node.getNodeIdentifier());
        }

        throw new RuntimeException("Never reach here");
    }

    public Pair<Object, Boolean> calcGenericParameter(VirtualFrame frame, SLExpressionNode node) {
        try {
            switch (shouldReExecute(node)) {
                case USE_CACHE:
                    return Pair.create(getReturnedValueOrThrow(node.getNodeIdentifier()), false);
                case RE_EXECUTE:
                    return Pair.create(node.calcGenericInner(frame), true);
                case NEW_EXECUTE:
                    return Pair.create(newExecutionGeneric(node.getNodeIdentifier(), frame, node::executeGeneric), true);
            }
        } finally {
            finishCalc(node.getNodeIdentifier());
        }

        throw new RuntimeException("Never reach here");
    }

    public boolean calcBoolean(VirtualFrame frame, Node currentNode, SLExpressionNode node) {
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
                return node.calcBooleanInner(frame);
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
                return node.calcLongInner(frame);
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
                node.calcVoidInner(frame);
                return;
            case NEW_EXECUTE:
                newExecutionVoid(node.getNodeIdentifier(), frame, node::executeVoid);
                return;
            }
        } finally {
            finishCalc(node.getNodeIdentifier());
        }

        throw new RuntimeException("Never reach here");
    }

    private Iterator<ItemWithTime<ExecutionHistory.ReadContent>> getReadContentIterator(NodeIdentifier identifier) {
        return currentHistory.getReadOperations(getExecutionContext(identifier));
    }

    public Iterator<ItemWithTime<ObjectUpdate>> getObjectUpdatesIterator(ExecutionContext execCtx) {
        return currentHistory.getObjectUpdates(execCtx);
    }

    public void startNewExecution(VirtualFrame frame, NodeIdentifier identifier) {
        if (isInExec) return;
        isInExec = true;
        final ExecutionHistory history = currentHistory;
        final ExecutionHistory.TimePair time = history.getTime(getExecutionContext(identifier));
        if (time != null) {
            history.deleteRecords(time.getStart(), time.getEnd());
        }
        final ExecutionContext lastCalcCtx = this.lastCalcCtx;
        if (lastCalcCtx != null) {
            final ExecutionHistory.TimePair tp = history.getTime(lastCalcCtx);
            if (tp == null) {
                currentTime.inc();
            } else {
                currentTime = tp.getEnd().subdivide();
            }
        }
        constructFrameAndObjects(currentTime, frame);
        currentHistory = new ExecutionHistory();
    }

    public void endNewExecution(NodeIdentifier identifier) {
        rootHistory.merge(currentHistory);
        currentHistory = rootHistory;
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
            endNewExecution(identifier);
            throw e;
        }

        try {
            tickNode.onReturnValue(frame, result);
            return result;
        } finally {
            endNewExecution(identifier);
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
            endNewExecution(identifier);
            throw e;
        }

        try {
            tickNode.onReturnValue(frame, result);
            return result;
        } finally {
            endNewExecution(identifier);
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
            endNewExecution(identifier);
            throw e;
        }

        try {
            tickNode.onReturnValue(frame, result);
            return result;
        } finally {
            endNewExecution(identifier);
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
            endNewExecution(identifier);
            throw e;
        }

        try {
            tickNode.onReturnValue(frame, null);
        } finally {
            endNewExecution(identifier);
        }
    }

    public void finishCalc(NodeIdentifier identifier) {
        lastCalcCtx = getExecutionContext(identifier);
    }

    private void constructFrameAndObjects(Time time, VirtualFrame frame) {
        ExecutionHistory history = currentHistory;
        HashMap<String, ArrayList<ItemWithTime<Object>>> local = history.getLocalHistory(getFunctionCallArray());
        if (local == null) return;
        FrameDescriptor descriptor = frame.getFrameDescriptor();

        final HashMap<ExecutionContext, WeakReference<Object>> ctxToObj = new HashMap<>();
        final WeakHashMap<Object, ExecutionContext> objToCtx = new WeakHashMap<>();

        for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> entry : local.entrySet()) {
            String name = entry.getKey();
            ArrayList<ItemWithTime<Object>> list = entry.getValue();

            int i = ItemWithTime.binarySearch(list, time);
            Object value = list.get(i).getItem();
            FrameSlot slot = descriptor.findFrameSlot(name);
            if (value instanceof Long) {
                frame.setLong(slot, (Long) value);
            } else if (value instanceof Boolean) {
                frame.setBoolean(slot, (Boolean) value);
            } else if (value instanceof SLBigNumber || value instanceof String) {
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

    private Object constructObjects(Time time, ExecutionContext objGenCtx, ExecutionHistory history, HashMap<ExecutionContext, WeakReference<Object>> ctxToObj, WeakHashMap<Object, ExecutionContext> objToCtx) {
        SLObject newObject = language.createObject(this, objGenCtx);
        HashMap<String, ArrayList<ItemWithTime<Object>>> currentObjectHistory = history.getObjectHistory(objGenCtx);
        if (currentObjectHistory != null) {
            InteropLibrary library = INTEROP_LIBRARY_.create(newObject);
            for (Map.Entry<String, ArrayList<ItemWithTime<Object>>> entry : currentObjectHistory.entrySet()) {
                String name = entry.getKey();
                ArrayList<ItemWithTime<Object>> list = entry.getValue();

                int i = ItemWithTime.binarySearch(list, time);
                Object value = list.get(i).getItem();

                try {
                    if (value instanceof Long
                            || value instanceof SLBigNumber
                            || value instanceof Boolean
                            || value instanceof String) {
                        library.writeMember(newObject, name, value);
                    } else if (value instanceof ExecutionHistory.ObjectReference) {
                        ExecutionContext valueGenCtx = ((ExecutionHistory.ObjectReference) value).getObjGenCtx();
                        WeakReference<Object> objRef = ctxToObj.get(valueGenCtx);
                        Object newValue;
                        if (objRef == null) {
                            newValue = constructObjects(time, valueGenCtx, history, ctxToObj, objToCtx);
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

    private Time getAndIncrementTime() {
        return currentTime = currentTime.inc();
    }

    public ExecutionContext getExecutionContext(NodeIdentifier identifier) {
        return new ExecutionContext(getCallContext(), identifier);
    }

    private CallContextElement.FunctionCallArray getFunctionCallArray() {
        CallContextElement.FunctionCallArray callArray = this.callArrayCache;
        if (callArrayCache == null) {
            ArrayList<CallContextElement.FunctionCall> functionCalls = new ArrayList<>(currentStack.size());
            for (CallContextElement elem : currentStack) {
                if (elem instanceof CallContextElement.FunctionCall) {
                    functionCalls.add((CallContextElement.FunctionCall) elem);
                }
            }

            this.callArrayCache = callArray = new CallContextElement.FunctionCallArray(functionCalls.toArray(new CallContextElement.FunctionCall[0]));
        }

        return callArray;
    }

    private CallContextElement[] getCallContext() {
        CallContextElement[] callContext = this.callContextCache;
        if (callContext == null) {
            callContext = this.callContextCache = currentStack.toArray(new CallContextElement[0]);
        }
        return callContext;
    }

    private HashSet<Object> getLocalVarFlagCache() {
        HashSet<Object> localVarFlag = this.localVarFlagCache;
        if (localVarFlag == null) {
            localVarFlag = this.localVarFlagCache = localVarFlagStack.peek();
        }
        return localVarFlag;
    }

    private void invalidateCallContextCache() {
        callContextCache = null;
    }

    private void invalidateLocalVarFlagCache() {
        localVarFlagCache = null;
    }

    private void invalidateCallArrayCache() {
        callArrayCache = null;
    }

    private void invalidateCache() {
        invalidateCallContextCache();
        invalidateLocalVarFlagCache();
        invalidateCallArrayCache();
    }

    private Object revertObject(Object value) {
        if (value instanceof Long
                || value instanceof Boolean
                || value instanceof SLBigNumber
                || value instanceof String) {
            return value;
        } else if (value instanceof ExecutionHistory.ObjectReference) {
            return ctxToObj.computeIfAbsent(
                        ((ExecutionHistory.ObjectReference) value).getObjGenCtx(),
                        objGenCtx -> new WeakReference<>(language.createObject(this, objGenCtx))
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

    private Object getObject(Time time, ExecutionHistory.ObjectReference ref) {
        ExecutionContext objGenCtx = ref.getObjGenCtx();
        WeakReference<Object> objRef = ctxToObj.get(objGenCtx);
        return objRef == null ? constructObjects(time, objGenCtx, currentHistory, ctxToObj, objToCtx) : objRef.get();
    }

    private SLFunction getFunction(String functionName) {
        return functionRegistry.getFunction(functionName);
    }

    private Object replaceReference(Object value) {
        Object saveNewValue;
        if (value == null
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Float
                || value instanceof Double
                || value instanceof Character
                || value instanceof String
                || value instanceof SLBigNumber
        ) {
            saveNewValue = value;
        } else if (value instanceof SLFunction) {
            saveNewValue = new FunctionReference(((SLFunction) value).getName());
        } else {
            saveNewValue = new ExecutionHistory.ObjectReference(objToCtx.get(value));
        }

        return saveNewValue;
    }

    public final static class ObjectUpdate {
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

    public final static class FunctionReference {
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
            if (exception instanceof RuntimeException) {
                currentHistory.onReturnExceptional(startTime.pop(), getAndIncrementTime(), getExecutionContext(identifier), (RuntimeException) exception);
            }
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

    private static enum ShouldReExecuteResult {
        USE_CACHE, RE_EXECUTE, NEW_EXECUTE
    }
}
