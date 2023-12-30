package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.nodes.controlflow.SLReturnException;
import com.oracle.truffle.sl.runtime.SLBigInteger;
import com.oracle.truffle.sl.runtime.SLFunction;
import com.oracle.truffle.sl.runtime.SLNull;
import com.oracle.truffle.sl.runtime.SLObject;
import com.oracle.truffle.sl.runtime.SLObjectBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public final class RecordOnlyRuntime<TIME extends Time<TIME>> extends ExecutionHistoryOperator<TIME> {

    private TIME currentTime;
    private final History<TIME> history = new History<>();
    private CallContext currentContext = CallContext.EXECUTION_BASE;
    private final LocalVarOperatorHolder localVarOperatorHolder = new LocalVarOperatorHolder(0);

    public RecordOnlyRuntime(TIME zero) {
        this.currentTime = zero;
    }

    @Override
    public void onReadArgument(int index) {}

    @Override
    public void onReadLocalVariable(int slot) {}

    @Override
    public void onReadObjectField(Object receiver, String fieldName) {}

    @Override
    public void onReadArrayElement(Object array, long index) {}

    @Override
    public Object generateObject(Node node) {
        final var currentTime = this.currentTime;
        final var object = new SLObject(SLLanguage.get(node).getRootShape(), currentTime);
        history.onCreateObject(currentTime);
        return object;
    }

    @Override
    public void onUpdateLocalVariable(int slot, Object value) {
        localVarOperatorHolder.onUpdateVariable(currentTime, slot, value);
    }

    @Override
    public void onUpdateObjectField(Object receiver, String fieldName, Object value) {
        @SuppressWarnings("unchecked")
        final var objGenTime = (TIME) ((SLObjectBase) receiver).getObjGenTime();
        history.onUpdateObjectWithHash(currentTime, objGenTime, fieldName, replaceToMock(value));
    }

    @Override
    public void onUpdateArrayElement(Object array, long index, Object value) {
        onUpdateObjectField(array, Long.toString(index), value);
    }

    @Override
    public void rewriteLocalVariable(int slot, Object value, NodeIdentifier identifier) {
        neverReachHere();
    }

    @Override
    public void rewriteObjectField(Object receiver, String fieldName, Object value, NodeIdentifier identifier, boolean fieldChanged) {
        neverReachHere();
    }

    @Override
    public void rewriteArrayElement(Object array, long index, Object value, NodeIdentifier identifier, boolean fieldChanged) {
        neverReachHere();
    }

    @Override
    public void deleteHistory(NodeIdentifier identifier) {}

    @Override
    public TIME onEnterExpression(NodeIdentifier identifier) {
        return currentTime;
    }

    @Override
    public void onReturnValue(NodeIdentifier identifier, TIME onEnterKey, Object result) {
        history.onReturnValue(onEnterKey, getAndIncrementTime(), getExecutionContext(identifier), replaceToMock(result));
    }

    @Override
    public void onReturnExceptional(NodeIdentifier identifier, TIME onEnterKey, Throwable exception) {
        if (exception instanceof SLReturnException) {
            final var forRecord = new SLReturnException(replaceToMock(((SLReturnException) exception).getResult()));
            history.onReturnExceptional(onEnterKey, getAndIncrementTime(), getExecutionContext(identifier), forRecord);
        } else if (exception instanceof ControlFlowException || exception instanceof AbstractTruffleException) {
            history.onReturnExceptional(onEnterKey, getAndIncrementTime(), getExecutionContext(identifier), (RuntimeException) exception);
        }
    }

    @Override
    public void onEnterFunctionDuringExec(NodeIdentifier callerIdentifier, TruffleString functionName, int argLen) {
        final var currentStack = CallContext.functionCall(this.currentContext, callerIdentifier);
        this.currentContext = currentStack;
        localVarOperatorHolder.push(argLen, currentStack);
    }

    @Override
    public void onEnterFunctionDuringCalc(NodeIdentifier callerIdentifier, TruffleString functionName, boolean[] argFlags) {
        neverReachHere();
    }

    @Override
    public void onExitFunction(boolean duringCalc) {
        currentContext = currentContext.getRoot();
        localVarOperatorHolder.pop();
    }

    @Override
    public void onEnterLoop(NodeIdentifier identifier) {
        currentContext = CallContext.loop(this.currentContext, identifier);
    }

    @Override
    public void onEnterNextIteration() {
        currentContext = CallContext.loopNextIter(currentContext);
    }

    @Override
    public void onExitLoop() {
        currentContext = currentContext.getRoot();
    }

    @Override
    public void calcVoid(VirtualFrame frame, SLStatementNode node) {
        neverReachHere();
    }

    @Override
    public CalcResult.Generic calcGeneric(VirtualFrame frame, SLExpressionNode calleeNode) {
        return neverReachHere();
    }

    @Override
    public CalcResult.Boolean calcBoolean(VirtualFrame frame, SLStatementNode callerNode, SLExpressionNode calleeNode) {
        return neverReachHere();
    }

    @Override
    public CalcResult.Long calcLong(VirtualFrame frame, SLExpressionNode callerNode, SLExpressionNode calleeNode) {
        return neverReachHere();
    }

    @Override
    public Object getVariableValue(int slot, NodeIdentifier identifier) {
        return neverReachHere();
    }

    @Override
    public Object getObjectFieldValue(Node node, Object receiver, String fieldName, NodeIdentifier identifier) {
        return neverReachHere();
    }

    @Override
    public Object getArrayElementValue(Node node, Object array, long index, NodeIdentifier identifier) {
        return neverReachHere();
    }

    @Override
    public boolean isInitialExecution() {
        return true;
    }

    private TIME getAndIncrementTime() {
        TIME tmp = currentTime;
        currentTime = tmp.inc();
        return tmp;
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
        } else if (value instanceof SLObject) {
            //noinspection unchecked
            return new ObjectReference<>((TIME) ((SLObject) value).getObjGenTime());
        } else {
            throw new RuntimeException("Unavailable object: " + value.getClass().getName());
        }
    }

    private static <T> T neverReachHere() {
        throw new RuntimeException("Never reach here");
    }

    private ExecutionContext getExecutionContext(NodeIdentifier identifier) {
        return new ExecutionContext(currentContext, identifier);
    }

    private static final class History<TIME extends Time<TIME>> {
        private final ArrayList<ItemWithTime<TIME, ExecutionContext>> timeToContext = new ArrayList<>(1_000);
        private final HashMap<NodeIdentifier, HashMap<CallContext, DiffExecHistory.TimeInfo<TIME>>> contextToTime = new HashMap<>(1_000);
        private final HashMap<TIME, HashMap<String, ArrayList<ItemWithTime<TIME, Object>>>> objectUpdateMap = new HashMap<>(1_000);
        private final ArrayList<ItemWithTime<TIME, ObjectUpdate<TIME>>> objectUpdateList = new ArrayList<>(1_000);
        private final HashMap<CallContext, LocalVarOperator<TIME>> localVarInfo = new HashMap<>(1_000);

        public void onReturnValue(TIME startTime, TIME endTime, ExecutionContext ctx, Object value) {
            timeToContext.add(new ItemWithTime<>(endTime, ctx));
            contextToTime.computeIfAbsent(ctx.getCurrentNodeIdentifier(), it -> new HashMap<>())
                    .put(ctx.getCallContext(), new DiffExecHistory.TimeInfo<>(startTime, endTime, value));
        }

        public void onReturnExceptional(TIME startTime, TIME endTime, ExecutionContext ctx, RuntimeException e) {
            timeToContext.add(new ItemWithTime<>(endTime, ctx));
            contextToTime.computeIfAbsent(ctx.getCurrentNodeIdentifier(), it -> new HashMap<>())
                    .put(ctx.getCallContext(), new DiffExecHistory.TimeInfo<>(startTime, endTime, e));
        }

        public void onCreateObject(TIME time) {
            objectUpdateMap.putIfAbsent(time, new HashMap<>());
        }

        public void onUpdateObjectWithHash(TIME time, TIME objGenTime, String fieldName, Object newValue) {
            objectUpdateMap.computeIfAbsent(objGenTime, it -> new HashMap<>())
                    .computeIfAbsent(fieldName, it -> new ArrayList<>())
                    .add(new ItemWithTime<>(time, newValue));
            objectUpdateList.add(new ItemWithTime<>(time, new ObjectUpdate<>(objGenTime, fieldName, newValue)));
        }

        public LocalVarOperator<TIME> getLocalVarOperator(CallContext ctx) {
            return localVarInfo.computeIfAbsent(ctx, it -> new LocalVarOperator<>());
        }
    }

    private final static class LocalVarOperator<TIME extends Time<TIME>> {
        private final HashMap<Integer, ArrayList<ItemWithTime<TIME, Object>>> writeVarMap = new HashMap<>();
        private final ArrayList<ItemWithTime<TIME, LocalVariableUpdate>> writeVarList = new ArrayList<>();

        public void onUpdateVariable(TIME time, int slot, Object newValue) {
            writeVarMap.computeIfAbsent(slot, it -> new ArrayList<>())
                    .add(new ItemWithTime<>(time, newValue));
            writeVarList.add(new ItemWithTime<>(time, new LocalVariableUpdate(slot, newValue)));
        }
    }

    private final class LocalVarOperatorHolder {
        private ScopeInfo<TIME>[] stack;
        private int pointer = 1;

        public LocalVarOperatorHolder(int executionParamLen) {
            @SuppressWarnings("unchecked") final ScopeInfo<TIME>[] stack = new ScopeInfo[32];
            stack[0] = new ScopeInfo<>(executionParamLen, CallContext.EXECUTION_BASE);
            this.stack = stack;
        }

        public void push(int paramLen, CallContext ctx) {
            var stack = this.stack;
            final var currentLen = stack.length;
            if (pointer == currentLen) this.stack = stack = Arrays.copyOf(stack, currentLen + (currentLen >> 1));
            stack[pointer++] = new ScopeInfo<>(paramLen, ctx);
        }

        public void pop() {
            assert pointer >= 0;
            stack[--pointer] = null;
        }

        public LocalVarOperator<TIME> peek() {
            ScopeInfo<TIME> info = stack[pointer - 1];
            if (info.op != null) return info.op;
            return info.op = history.getLocalVarOperator(info.cc);
        }

        public void onUpdateVariable(TIME time, int slot, Object newValue) {
            peek().onUpdateVariable(time, slot, replaceToMock(newValue));
        }

        private static final class ScopeInfo<TIME extends Time<TIME>> {
            final int paramLen;
            final CallContext cc;
            LocalVarOperator<TIME> op = null;

            public ScopeInfo(int paramLen, CallContext cc) {
                this.paramLen = paramLen;
                this.cc = cc;
            }
        }
    }
}
