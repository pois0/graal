package com.oracle.truffle.api.instrumentation;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.LanguageInfo;

import java.util.Arrays;

public class StackTracker {
    @CompilerDirectives.CompilationFinal(dimensions = 1) private volatile StackListener[] listeners = null;

    final LanguageInfo language;

    public StackTracker(LanguageInfo language) {
        this.language = language;
    }

    public void addListener(StackListener l) {
        CompilerAsserts.neverPartOfCompilation();
        synchronized (this) {
            if (listeners == null) {
                listeners = new StackListener[]{l};
            } else {
                int i = listeners.length;
                StackListener[] newListeners = Arrays.copyOf(listeners, i + 1);
                newListeners[i] = l;
                listeners = newListeners;
            }
        }
    }

    public void removeListener(StackListener l) {
        CompilerAsserts.neverPartOfCompilation();
        synchronized (this) {
            final int len = listeners.length;
            if (len == 1) {
                if (listeners[0] == l) {
                    listeners = null;
                }
            } else {
                for (int i = 0; i < len; i++) {
                    if (listeners[i] == l) {
                        if (i == (len - 1)) {
                            listeners = Arrays.copyOf(listeners, i);
                        } else if (i == 0) {
                            listeners = Arrays.copyOfRange(listeners, 1, len);
                        } else {
                            StackListener[] newListeners = new StackListener[len - 1];
                            System.arraycopy(listeners, 0, newListeners, 0, i);
                            System.arraycopy(listeners, i + 1, newListeners, i, len - i - 1);
                            listeners = newListeners;
                        }
                        break;
                    }
                }
            }
        }
    }

    @ExplodeLoop
    public void notifyBooleanSet(FrameSlot slot, boolean value) {
        CompilerAsserts.partialEvaluationConstant(this);

        if (listeners != null) {
            StackListener[] ls = listeners;
            for (StackListener listener : ls) {
                listener.onBooleanSet(slot, value);
            }
        }
    }

    @ExplodeLoop
    public void notifyByteSet(FrameSlot slot, byte value) {
        CompilerAsserts.partialEvaluationConstant(this);

        if (listeners != null) {
            StackListener[] ls = listeners;
            for (StackListener listener : ls) {
                listener.onByteSet(slot, value);
            }
        }
    }

    @ExplodeLoop
    public void notifyIntSet(FrameSlot slot, int value) {
        CompilerAsserts.partialEvaluationConstant(this);

        if (listeners != null) {
            StackListener[] ls = listeners;
            for (StackListener listener : ls) {
                listener.onIntSet(slot, value);
            }
        }
    }

    @ExplodeLoop
    public void notifyLongSet(FrameSlot slot, long value) {
        CompilerAsserts.partialEvaluationConstant(this);

        if (listeners != null) {
            StackListener[] ls = listeners;
            for (StackListener listener : ls) {
                listener.onLongSet(slot, value);
            }
        }
    }

    @ExplodeLoop
    public void notifyFloatSet(FrameSlot slot, float value) {
        CompilerAsserts.partialEvaluationConstant(this);

        if (listeners != null) {
            StackListener[] ls = listeners;
            for (StackListener listener : ls) {
                listener.onFloatSet(slot, value);
            }
        }
    }

    @ExplodeLoop
    public void notifyDoubleSet(FrameSlot slot, double value) {
        CompilerAsserts.partialEvaluationConstant(this);

        if (listeners != null) {
            StackListener[] ls = listeners;
            for (StackListener listener : ls) {
                listener.onDoubleSet(slot, value);
            }
        }
    }

    @ExplodeLoop
    public void notifyObjectSet(FrameSlot slot, Object value) {
        CompilerAsserts.partialEvaluationConstant(this);

        if (listeners != null) {
            StackListener[] ls = listeners;
            for (StackListener listener : ls) {
                listener.onObjectSet(slot, value);
            }
        }
    }
}
