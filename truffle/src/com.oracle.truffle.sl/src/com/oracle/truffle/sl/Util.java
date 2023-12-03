package com.oracle.truffle.sl;

public final class Util {
    public static <T> T assertNonNull(T object) {
        assert object != null;
        return object;
    }

    public static <T> T assertNonNull(T object, String errorMessage) {
        assert object != null : errorMessage;
        return object;
    }
}
