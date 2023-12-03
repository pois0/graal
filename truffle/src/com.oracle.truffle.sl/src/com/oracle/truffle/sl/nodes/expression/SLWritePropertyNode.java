/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.util.SLToMemberNode;
import com.oracle.truffle.sl.nodes.util.SLToTruffleStringNode;
import com.oracle.truffle.sl.runtime.SLContext;
import com.oracle.truffle.sl.runtime.SLObject;
import com.oracle.truffle.sl.runtime.SLObjectBase;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;
import com.oracle.truffle.sl.runtime.diffexec.CalcResult;
import com.oracle.truffle.sl.runtime.diffexec.NodeIdentifier;

/**
 * The node for writing a property of an object. When executed, this node:
 * <ol>
 * <li>evaluates the object expression on the left hand side of the object access operator</li>
 * <li>evaluates the property name</li>
 * <li>evaluates the value expression on the right hand side of the assignment operator</li>
 * <li>writes the named property</li>
 * <li>returns the written value</li>
 * </ol>
 */
@NodeInfo(shortName = ".=")
@NodeChild("receiverNode")
@NodeChild("nameNode")
@NodeChild("valueNode")
public abstract class SLWritePropertyNode extends SLExpressionNode {

    static final int LIBRARY_LIMIT = 3;

    protected abstract SLExpressionNode getReceiverNode();
    protected abstract SLExpressionNode getNameNode();
    protected abstract SLExpressionNode getValueNode();

    @Specialization(guards = "arrays.hasArrayElements(receiver)", limit = "LIBRARY_LIMIT")
    protected Object writeArray(Object receiver, Object index, Object value,
                    @CachedLibrary("receiver") InteropLibrary arrays,
                    @CachedLibrary("index") InteropLibrary numbers) {
        try {
            long i = numbers.asLong(index);
            arrays.writeArrayElement(receiver, i, value);
            getContext().getHistoryOperator().onUpdateArrayElement(receiver, i, value);
        } catch (UnsupportedMessageException | UnsupportedTypeException | InvalidArrayIndexException e) {
            // read was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(this, index);
        }
        return value;
    }

    @Specialization(limit = "LIBRARY_LIMIT")
    protected static Object writeSLObject(SLObjectBase receiver, Object name, Object value,
                    @Bind("this") Node node,
                    @CachedLibrary("receiver") DynamicObjectLibrary objectLibrary,
                    @Cached SLToTruffleStringNode toTruffleStringNode) {
        TruffleString nameTS = toTruffleStringNode.execute(node, name);
        objectLibrary.put(receiver, nameTS, value);
        SLContext.get(node).getHistoryOperator().onUpdateObjectField(receiver, nameTS.toJavaStringUncached(), value);
        return value;
    }

    @Specialization(guards = "!isSLObject(receiver)", limit = "LIBRARY_LIMIT")
    protected static Object writeObject(Object receiver, Object name, Object value,
                    @Bind("this") Node node,
                    @CachedLibrary("receiver") InteropLibrary objectLibrary,
                    @Cached SLToMemberNode asMember) {
        try {
            String nameS = asMember.execute(node, name);
            objectLibrary.writeMember(receiver, nameS, value);
            SLContext.get(node).getHistoryOperator().onUpdateObjectField(receiver, nameS, value);
        } catch (UnsupportedMessageException | UnknownIdentifierException | UnsupportedTypeException e) {
            // write was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(node, name);
        }
        return value;
    }

    @Override
    public CalcResult.Generic calcGenericInner(VirtualFrame frame) {
        final var op = getContext().getHistoryOperator();
        final NodeIdentifier identifier = getNodeIdentifier();
        final CalcResult.Generic receiverWrapped = op.calcGeneric(frame, getReceiverNode());
        final Object receiver = receiverWrapped.getResult();
        final CalcResult.Generic nameWrapped = op.calcGeneric(frame, getNameNode());
        final CalcResult.Generic valueWrapped = op.calcGeneric(frame, getValueNode());
        final Object value = valueWrapped.getResult();

        if (receiverWrapped.isFresh() || nameWrapped.isFresh() || valueWrapped.isFresh()) {
            final Object name = nameWrapped.getResult();
//            final InteropLibrary nameOp = InteropLibrary.getUncached(name);
//            InteropLibrary receiverOp = InteropLibrary.getUncached(receiver);
//            try {
//                if (receiverOp.hasArrayElements(receiver)) {
//                    receiverOp.writeArrayElement(receiver, nameOp.asLong(name), value);
//                } else {
//                    receiverOp.writeMember(receiver, nameOp.asString(name), value);
//                }
//            } catch (UnsupportedMessageException | UnknownIdentifierException | UnsupportedTypeException | InvalidArrayIndexException e) {
//                // write was not successful. In SL we only have basic support for errors.
//                throw SLUndefinedNameException.undefinedProperty(this, name);
//            }
            op.rewriteObjectField(receiver, name.toString(), value, identifier, receiverWrapped.isFresh() || nameWrapped.isFresh());
            return CalcResult.Generic.fresh(value);
        }

        return CalcResult.Generic.cached(value);
    }

    @Override
    protected boolean hasNewChildNode() {
        return getReceiverNode().hasNewNode() || getNameNode().hasNewNode() || getValueNode().hasNewNode();
    }

    static boolean isSLObject(Object receiver) {
        return receiver instanceof SLObjectBase;
    }
}
