package com.oracle.truffle.sl.nodes.diffexec;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.runtime.SLNull;
import com.oracle.truffle.sl.runtime.diffexec.CalcResult;
import com.oracle.truffle.sl.runtime.diffexec.NodeIdentifier;

public final class DeleteNode extends SLExpressionNode {
    private final NodeIdentifier deleted;

    public DeleteNode(NodeIdentifier deleted) {
        this.deleted = deleted;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        final var op = getContext().getHistoryOperator();
        op.deleteHistory(deleted);
        return SLNull.SINGLETON;
    }

    @Override
    public CalcResult.Generic calcGenericInner(VirtualFrame frame) {
        return CalcResult.Generic.fresh(executeGeneric(frame));
    }

    @Override
    protected boolean hasNewChildNode() {
        return true;
    }
}
