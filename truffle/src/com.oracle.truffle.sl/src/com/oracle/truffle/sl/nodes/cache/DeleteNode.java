package com.oracle.truffle.sl.nodes.cache;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.SLNull;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;

public final class DeleteNode extends SLExpressionNode {
    private final NodeIdentifier from;
    private final NodeIdentifier to;

    public DeleteNode(NodeIdentifier from, NodeIdentifier to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return SLNull.SINGLETON;
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
