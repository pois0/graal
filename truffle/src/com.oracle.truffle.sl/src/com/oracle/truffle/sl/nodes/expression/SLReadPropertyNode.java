/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.nodes.util.SLToMemberNode;
import com.oracle.truffle.sl.runtime.SLContext;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;
import com.oracle.truffle.sl.runtime.cache.ExecutionHistoryOperator;
import com.oracle.truffle.sl.runtime.cache.NodeIdentifier;

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
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY = LibraryFactory.resolve(InteropLibrary.class);

    static final int LIBRARY_LIMIT = 3;

    protected abstract SLExpressionNode getReceiverNode();
    protected abstract SLExpressionNode getNameNode();

    @Specialization(guards = "arrays.hasArrayElements(receiver)", limit = "LIBRARY_LIMIT")
    protected Object readArray(Object receiver, Object index,
                    @CachedLibrary("receiver") InteropLibrary arrays,
                    @CachedLibrary("index") InteropLibrary numbers) {
        try {
            final long id = numbers.asLong(index);
            Object result = arrays.readArrayElement(receiver, id);
            context.getHistoryOperator().onReadObjectField(receiver, id);
            return result;
        } catch (UnsupportedMessageException | InvalidArrayIndexException e) {
            // read was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(this, index);
        }
    }

    @Specialization(guards = "objects.hasMembers(receiver)", limit = "LIBRARY_LIMIT")
    protected Object readObject(Object receiver, Object name,
                    @CachedLibrary("receiver") InteropLibrary objects,
                    @Cached SLToMemberNode asMember) {
        try {
            final String field = asMember.execute(name);
            Object result = objects.readMember(receiver, field);
            context.getHistoryOperator().onReadObjectField(receiver, field);
            return result;
        } catch (UnsupportedMessageException | UnknownIdentifierException e) {
            // read was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(this, name);
        }
    }

    @Override
    public Object calcGeneric(VirtualFrame frame) {
        final ExecutionHistoryOperator op = context.getHistoryOperator();
        final NodeIdentifier identifier = getNodeIdentifier();
        if (isNewNode()) {
            op.startNewExecution(identifier);
            try {
                executeGeneric(frame);
            } finally {
                op.endNewExecution(identifier);
            }
        }

        return executeGeneric(frame);
    }

    @Override
    public boolean isEqualNode(SLStatementNode that) {
        if (!(that instanceof SLReadPropertyNode)) return false;
        final SLReadPropertyNode thatRP = (SLReadPropertyNode) that;
        return getReceiverNode().isEqualNode(thatRP.getReceiverNode())
                && getNameNode().isEqualNode(thatRP.getNameNode());
    }

    @Override
    protected boolean hasNewChildNode() {
        return getReceiverNode().hasNewNode() || getNameNode().hasNewNode();
    }
}
