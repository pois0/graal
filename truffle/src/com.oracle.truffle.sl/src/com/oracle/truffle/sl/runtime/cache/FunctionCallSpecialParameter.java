package com.oracle.truffle.sl.runtime.cache;

import com.oracle.truffle.api.interop.TruffleObject;

@SuppressWarnings("InstantiationOfUtilityClass")
public class FunctionCallSpecialParameter implements TruffleObject {
    public static final FunctionCallSpecialParameter CALC = new FunctionCallSpecialParameter();
    public static final FunctionCallSpecialParameter EXEC = new FunctionCallSpecialParameter();

    private FunctionCallSpecialParameter() {
    }
}
