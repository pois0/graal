package com.oracle.truffle.sl.nodes.cache;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;

public final class ReplaceNode extends SLExpressionNode {
    @Child private SLExpressionNode delegateNode;
    private final NodeIdentifier from;
    private final NodeIdentifier to;

    public ReplaceNode(SLExpressionNode delegateNode, NodeIdentifier from, NodeIdentifier to) {
        this.delegateNode = delegateNode;
        this.from = from;
        this.to = to;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return null;
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
