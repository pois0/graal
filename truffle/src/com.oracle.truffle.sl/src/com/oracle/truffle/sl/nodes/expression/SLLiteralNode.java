package com.oracle.truffle.sl.nodes.expression;

import com.oracle.truffle.sl.nodes.SLExpressionNode;

public abstract class SLLiteralNode extends SLExpressionNode {
    @Override
    protected boolean hasNewChildNode() {
        return false;
    }
}
