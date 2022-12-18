package com.oracle.truffle.sl.nodes.cache;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;

public final class NewNode extends SLExpressionNode {
    @Child private SLExpressionNode delegateNode;

    public NewNode(SLExpressionNode delegateNode) {
        this.delegateNode = delegateNode;
        this.setNewNode();
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return delegateNode.executeGeneric(frame);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        return delegateNode.executeBoolean(frame);
    }

    @Override
    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return delegateNode.executeLong(frame);
    }

    @Override
    public Object calcGenericInner(VirtualFrame frame) {
        return getContext().getHistoryOperator().newExecutionGeneric(getNodeIdentifier(), frame, this::executeGeneric);
    }

    @Override
    public boolean calcBooleanInner(VirtualFrame frame) throws UnexpectedResultException {
        return getContext().getHistoryOperator().newExecutionBoolean(getNodeIdentifier(), frame, this::executeBoolean);
    }

    @Override
    public long calcLongInner(VirtualFrame frame) throws UnexpectedResultException {
        return getContext().getHistoryOperator().newExecutionLong(getNodeIdentifier(), frame, this::executeLong);
    }

    @Override
    public boolean isEqualNode(SLStatementNode that) {
        return false;
    }

    @Override
    protected boolean hasNewChildNode() {
        return true;
    }
}
