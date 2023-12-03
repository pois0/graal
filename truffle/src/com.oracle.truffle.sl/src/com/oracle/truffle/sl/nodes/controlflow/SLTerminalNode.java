package com.oracle.truffle.sl.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLStatementNode;

public abstract class SLTerminalNode extends SLStatementNode {
    @Override
    public void calcVoidInner(VirtualFrame frame) {
        executeVoid(frame);
    }

    @Override
    protected boolean hasNewChildNode() {
        return false;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void handleAsReplaced(int i) {
        throw new UnsupportedOperationException();
    }
}
