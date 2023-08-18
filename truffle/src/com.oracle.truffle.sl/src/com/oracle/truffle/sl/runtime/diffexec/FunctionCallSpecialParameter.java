package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.interop.TruffleObject;

public abstract sealed class FunctionCallSpecialParameter implements TruffleObject {
    public static final FunctionCallSpecialParameter CALC = new Calc();
    public static final FunctionCallSpecialParameter EXEC = new Exec();

    private static final class Calc extends FunctionCallSpecialParameter {}
    private static final class Exec extends FunctionCallSpecialParameter {}
}
