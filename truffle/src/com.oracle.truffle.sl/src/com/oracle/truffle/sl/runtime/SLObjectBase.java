package com.oracle.truffle.sl.runtime;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

public abstract class SLObjectBase extends DynamicObject implements TruffleObject {
    protected SLObjectBase(Shape shape) {
        super(shape);
    }
}
