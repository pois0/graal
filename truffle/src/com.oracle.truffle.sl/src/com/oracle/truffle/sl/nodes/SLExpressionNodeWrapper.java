package com.oracle.truffle.sl.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;

final class SLExpressionNodeWrapper extends SLExpressionNode implements InstrumentableNode.WrapperNode {

    @Child private SLExpressionNode delegateNode;
    @Child private ProbeNode probeNode;

    SLExpressionNodeWrapper(SLExpressionNode delegateNode, ProbeNode probeNode) {
        this.delegateNode = delegateNode;
        this.probeNode = probeNode;
    }

    @Override
    public SLExpressionNode getDelegateNode() {
        return delegateNode;
    }

    @Override
    public ProbeNode getProbeNode() {
        return probeNode;
    }

    @Override
    public NodeCost getCost() {
        return NodeCost.NONE;
    }

    @Override
    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        boolean returnValue;
        for (;;) {
            boolean wasOnReturnExecuted = false;
            try {
                try {
                    probeNode.onEnter(frame);
                    returnValue = delegateNode.executeBoolean(frame);
                    wasOnReturnExecuted = true;
                    probeNode.onReturnValue(frame, returnValue);
                    break;
                } catch (UnexpectedResultException e) {
                    wasOnReturnExecuted = true;
                    probeNode.onReturnValue(frame, e.getResult());
                    throw e;
                }
            } catch (Throwable t) {
                Object result = probeNode.onReturnExceptionalOrUnwind(frame, t, wasOnReturnExecuted);
                if (result == ProbeNode.UNWIND_ACTION_REENTER) {
                    continue;
                }
                if (result instanceof Boolean) {
                    returnValue = (boolean) result;
                    break;
                } else if (result != null) {
                    throw new UnexpectedResultException(result);
                }
                throw t;
            }
        }
        return returnValue;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object returnValue;
        for (;;) {
            boolean wasOnReturnExecuted = false;
            try {
                probeNode.onEnter(frame);
                returnValue = delegateNode.executeGeneric(frame);
                wasOnReturnExecuted = true;
                probeNode.onReturnValue(frame, returnValue);
                break;
            } catch (Throwable t) {
                Object result = probeNode.onReturnExceptionalOrUnwind(frame, t, wasOnReturnExecuted);
                if (result == ProbeNode.UNWIND_ACTION_REENTER) {
                    continue;
                } else if (result != null) {
                    returnValue = result;
                    break;
                }
                throw t;
            }
        }
        return returnValue;
    }

    @Override
    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        long returnValue;
        for (;;) {
            boolean wasOnReturnExecuted = false;
            try {
                try {
                    probeNode.onEnter(frame);
                    returnValue = delegateNode.executeLong(frame);
                    wasOnReturnExecuted = true;
                    probeNode.onReturnValue(frame, returnValue);
                    break;
                } catch (UnexpectedResultException e) {
                    wasOnReturnExecuted = true;
                    probeNode.onReturnValue(frame, e.getResult());
                    throw e;
                }
            } catch (Throwable t) {
                Object result = probeNode.onReturnExceptionalOrUnwind(frame, t, wasOnReturnExecuted);
                if (result == ProbeNode.UNWIND_ACTION_REENTER) {
                    continue;
                }
                if (result instanceof Long) {
                    returnValue = (long) result;
                    break;
                } else if (result != null) {
                    throw new UnexpectedResultException(result);
                }
                throw t;
            }
        }
        return returnValue;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        Object returnValue;
        for (;;) {
            boolean wasOnReturnExecuted = false;
            try {
                probeNode.onEnter(frame);
                returnValue = delegateNode.executeGeneric(frame);
                wasOnReturnExecuted = true;
                probeNode.onReturnValue(frame, returnValue);
                break;
            } catch (Throwable t) {
                Object result = probeNode.onReturnExceptionalOrUnwind(frame, t, wasOnReturnExecuted);
                if (result == ProbeNode.UNWIND_ACTION_REENTER) {
                    continue;
                } else if (result != null) {
                    break;
                }
                throw t;
            }
        }
    }

    @Override
    public boolean isEqualNode(SLStatementNode that) {
        return this.delegateNode.isEqualNode(that);
    }

    @Override
    protected boolean hasNewChildNode() {
        return this.delegateNode.hasNewNode();
    }

    @Override
    public Object calcGenericInner(VirtualFrame frame) {
        return this.delegateNode.calcGenericInner(frame);
    }

    @Override
    public NodeIdentifier getNodeIdentifier() {
        return delegateNode.getNodeIdentifier();
    }
}
