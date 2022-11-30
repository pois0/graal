package com.oracle.truffle.sl.runtime.cache;

@SuppressWarnings("InstantiationOfUtilityClass")
public class FunctionCallSpecialParameter {
    public static final FunctionCallSpecialParameter CALC = new FunctionCallSpecialParameter();
    public static final FunctionCallSpecialParameter EXEC = new FunctionCallSpecialParameter();

    private FunctionCallSpecialParameter() {
    }
}
