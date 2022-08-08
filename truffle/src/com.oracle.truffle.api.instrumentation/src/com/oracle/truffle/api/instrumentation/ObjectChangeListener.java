package com.oracle.truffle.api.instrumentation;

public interface ObjectChangeListener {
    void onFieldAssigned(ObjectChangeEvent e);
}
