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
import com.oracle.truffle.sl.runtime.diffexec.ExecutionHistoryOperator;
import com.oracle.truffle.sl.runtime.diffexec.NodeIdentifier;
import com.oracle.truffle.sl.runtime.diffexec.SLDEObject;

/**
 * The node for reading a property of an object. When executed, this node:
 * <ol>
 * <li>evaluates the object expression on the left hand side of the object access operator</li>
 * <li>evaluated the property name</li>
 * <li>reads the named property</li>
 * </ol>
 */
@NodeInfo(shortName = ".")
@NodeChild("receiverNode")
@NodeChild("nameNode")
public abstract class SLReadPropertyNode extends SLExpressionNode {

    static final int LIBRARY_LIMIT = 3;

    protected abstract SLExpressionNode getReceiverNode();
    protected abstract SLExpressionNode getNameNode();

    @Specialization(guards = "arrays.hasArrayElements(receiver)", limit = "LIBRARY_LIMIT")
    protected Object readArray(Object receiver, Object index,
                    @CachedLibrary("receiver") InteropLibrary arrays,
                    @CachedLibrary("index") InteropLibrary numbers) {
        try {
            long i = numbers.asLong(index);
            Object result = arrays.readArrayElement(receiver, i);
            getContext().getHistoryOperator().onReadArrayElement(receiver, i);
            return result;
        } catch (UnsupportedMessageException | InvalidArrayIndexException e) {
            // read was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(this, index);
        }
    }

    @Specialization(limit = "LIBRARY_LIMIT")
    protected static Object readSLObject(SLObjectBase receiver, Object name,
                    @Bind("this") Node node,
                    @CachedLibrary("receiver") DynamicObjectLibrary objectLibrary,
                    @Cached SLToTruffleStringNode toTruffleStringNode) {
        TruffleString nameTS = toTruffleStringNode.execute(node, name);
        Object result;
        if (receiver instanceof SLObject) {
            result = objectLibrary.getOrDefault(receiver, nameTS, null);
        } else {
            try {
                result = ((SLDEObject) receiver).readMember(nameTS.toJavaStringUncached(), nameTS, DynamicObjectLibrary.getUncached());
            } catch (UnknownIdentifierException e) {
                throw SLUndefinedNameException.undefinedProperty(node, nameTS);
            }
        }
        if (result == null) {
            // read was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(node, nameTS);
        }
        SLContext.get(node).getHistoryOperator().onReadObjectField(receiver, nameTS.toString());
        return result;
    }

    @Specialization(guards = {"!isSLObject(receiver)", "objects.hasMembers(receiver)"}, limit = "LIBRARY_LIMIT")
    protected static Object readObject(Object receiver, Object name,
                    @Bind("this") Node node,
                    @CachedLibrary("receiver") InteropLibrary objects,
                    @Cached SLToMemberNode asMember) {
        try {
            String nameS = asMember.execute(node, name);
            Object result = objects.readMember(receiver, nameS);
            SLContext.get(node).getHistoryOperator().onReadObjectField(receiver, nameS);
            return result;
        } catch (UnsupportedMessageException | UnknownIdentifierException e) {
            // read was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(node, name);
        }
    }

    @Override
    public CalcResult.Generic calcGenericInner(VirtualFrame frame) {
        final var op = getContext().getHistoryOperator();

        final CalcResult.Generic receiverWrapped = op.calcGeneric(frame, getReceiverNode());
        Object receiver = receiverWrapped.getResult();
        final CalcResult.Generic fldNameWrapped = op.calcGeneric(frame, getNameNode());
        Object name = fldNameWrapped.getResult();

        Object result = op.getObjectFieldValue(this, receiver, name.toString(), getNodeIdentifier());
        return new CalcResult.Generic(result, receiverWrapped.isFresh() || fldNameWrapped.isFresh());
    }

    @Override
    protected boolean hasNewChildNode() {
        return getReceiverNode().hasNewNode() || getNameNode().hasNewNode();
    }

    static boolean isSLObject(Object receiver) {
        return receiver instanceof SLObjectBase;
    }
}
