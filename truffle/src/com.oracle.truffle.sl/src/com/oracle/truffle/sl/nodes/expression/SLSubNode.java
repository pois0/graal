/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.sl.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.nodes.SLBinaryNode;
import com.oracle.truffle.sl.runtime.SLBigNumber;
import com.oracle.truffle.sl.runtime.cache.ExecutionHistoryOperator;
import com.oracle.truffle.sl.runtime.cache.ResultAndStrategy;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

/**
 * This class is similar to the extensively documented {@link SLAddNode}.
 */
@NodeInfo(shortName = "-")
public abstract class SLSubNode extends SLBinaryNode {

    @Specialization(rewriteOn = ArithmeticException.class)
    protected long sub(long left, long right) {
        return Math.subtractExact(left, right);
    }

    @Specialization
    @TruffleBoundary
    protected SLBigNumber sub(SLBigNumber left, SLBigNumber right) {
        return new SLBigNumber(left.getValue().subtract(right.getValue()));
    }

    @Override
    public ResultAndStrategy.Generic<Object> calcGenericInner(VirtualFrame frame) {
        final ExecutionHistoryOperator op = getContext().getHistoryOperator();

        final ResultAndStrategy.Generic<Object> wrappedLeft = op.calcGeneric(frame, getLeftNode());
        final Object left = wrappedLeft.getResult();
        final InteropLibrary leftInterop = INTEROP_LIBRARY.getUncached(left);
        final ResultAndStrategy.Generic<Object> wrappedRight = op.calcGeneric(frame, getRightNode());
        final Object right = wrappedRight.getResult();
        final InteropLibrary rightInterop = INTEROP_LIBRARY.getUncached(right);

        try {
            Object result;
            if (leftInterop.fitsInLong(left) && rightInterop.fitsInLong(right)) {
                result = sub(leftInterop.asLong(left), rightInterop.asLong(right));
            } else if (left instanceof SLBigNumber && right instanceof SLBigNumber) {
                result = sub((SLBigNumber) left, (SLBigNumber) right);
            } else {
                result = 0;
            }
            return new ResultAndStrategy.Generic<>(result, wrappedLeft.isFresh() || wrappedRight.isFresh());
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

    @Override
    public ResultAndStrategy.Long calcLongInner(VirtualFrame frame) throws UnexpectedResultException {
        final ExecutionHistoryOperator op = getContext().getHistoryOperator();

        final ResultAndStrategy.Long wrappedLeft = op.calcLong(frame, this, getLeftNode());
        final long left = wrappedLeft.getResult();
        final ResultAndStrategy.Long wrappedRight = op.calcLong(frame, this, getRightNode());
        final long right = wrappedRight.getResult();
        return new ResultAndStrategy.Long(sub(left, right), wrappedLeft.isFresh() || wrappedRight.isFresh());
    }

    @Fallback
    protected Object typeError(Object left, Object right) {
        throw SLException.typeError(this, left, right);
    }

}
