package com.oracle.truffle.sl.runtime;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

public abstract class SLObjectBase extends DynamicObject implements TruffleObject {
    private final Object objGenTime;
    protected SLObjectBase(Shape shape, Object objGenTime) {
        super(shape);
        this.objGenTime = objGenTime;
    }

    public Object getObjGenTime() {
        return objGenTime;
    }
}
