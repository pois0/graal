package com.oracle.truffle.sl.runtime.cache;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory;
import com.oracle.truffle.api.instrumentation.ObjectChangeEvent;
import com.oracle.truffle.api.instrumentation.ObjectChangeListener;
import com.oracle.truffle.api.instrumentation.StackListener;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.SLBigNumber;
import com.oracle.truffle.sl.runtime.SLObject;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public final class ExecutionHistoryOperator {
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    private final SLLanguage language;
    private final AllocationReporter allocationReporter;

    private Time currentTime = Time.zero().subdivide();
    private boolean isInExec = false;
    private ExecutionContext lastCalcCtx = null;
    private ExecutionHistory currentHistory;
    private final ExecutionHistory rootHistory;
    private final ArrayDeque<HashSet<Object>> localVarFlagStack = new ArrayDeque<>();
    private final HashMap<Integer, HashSet<Object>> objectFieldFlags = new HashMap<>();
    private final ArrayDeque<CallContextElement> currentStack = new ArrayDeque<>();
    private HashMap<Integer, WeakReference<Object>> objectHolder = new HashMap<>(); // hash -> weak reference of object

    // Caches
    private CallContextElement[] callContextCache = null;
    private CallContextElement.FunctionCallArray callArrayCache = null;
    private HashSet<Object> localVarFlagCache = null;

    private final ObjectChangeListener objectChangeListener = new ObjectChangeListener() {
        @Override
        public void onFieldAssigned(ObjectChangeEvent e) {
            int objectHash = e.getObject().hashCode();
            String key = (String) e.getKey();

            objectFieldFlags.computeIfAbsent(objectHash, it -> new HashSet<>())
                    .add(key);
            currentHistory.onUpdateObjectWithHash(currentTime, objectHash, key, e.getValue());
        }
    };

    private final StackListener stackListener = new StackListener() {
        @Override
        public void onObjectSet(FrameSlot slot, Object value) {
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onBooleanSet(FrameSlot slot, boolean value) {
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onByteSet(FrameSlot slot, byte value) {
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onIntSet(FrameSlot slot, int value) {
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onLongSet(FrameSlot slot, long value) {
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onFloatSet(FrameSlot slot, float value) {
            getLocalVarFlagCache().add(slot.getIdentifier());
        }

        @Override
        public void onDoubleSet(FrameSlot slot, double value) {
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

    public ExecutionHistoryOperator(SLLanguage language, AllocationReporter allocationReporter, ExecutionHistory prevHistory) {
        this.language = language;
        this.allocationReporter = allocationReporter;
        this.currentHistory = prevHistory;
        this.rootHistory = currentHistory;
    }

    public ExecutionHistoryOperator(SLLanguage language, AllocationReporter reporter) {
        this(language, reporter, new ExecutionHistory());
    }

    public ObjectChangeListener getObjectChangeListener() {
        return objectChangeListener;
    }

    public StackListener getStackListener() {
        return stackListener;
    }

    public ExecutionEventNodeFactory getTickFactory() {
        return tickFactory;
    }

    public void onReadArgument(Object argumentName) {
        currentHistory.onReadArgument(currentTime, getCallContext(), argumentName);
    }

    public void onReadLocalVariable(Object variableName) {
        currentHistory.onReadLocalVariable(currentTime, getCallContext(), variableName);
    }

    public void onReadObjectField(Object object, Object field) {
        currentHistory.onReadObjectField(currentTime, object, field);
    }

    public void onEnterFunction(NodeIdentifier identifier) {
        invalidateCache();
        currentStack.push(new CallContextElement.FunctionCall(identifier));
        localVarFlagStack.push(new HashSet<>());
    }

    public void onExitFunction(NodeIdentifier identifier) {
        invalidateCache();
        CallContextElement elem = currentStack.pop();
        assert elem instanceof CallContextElement.FunctionCall && elem.getNodeIdentifier() == identifier;
        localVarFlagStack.pop();
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

    public boolean shouldReExecute(SLStatementNode node) {
        if (node.hasNewNode()) return true;
        final NodeIdentifier identifier = node.getNodeIdentifier();
        Iterator<ItemWithTime<ExecutionHistory.ReadContent>> iter = getReadContentIterator(identifier);

        while (iter.hasNext()) {
            ExecutionHistory.ReadContent readContent = iter.next().getItem();
            if (readContent instanceof ExecutionHistory.ReadArgument) {

            } else if (readContent instanceof ExecutionHistory.ReadLocalVariable) {
                ExecutionHistory.ReadLocalVariable content = (ExecutionHistory.ReadLocalVariable) readContent;
                if (Arrays.equals(content.getCallContext(), getCallContext()) && getLocalVarFlagCache().contains(content.getVariableName())) {
                    return true;
                }
            } else if (readContent instanceof ExecutionHistory.ReadObjectField) {
                ExecutionHistory.ReadObjectField content = (ExecutionHistory.ReadObjectField) readContent;
                HashSet<Object> fields = objectFieldFlags.get(content.getObjectId());
                if (fields != null && fields.contains(content.getFieldName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public Object getReturnedValueOrThrow(NodeIdentifier identifier, VirtualFrame frame) {
        ExecutionContext execCtx = getExecutionContext(identifier);

        Iterator<ItemWithTime<ExecutionHistory.ObjectUpdate>> updateContentIterator = getObjectUpdatesIterator(execCtx);
        ItemWithTime<ExecutionHistory.ObjectUpdate> it = updateContentIterator.next();
        while (it != null) {
            ExecutionHistory.ObjectUpdate item = it.getItem();

            Object obj = objectHolder.get(item.getObjectId()).get();
            InteropLibrary library = INTEROP_LIBRARY_.getUncached(obj);
            try {
                library.writeMember(obj, item.getFieldName(), revertObject(item.getNewValue()));
            } catch (UnsupportedMessageException | UnknownIdentifierException | UnsupportedTypeException e) {
                throw new RuntimeException(e);
            }

            it = updateContentIterator.next();
        }

        Time finishedTime = currentHistory.getFinishedTime(execCtx);
        assert finishedTime != null;
        return currentHistory.getReturnedValueOrThrow(finishedTime);
    }

    public Object calcGeneric(VirtualFrame frame, SLExpressionNode node) {
        try {
            if (shouldReExecute(node)) {
                return node.calcGenericInner(frame);
            } else {
                return getReturnedValueOrThrow(node.getNodeIdentifier(), frame);
            }
        } finally {
            finishCalc(node.getNodeIdentifier());
        }
    }

    public boolean calcBoolean(VirtualFrame frame, SLExpressionNode currentNode, SLExpressionNode node) {
        try {
            if (shouldReExecute(node)) {
                return node.calcBooleanInner(frame);
            } else {
                final Object obj = getReturnedValueOrThrow(node.getNodeIdentifier(), frame);
                if (obj instanceof Boolean) {
                    return (boolean) obj;
                } else {
                    throw new UnexpectedResultException(obj);
                }
            }
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(currentNode, ex.getResult());
        }
    }

    public long calcLong(VirtualFrame frame, SLExpressionNode currentNode, SLExpressionNode node) {
        try {
            if (shouldReExecute(node)) {
                try {
                    return node.calcLongInner(frame);
                } finally {
                    finishCalc(node.getNodeIdentifier());
                }
            } else {
                final Object obj = getReturnedValueOrThrow(node.getNodeIdentifier(), frame);
                if (obj instanceof Long) {
                    return (Long) obj;
                } else {
                    throw new UnexpectedResultException(obj);
                }
            }
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(currentNode, ex.getResult());
        }
    }

    public void calcVoid(VirtualFrame frame, SLStatementNode node) {
        if (shouldReExecute(node)) {
            node.calcVoidInner(frame);
            finishCalc(node.getNodeIdentifier());
        } else {
            getReturnedValueOrThrow(node.getNodeIdentifier(), frame);
        }
    }

    public Object calcFunction(VirtualFrame frame, SLStatementNode node) {
        // TODO
        return null;
    }

    public Iterator<ItemWithTime<ExecutionHistory.ReadContent>> getReadContentIterator(NodeIdentifier identifier) {
        return currentHistory.getReadOperations(getExecutionContext(identifier));
    }

    public Iterator<ItemWithTime<ExecutionHistory.ObjectUpdate>> getObjectUpdatesIterator(NodeIdentifier identifier) {
        return currentHistory.getObjectUpdates(getExecutionContext(identifier));
    }

    public Iterator<ItemWithTime<ExecutionHistory.ObjectUpdate>> getObjectUpdatesIterator(ExecutionContext execCtx) {
        return currentHistory.getObjectUpdates(execCtx);
    }

    public void startNewExecution(VirtualFrame frame, NodeIdentifier identifier) {
        if (isInExec) return;
        isInExec = true;
        final ExecutionContext lastCalcCtx = this.lastCalcCtx;
        if (lastCalcCtx != null) {

            currentTime = currentHistory.getTime(lastCalcCtx).getEnd().subdivide();
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
        FrameDescriptor descriptor = frame.getFrameDescriptor();

        HashMap<Integer, WeakReference<Object>> hashToObj = new HashMap<>();

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
                Object obj = constructObjects(time, ((ExecutionHistory.ObjectReference) value).getReferenceHash(), history, hashToObj);
                frame.setObject(slot, obj);
            } else {
                throw new RuntimeException("Unknown value type: " + value.getClass().getName());
            }
        }

        objectHolder = hashToObj;
    }

    private Object constructObjects(Time time, int idHash, ExecutionHistory history, HashMap<Integer, WeakReference<Object>> hashToObj) {
        SLObject newObject = language.createObject(allocationReporter);
        hashToObj.put(idHash, new WeakReference<>(newObject));
        HashMap<String, ArrayList<ItemWithTime<Object>>> currentObjectHistory = history.getObjectHistory(idHash);
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
                        int valueObjIdHash = ((ExecutionHistory.ObjectReference) value).getReferenceHash();
                        WeakReference<Object> objRef = hashToObj.get(valueObjIdHash);
                        Object newValue;
                        if (objRef == null) {
                            newValue = constructObjects(time, valueObjIdHash, history, hashToObj);
                        } else {
                            newValue = objRef.get();
                        }
                        library.writeMember(newObject, name, newValue);
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

    private ExecutionContext getExecutionContext(NodeIdentifier identifier) {
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

            callArray = new CallContextElement.FunctionCallArray(functionCalls.toArray(new CallContextElement.FunctionCall[0]));
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

    private void invalidateCache() {
        invalidateCallContextCache();
        invalidateLocalVarFlagCache();
    }

    private Object revertObject(Time time, ExecutionHistory history, Object value) {
        if (value instanceof Long
                || value instanceof Boolean
                || value instanceof SLBigNumber
                || value instanceof String) {
            return value;
        } else if (value instanceof ExecutionHistory.ObjectReference) {
            int hash = ((ExecutionHistory.ObjectReference) value).getReferenceHash();
            WeakReference<Object> objRef = objectHolder.get(hash);
            return objRef == null ? constructObjects(time, hash, history, objectHolder) : objRef.get();
        } else {
            throw new RuntimeException("Invalid type: " + value.getClass().getName());
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
            currentHistory.onReturnValue(startTime.pop(), getAndIncrementTime(), getExecutionContext(identifier), result);
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
}
