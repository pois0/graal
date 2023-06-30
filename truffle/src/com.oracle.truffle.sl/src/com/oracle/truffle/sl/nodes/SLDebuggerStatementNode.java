package com.oracle.truffle.sl.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;
import org.graalvm.polyglot.impl.AbstractPolyglotImpl;

public final class SLDebuggerStatementNode extends SLStatementNode {
    public static int count = 0;

    @Child private SLStatementNode delegateNode;

    public SLDebuggerStatementNode(SLStatementNode delegateNode) {
        this.delegateNode = delegateNode;
    }

    public NodeCost getCost() {
        return NodeCost.NONE;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        System.out.println("ExecuteVoid!");
        AbstractPolyglotImpl.testCount++;
        delegateNode.executeVoid(frame);
    }

    @Override
    public void calcVoidInner(VirtualFrame frame) {
        System.out.println("calcVoid!!");
        AbstractPolyglotImpl.testCount++;
        this.delegateNode.calcVoidInner(frame);
    }

    @Override
    protected boolean hasNewChildNode() {
        return this.delegateNode.hasNewNode();
    }

    @Override
    public NodeIdentifier getNodeIdentifier() {
        return delegateNode.getNodeIdentifier();
    }

    @Override
    public SLStatementNode unwrap() {
        return delegateNode;
    }

    @Override
    public int getSize() {
        return delegateNode.getSize();
    }

    @Override
    public void handleAsReplaced(int i) {
        delegateNode.handleAsReplaced(i);
    }
}
