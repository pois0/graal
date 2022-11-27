package com.oracle.truffle.sl.nodes.cache;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;

public final class NewNode extends SLExpressionNode {
    @Child private SLExpressionNode delegateNode;

    public NewNode(SLExpressionNode delegateNode) {
        this.delegateNode = delegateNode;
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
        return null;
    }

    @Override
    public boolean isEqualNode(SLStatementNode that) {
        return false;
    }

    @Override
    protected boolean hasNewChildNode() {
        return false;
    }
}
