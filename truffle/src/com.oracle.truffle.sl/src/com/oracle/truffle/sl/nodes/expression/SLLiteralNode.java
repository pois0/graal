package com.oracle.truffle.sl.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.runtime.cache.ResultAndStrategy;

public abstract class SLLiteralNode extends SLExpressionNode {
    @Override
    protected boolean hasNewChildNode() {
        return false;
    }

    @Override
    public ResultAndStrategy.Generic<Object> calcGenericInner(VirtualFrame frame) {
        throw new IllegalStateException("Unreachable: " + getSourceSection());
    }
}
