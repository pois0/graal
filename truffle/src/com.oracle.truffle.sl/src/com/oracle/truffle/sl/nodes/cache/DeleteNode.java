package com.oracle.truffle.sl.nodes.cache;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.SLNull;
import com.oracle.truffle.sl.runtime.cache.ExecutionHistoryOperator;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;

public final class DeleteNode extends SLExpressionNode {
    private final NodeIdentifier deleted;

    public DeleteNode(NodeIdentifier deleted) {
        this.deleted = deleted;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return SLNull.SINGLETON;
    }

    @Override
    public Object calcGenericInner(VirtualFrame frame) {
        final ExecutionHistoryOperator op = getContext().getHistoryOperator();
        op.deleteHistory(deleted);
        return executeGeneric(frame);
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
