package com.oracle.truffle.api.instrumentation;

public class ObjectChangeEvent {
    private final Object object;
    private final Object key;
    private final Object value;

    ObjectChangeEvent(Object object, Object key, Object value) {
        this.object = object;
        this.key = key;
        this.value = value;
    }

    public Object getObject() {
        return object;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
