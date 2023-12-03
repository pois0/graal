package com.oracle.truffle.sl.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.runtime.diffexec.CalcResult;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

public abstract class SLLiteralNode extends SLExpressionNode {
    @Override
    protected boolean hasNewChildNode() {
        return false;
    }

    @Override
    public CalcResult.Generic calcGenericInner(VirtualFrame frame) {
        throw shouldNotReachHere();
    }
}
