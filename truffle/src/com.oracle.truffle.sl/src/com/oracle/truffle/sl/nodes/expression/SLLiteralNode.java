package com.oracle.truffle.sl.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;

public abstract class SLLiteralNode extends SLExpressionNode {
    @Override
    protected boolean hasNewChildNode() {
        return false;
    }

    @Override
    public Object calcGenericInner(VirtualFrame frame) {
        throw new IllegalStateException("Unreachable: " + getSourceSection());
    }
}
