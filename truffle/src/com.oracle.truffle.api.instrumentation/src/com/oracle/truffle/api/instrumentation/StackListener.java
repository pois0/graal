package com.oracle.truffle.api.instrumentation;

import com.oracle.truffle.api.frame.FrameSlot;

public interface StackListener {
    public void onObjectSet(FrameSlot slot, Object value);

    public void onBooleanSet(FrameSlot slot, boolean value);

    public void onByteSet(FrameSlot slot, byte value);

    public void onIntSet(FrameSlot slot, int value);

    public void onLongSet(FrameSlot slot, long value);

    public void onFloatSet(FrameSlot slot, float value);

    public void onDoubleSet(FrameSlot slot, double value);
}
