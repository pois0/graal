package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.utilities.TriState;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.runtime.Keys;
import com.oracle.truffle.sl.runtime.SLObjectBase;

import java.util.Arrays;

@ExportLibrary(InteropLibrary.class)
public class SLDEObject extends SLObjectBase {
    private static final Shape SHAPE = Shape.newBuilder().layout(SLDEObject.class).build();

    final ObjectHistory oh;

    SLDEObject(ObjectHistory oh, Object objGenTime) {
        super(SHAPE, objGenTime);
        this.oh = oh;
    }

    @ExportMessage
    boolean hasLanguage() {
        return true;
    }

    @ExportMessage
    Class<? extends TruffleLanguage<?>> getLanguage() {
        return SLLanguage.class;
    }

    @ExportMessage
    @SuppressWarnings("unused")
    static final class IsIdenticalOrUndefined {
        @Specialization
        static TriState doSLDEObject(SLDEObject receiver, SLDEObject other) {
            return TriState.valueOf(receiver == other);
        }

        @Fallback
        static TriState doOther(SLDEObject receiver, Object other) {
            return TriState.UNDEFINED;
        }
    }

    @ExportMessage
    @CompilerDirectives.TruffleBoundary
    int identityHashCode() {
        return System.identityHashCode(this);
    }

    @ExportMessage
    @CompilerDirectives.TruffleBoundary
    Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
        return "DEObject";
    }

    @ExportMessage
    boolean hasMembers() {
        return true;
    }

    @ExportMessage
    void removeMember(String member) {
        throw new UnsupportedOperationException();
    }

    @ExportMessage
    Object getMembers(@SuppressWarnings("unused") boolean includeInternal,
                      @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {

        Object[] keyArray = objectLibrary.getKeyArray(this);
        System.out.println("Key");
        if (keyArray.length != 0) System.out.println(keyArray[0].getClass());
        System.out.println(Arrays.toString(keyArray));
        return new Keys(keyArray); // TODO
    }

    @ExportMessage(name = "isMemberReadable")
    @ExportMessage(name = "isMemberModifiable")
    @ExportMessage(name = "isMemberRemovable")
    boolean existsMember(String member,
                         @Cached @Cached.Shared("fromJavaStringNode") TruffleString.FromJavaStringNode fromJavaStringNode,
                         @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
        return objectLibrary.containsKey(this, fromJavaStringNode.execute(member, SLLanguage.STRING_ENCODING))
                || oh.existsField(member);
    }

    @ExportMessage
    boolean isMemberInsertable(String member,
                               @CachedLibrary("this") InteropLibrary receivers) {
        return !receivers.isMemberExisting(this, member) || !oh.existsField(member);
    }

    /**
     * {@link DynamicObjectLibrary} provides the polymorphic inline cache for reading properties.
     */
    @ExportMessage
    Object readMember(String name,
                      @Cached @Cached.Shared("fromJavaStringNode") TruffleString.FromJavaStringNode fromJavaStringNode,
                      @CachedLibrary("this") DynamicObjectLibrary objectLibrary) throws UnknownIdentifierException {
        TruffleString truffleName = fromJavaStringNode.execute(name, SLLanguage.STRING_ENCODING);
        return readMember(name, truffleName, objectLibrary);
    }

    @ExportMessage.Ignore
    public Object readMember(String name, TruffleString truffleName, DynamicObjectLibrary objectLibrary) throws UnknownIdentifierException {
        Object result;
        if (oh.canUseCache(name)) {
            result = objectLibrary.getOrDefault(this, truffleName, null);
            if (result == null) throw new RuntimeException("Expected to be Unreachable");
            return result;
        }

        /* Property does not exist. */
        result = oh.getObjectFieldValue(name);
        if (result == null) throw UnknownIdentifierException.create(name);
        objectLibrary.put(this, truffleName, result);
        oh.onWrite(name);
        return result;
    }

    /**
     * {@link DynamicObjectLibrary} provides the polymorphic inline cache for writing properties.
     */
    @ExportMessage
    void writeMember(String name, Object value,
                     @Cached @Cached.Shared("fromJavaStringNode") TruffleString.FromJavaStringNode fromJavaStringNode,
                     @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
        objectLibrary.put(this, fromJavaStringNode.execute(name, SLLanguage.STRING_ENCODING), value);
        oh.onWrite(name);
    }

    public interface ObjectHistory {

        Object getObjectFieldValue(String name);

        boolean existsField(String member);

        boolean canUseCache(String member);

        void onWrite(String member);
    }
}
