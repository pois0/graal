package com.oracle.truffle.sl.nodes.cache;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.cache.ExecutionHistoryOperator;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;

public final class ReplaceNode extends SLExpressionNode {
    @Child private SLExpressionNode delegateNode;
    private final NodeIdentifier deleted;

    public ReplaceNode(SLExpressionNode delegateNode, NodeIdentifier deleted) {
        this.delegateNode = delegateNode;
        this.deleted = deleted;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return delegateNode.executeGeneric(frame);
    }

    @Override
    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return delegateNode.executeLong(frame);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        return delegateNode.executeBoolean(frame);
    }

    @Override
    public Object calcGenericInner(VirtualFrame frame) {
        final ExecutionHistoryOperator op = getContext().getHistoryOperator();
        op.deleteHistory(deleted);
        return op.newExecutionGeneric(getNodeIdentifier(), frame, this::executeGeneric);
    }

    @Override
    public boolean calcBooleanInner(VirtualFrame frame) throws UnexpectedResultException {
        final ExecutionHistoryOperator op = getContext().getHistoryOperator();
        op.deleteHistory(deleted);
        return op.newExecutionBoolean(getNodeIdentifier(), frame, this::executeBoolean);
    }

    @Override
    public long calcLongInner(VirtualFrame frame) throws UnexpectedResultException {
        final ExecutionHistoryOperator op = getContext().getHistoryOperator();
        op.deleteHistory(deleted);
        return op.newExecutionLong(getNodeIdentifier(), frame, this::executeLong);
    }

    public boolean isEqualNode(SLStatementNode that) {
        return false;
    }

    @Override
    protected boolean hasNewChildNode() {
        return true;
    }
}
