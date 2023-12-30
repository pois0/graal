package com.oracle.truffle.sl.runtime.diffexec;

public record ObjectUpdate<TIME extends Time<TIME>>(TIME objectGenCtx, String fieldName, Object newValue) {
}
