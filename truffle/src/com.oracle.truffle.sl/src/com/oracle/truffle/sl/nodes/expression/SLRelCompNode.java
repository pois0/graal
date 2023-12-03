package com.oracle.truffle.sl.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.nodes.SLBinaryNode;
import com.oracle.truffle.sl.runtime.SLBigInteger;
import com.oracle.truffle.sl.runtime.SLContext;
import com.oracle.truffle.sl.runtime.diffexec.CalcResult;
import com.oracle.truffle.sl.runtime.diffexec.ExecutionHistoryOperator;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

public abstract class SLRelCompNode extends SLBinaryNode {
    protected abstract boolean doLong(long left, long right);
    protected abstract boolean doSLBigInteger(SLBigInteger left, SLBigInteger right);

    @Override
    public CalcResult.Generic calcGenericInner(VirtualFrame frame) {
        return calcBooleanInner(frame).getGenericResult();
    }

    @Override
    public CalcResult.Boolean calcBooleanInner(VirtualFrame frame) {
        final var op = getContext().getHistoryOperator();

        final CalcResult.Generic wrappedLeft = op.calcGeneric(frame, getLeftNode());
        final Object left = wrappedLeft.getResult();
        final InteropLibrary leftInterop = InteropLibrary.getUncached(left);
        final CalcResult.Generic wrappedRight = op.calcGeneric(frame, getRightNode());
        final Object right = wrappedRight.getResult();
        final InteropLibrary rightInterop = InteropLibrary.getUncached(right);

        try {
            boolean result;
            if (leftInterop.fitsInLong(left) && rightInterop.fitsInLong(right)) {
                result = doLong(leftInterop.asLong(left), rightInterop.asLong(right));
            } else if (left instanceof SLBigInteger && right instanceof SLBigInteger) {
                result = doSLBigInteger((SLBigInteger) left, (SLBigInteger) right);
            } else {
                typeError(left, right);
                result = false;
            }
            return new CalcResult.Boolean(result, wrappedLeft.isFresh() || wrappedRight.isFresh());
        } catch (UnsupportedMessageException ex) {
            throw shouldNotReachHere(ex);
        }
    }

    private Object typeError(Object left, Object right) {
        throw SLException.typeError(this, left, right);
    }
}
