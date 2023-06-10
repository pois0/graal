package com.oracle.truffle.sl.nodes.cache;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.runtime.SLNull;
import com.oracle.truffle.sl.runtime.cache.ExecutionHistoryOperator;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;
import com.oracle.truffle.sl.runtime.cache.ResultAndStrategy;

public final class DeleteNode extends SLExpressionNode {
    private final NodeIdentifier deleted;

    public DeleteNode(NodeIdentifier deleted) {
        this.deleted = deleted;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        final ExecutionHistoryOperator op = getContext().getHistoryOperator();
        op.deleteHistory(deleted);
        return SLNull.SINGLETON;
    }

    @Override
    public ResultAndStrategy.Generic<Object> calcGenericInner(VirtualFrame frame) {
        return ResultAndStrategy.Generic.fresh(executeGeneric(frame));
    }

    @Override
    protected boolean hasNewChildNode() {
        return true;
    }
}
