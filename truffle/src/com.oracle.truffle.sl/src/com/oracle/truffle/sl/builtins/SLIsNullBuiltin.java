/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.sl.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.cache.ExecutionHistoryOperator;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;

/**
 * Built-in function that queries if the foreign object is a null value. See
 * <link>Messages.IS_NULL</link>.
 */
@NodeInfo(shortName = "isNull")
public abstract class SLIsNullBuiltin extends SLBuiltinNode {

    @Specialization(limit = "3")
    public boolean isExecutable(Object obj, @CachedLibrary("obj") InteropLibrary values) {
        return values.isNull(obj);
    }

    @Override
    public Object calcGenericInner(VirtualFrame frame) {
        return calcBooleanInner(frame);
    }

    @Override
    public boolean calcBooleanInner(VirtualFrame frame) {
        final ExecutionHistoryOperator op = context.getHistoryOperator();
        final NodeIdentifier identifier = getNodeIdentifier();
        if (isNewNode()) {
            op.startNewExecution(frame, identifier);
            try {
                return executeBoolean(frame);
            } catch (UnexpectedResultException e) {
                throw new RuntimeException(e);
            } finally {
                op.endNewExecution(identifier);
            }
        }

        final SLExpressionNode arg = getArguments()[0];
        final Object o = op.calcGeneric(frame, arg);

        return INTEROP_LIBRARY.getUncached(o).isNull(o);
    }

    @Override
    public boolean isEqualNode(SLStatementNode that) {
        if (!(that instanceof SLIsNullBuiltin)) return false;
        return getArguments()[0].isEqualNode(((SLIsNullBuiltin) that).getArguments()[0]);
    }
}
