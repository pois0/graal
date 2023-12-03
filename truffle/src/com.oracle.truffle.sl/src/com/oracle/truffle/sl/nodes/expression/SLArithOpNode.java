package com.oracle.truffle.sl.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.sl.nodes.SLBinaryNode;
import com.oracle.truffle.sl.runtime.diffexec.CalcResult;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

public abstract class SLArithOpNode extends SLBinaryNode {
    protected abstract long doLong(long left, long right) throws ArithmeticException;

    protected abstract Object calcOpApplication(Object left,
                                                InteropLibrary leftInterop,
                                                Object right,
                                                InteropLibrary rightInterop) throws UnsupportedMessageException;

    @Override
    public CalcResult.Generic calcGenericInner(VirtualFrame frame) {
        final var op = getContext().getHistoryOperator();

        final CalcResult.Generic wrappedLeft = op.calcGeneric(frame, getLeftNode());
        final Object left = wrappedLeft.getResult();
        final CalcResult.Generic wrappedRight = op.calcGeneric(frame, getRightNode());
        final Object right = wrappedRight.getResult();

        try {
            Object result = calcOpApplication(
                    left,
                    InteropLibrary.getUncached(left),
                    right,
                    InteropLibrary.getUncached(right)
            );

            return new CalcResult.Generic(result, wrappedLeft.isFresh() || wrappedRight.isFresh());
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

    @Override
    public final CalcResult.Long calcLongInner(VirtualFrame frame) {
        final var op = getContext().getHistoryOperator();

        final CalcResult.Long wrappedLeft = op.calcLong(frame, this, getLeftNode());
        final CalcResult.Long wrappedRight = op.calcLong(frame, this, getRightNode());
        return new CalcResult.Long(doLong(wrappedLeft.getResult(), wrappedRight.getResult()), wrappedLeft.isFresh() || wrappedRight.isFresh());
    }
}
