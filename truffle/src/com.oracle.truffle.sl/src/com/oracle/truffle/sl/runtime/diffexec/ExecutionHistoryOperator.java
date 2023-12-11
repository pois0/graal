package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.SLObjectBase;

import java.util.ArrayDeque;

public abstract class ExecutionHistoryOperator<ExecKey> {
    private final ExecutionEventNodeFactory tickFactory = context -> {
        final SLStatementNode instrumentedNode = (SLStatementNode) context.getInstrumentedNode();
        if (instrumentedNode == null) return null;

        final NodeIdentifier identifier = instrumentedNode.getNodeIdentifier();
        if (identifier == null) return null;

        return new TickNode(identifier);
    };

    public ExecutionEventNodeFactory getTickFactory() {
        return tickFactory;
    }

    public abstract void onReadArgument(int index);
    public abstract void onReadLocalVariable(int slot);
    public abstract void onReadObjectField(Object receiver, String fieldName);
    public abstract void onReadArrayElement(Object array, long index);

    public abstract void onGenerateObject(SLObjectBase object);

    public abstract void onUpdateLocalVariable(int slot, Object value);
    public abstract void onUpdateObjectField(Object receiver, String fieldName, Object value);
    public abstract void onUpdateArrayElement(Object array, long index, Object value);

    public abstract void rewriteLocalVariable(int slot, Object value, NodeIdentifier identifier);
    public abstract void rewriteObjectField(Object receiver, String fieldName, Object value, NodeIdentifier identifier, boolean fieldChanged);
    public abstract void rewriteArrayElement(Object array, long index, Object value, NodeIdentifier identifier, boolean fieldChanged);

    public abstract void deleteHistory(NodeIdentifier identifier);

    public abstract ExecKey onEnterExpression(NodeIdentifier identifier);
    public abstract void onReturnValue(NodeIdentifier identifier, ExecKey onEnterKey, Object result);
    public abstract void onReturnExceptional(NodeIdentifier identifier, ExecKey onEnterKey, Throwable exception);

    public abstract void onEnterFunctionDuringExec(NodeIdentifier callerIdentifier, TruffleString functionName, int argLen);
    public abstract void onEnterFunctionDuringCalc(NodeIdentifier callerIdentifier, TruffleString functionName, boolean[] argFlags);
    public abstract void onExitFunction(boolean duringCalc);
    public abstract void onEnterLoop(NodeIdentifier identifier);
    public abstract void onEnterNextIteration();
    public abstract void onExitLoop();

    public abstract void calcVoid(VirtualFrame frame, SLStatementNode node);
    public abstract CalcResult.Generic calcGeneric(VirtualFrame frame, SLExpressionNode calleeNode);
    public abstract CalcResult.Boolean calcBoolean(VirtualFrame frame, SLStatementNode callerNode, SLExpressionNode calleeNode);
    public abstract CalcResult.Long calcLong(VirtualFrame frame, SLExpressionNode callerNode, SLExpressionNode calleeNode);

    public abstract Object getVariableValue(int slot, NodeIdentifier identifier);
    public abstract Object getObjectFieldValue(Node node, Object receiver, String fieldName, NodeIdentifier identifier);
    public abstract Object getArrayElementValue(Node node, Object array, long index, NodeIdentifier identifier);

    public abstract boolean isInitialExecution();

    protected TickNode getNewTickNode(NodeIdentifier identifier) {
        return new TickNode(identifier);
    }

    protected class TickNode extends ExecutionEventNode {
        private final ArrayDeque<ExecKey> stack;
        private final NodeIdentifier identifier;

        private TickNode(NodeIdentifier identifier) {
            this.identifier = identifier;
            stack = new ArrayDeque<>();
        }

        @Override
        protected void onEnter(VirtualFrame frame) {
            stack.push(onEnterExpression(identifier));
        }

        @Override
        protected void onReturnValue(VirtualFrame frame, Object result) {
            ExecutionHistoryOperator.this.onReturnValue(identifier, stack.pop(), result);
        }

        @Override
        protected void onReturnExceptional(VirtualFrame frame, Throwable exception) {
            ExecutionHistoryOperator.this.onReturnExceptional(identifier, stack.pop(), exception);
        }
    }
}
