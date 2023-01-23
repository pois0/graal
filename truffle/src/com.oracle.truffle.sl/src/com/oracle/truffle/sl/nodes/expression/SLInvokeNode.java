/*
 * Copyright (c) 2012, 2020, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.runtime.SLContext;
import com.oracle.truffle.sl.runtime.SLFunction;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;
import com.oracle.truffle.sl.runtime.cache.ExecutionHistoryOperator;
import com.oracle.truffle.sl.runtime.cache.FunctionCallSpecialParameter;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;
import org.graalvm.collections.Pair;

/**
 * The node for function invocation in SL. Since SL has first class functions, the {@link SLFunction
 * target function} can be computed by an arbitrary expression. This node is responsible for
 * evaluating this expression, as well as evaluating the {@link #argumentNodes arguments}. The
 * actual invocation is delegated to a {@link InteropLibrary} instance.
 *
 * @see InteropLibrary#execute(Object, Object...)
 */
@NodeInfo(shortName = "invoke")
public final class SLInvokeNode extends SLExpressionNode {

    @Child private SLExpressionNode functionNode;
    @Children private final SLExpressionNode[] argumentNodes;
    @Child private InteropLibrary library;

    public SLInvokeNode(SLExpressionNode functionNode, SLExpressionNode[] argumentNodes) {
        this.functionNode = functionNode;
        this.argumentNodes = argumentNodes;
        this.library = InteropLibrary.getFactory().createDispatched(3);
    }

    @ExplodeLoop
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object function = functionNode.executeGeneric(frame);

        /*
         * The number of arguments is constant for one invoke node. During compilation, the loop is
         * unrolled and the execute methods of all arguments are inlined. This is triggered by the
         * ExplodeLoop annotation on the method. The compiler assertion below illustrates that the
         * array length is really constant.
         */
        CompilerAsserts.compilationConstant(argumentNodes.length);

        final int argumentLength = argumentNodes.length;
        Object[] argumentValues = new Object[argumentLength];
        for (int i = 0; i < argumentNodes.length; i++) {
            argumentValues[i] = argumentNodes[i].executeGeneric(frame);
        }

        try {
            getContext().getHistoryOperator().onEnterFunction(getNodeIdentifier(), ((SLFunction) function).getName(), argumentLength, false);
            return library.execute(function, argumentValues);
        } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
            /* Execute was not successful. */
            throw SLUndefinedNameException.undefinedFunction(this, function);
        } finally {
            getContext().getHistoryOperator().onExitFunction(getNodeIdentifier());
        }
    }

    @ExplodeLoop
    @Override
    public Object calcGenericInner(VirtualFrame frame) {
        final SLContext context = getContext();
        final ExecutionHistoryOperator op = context.getHistoryOperator();
        final NodeIdentifier identifier = getNodeIdentifier();
        final Pair<Object, Boolean> functionPair = op.calcGenericParameter(frame, functionNode);
        final Object function = functionPair.getLeft();
        boolean shouldRecalc = functionPair.getRight();

        /*
         * The number of arguments is constant for one invoke node. During compilation, the loop is
         * unrolled and the execute methods of all arguments are inlined. This is triggered by the
         * ExplodeLoop annotation on the method. The compiler assertion below illustrates that the
         * array length is really constant.
         */
        CompilerAsserts.compilationConstant(argumentNodes.length);

        final int argumentLength = argumentNodes.length;
        Object[] argumentValues = new Object[argumentLength + 1];
        boolean[] argumentFlags = new boolean[argumentLength];
        argumentValues[argumentLength] = FunctionCallSpecialParameter.CALC;
        for (int i = 0; i < argumentNodes.length; i++) {
            final Pair<Object, Boolean> parameter = op.calcGenericParameter(frame, argumentNodes[i]);
            argumentValues[i] = parameter.getLeft();
            argumentFlags[i] = parameter.getRight();
            shouldRecalc |= argumentFlags[i];
        }

        if (!shouldRecalc && !context.getHistoryOperator().checkContainsNewNodeInFunctionCalls(getNodeIdentifier())) {
            final Object returnedValueOrThrow = op.getReturnedValueOrThrow(identifier);
            return returnedValueOrThrow;
        }

        op.onEnterFunction(identifier, ((SLFunction) function).getName(), argumentLength, true);
        try {
            op.pushArgumentFlags(argumentFlags);
            return library.execute(function, argumentValues);
        } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
            /* Execute was not successful. */
            throw SLUndefinedNameException.undefinedFunction(this, function);
        } finally {
            op.onExitFunction(identifier);
            op.popArgumentFlags();
        }
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.CallTag.class) {
            return true;
        }
        return super.hasTag(tag);
    }

    @Override
    protected boolean hasNewChildNode() {
        if (functionNode.hasNewNode()) return true;
        for (SLExpressionNode argNode : argumentNodes) {
            if (argNode.hasNewNode()) return true;
        }
        return false;
    }
}
