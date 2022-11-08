package com.oracle.truffle.sl.runtime.cache;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory;
import com.oracle.truffle.api.instrumentation.ObjectChangeEvent;
import com.oracle.truffle.api.instrumentation.ObjectChangeListener;
import com.oracle.truffle.api.instrumentation.StackListener;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public final class ExecutionHistoryOperator {
    private Time currentTime = Time.zero();
    private final ExecutionHistory history;
    private final ArrayDeque<HashSet<Object>> localVarFlagStack = new ArrayDeque<>();
    private final HashMap<Integer, HashSet<Object>> objectFieldFlags = new HashMap<>();
    private final ArrayDeque<CallContextElement> currentStack = new ArrayDeque<>();
    private final HashMap<Integer, Integer> objectMapping = new HashMap<>(); // objId in prevExec -> objId in currentExec

    private CallContextElement[] callContextCache = null;
    private HashSet<Object> localVarFlagCache = null;
    private NextTimeState nextTimeState;

    private final ObjectChangeListener objectChangeListener = new ObjectChangeListener() {
        @Override
        public void onFieldAssigned(ObjectChangeEvent e) {
            int objectHash = e.getObject().hashCode();
            String key = (String) e.getKey();

            objectFieldFlags.computeIfAbsent(objectHash, it -> new HashSet<>())
                    .add(key);
            history.onUpdateObjectWithHash(currentTime, objectHash, key, e.getValue());
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
        return instrumentedNode != null ? new TickNode(instrumentedNode.getNodeIdentifier()) : null;
    };

    public ExecutionHistoryOperator(ExecutionHistory prevHistory) {
        this.history = prevHistory;
    }

    public ExecutionHistoryOperator() {
        this(new ExecutionHistory());
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
        history.onReadArgument(currentTime, getCallContext(), argumentName);
    }

    public void onReadLocalVariable(Object variableName) {
        history.onReadLocalVariable(currentTime, getCallContext(), variableName);
    }

    public void onReadObjectField(Object object, Object field) {
        history.onReadObjectField(currentTime, object, field);
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

    public boolean shouldRecalculate(SLStatementNode node) {
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

    public Object getReturnedValueOrThrow(NodeIdentifier identifier) {
        Time finishedTime = history.getFinishedTime(getExecutionContext(identifier));
        assert finishedTime != null;
        return history.getReturnedValueOrThrow(finishedTime);
    }

    public Object calcGeneric(VirtualFrame frame, SLExpressionNode node) {
        if (shouldRecalculate(node)) {
            return node.calcGeneric(frame);
        } else {
            return getReturnedValueOrThrow(node.getNodeIdentifier());
        }
    }

    public boolean calcBoolean(VirtualFrame frame, SLExpressionNode currentNode, SLExpressionNode node) {
        try {
            if (shouldRecalculate(node)) {
                return node.calcBoolean(frame);
            } else {
                final Object obj = getReturnedValueOrThrow(node.getNodeIdentifier());
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
            if (shouldRecalculate(node)) {
                return node.calcLong(frame);
            } else {
                final Object obj = getReturnedValueOrThrow(node.getNodeIdentifier());
                if (obj instanceof Long) {
                    return (long) obj;
                } else {
                    throw new UnexpectedResultException(obj);
                }
            }
        } catch (UnexpectedResultException ex) {
            throw SLException.typeError(currentNode, ex.getResult());
        }
    }

    public void calcVoid(VirtualFrame frame, SLStatementNode node) {
        if (shouldRecalculate(node)) {
            node.calcVoid(frame);
        } else {
            getReturnedValueOrThrow(node.getNodeIdentifier());
        }
    }

    public boolean didExecuted(NodeIdentifier nodeIdentifier) {
        return history.didExecuted(getExecutionContext(nodeIdentifier));
    }

    public Iterator<ItemWithTime<ExecutionHistory.ReadContent>> getReadContentIterator(NodeIdentifier identifier) {
        return history.getReadOperations(getExecutionContext(identifier));
    }

    public Iterator<ItemWithTime<ExecutionHistory.UpdateContent>> getUpdateContentIterator(NodeIdentifier identifier) {
        return history.getUpdateOperations(getExecutionContext(identifier));
    }

    public void startNewExecution(NodeIdentifier identifier) {
        nextTimeState = NextTimeState.SUBDIVIDE;
    }

    public void endNewExecution(NodeIdentifier identifier) {
        nextTimeState = NextTimeState.NORMAL;
    }

    private Time getAndIncrementTime() {
        Time currentTime = this.currentTime;

        switch (nextTimeState) {
            case SUBDIVIDE:
                this.currentTime = currentTime.subdivide();
                nextTimeState = NextTimeState.NORMAL;
                break;
            case NORMAL:
                this.currentTime = currentTime.inc();
                break;
        }

        return currentTime;
    }

    private ExecutionContext getExecutionContext(NodeIdentifier identifier) {
        return new ExecutionContext(getCallContext(), identifier);
    }

    private CallContextElement[] getCallContext() {
        CallContextElement[] callContextCache = this.callContextCache;
        if (callContextCache == null) {
            callContextCache = this.callContextCache = currentStack.toArray(new CallContextElement[0]);
        }
        return callContextCache;
    }

    private HashSet<Object> getLocalVarFlagCache() {
        HashSet<Object> localVarFlagCache = this.localVarFlagCache;
        if (localVarFlagCache == null) {
            localVarFlagCache = this.localVarFlagCache = localVarFlagStack.peek();
        }
        return localVarFlagCache;
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

    private final class TickNode extends ExecutionEventNode {
        private Time startTime;
        private final NodeIdentifier identifier;

        private TickNode(NodeIdentifier identifier) {
            this.identifier = identifier;
        }

        @Override
        protected void onEnter(VirtualFrame frame) {
            startTime = currentTime;
        }

        @Override
        protected void onReturnValue(VirtualFrame frame, Object result) {
            history.onReturnValue(startTime, getAndIncrementTime(), getExecutionContext(identifier), result);
        }

        @Override
        protected void onReturnExceptional(VirtualFrame frame, Throwable exception) {
            if (exception instanceof RuntimeException) {
                history.onReturnExceptional(startTime, getAndIncrementTime(), getExecutionContext(identifier), (RuntimeException) exception);
            }
        }
    }

    private enum NextTimeState {
        NORMAL, SUBDIVIDE
    }
}
