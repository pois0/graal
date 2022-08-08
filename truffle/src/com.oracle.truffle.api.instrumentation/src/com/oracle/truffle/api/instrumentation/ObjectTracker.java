package com.oracle.truffle.api.instrumentation;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.LanguageInfo;

import java.util.Arrays;

public class ObjectTracker {
    @CompilerDirectives.CompilationFinal(dimensions = 1) private volatile ObjectChangeListener[] listeners = null;

    final LanguageInfo language;

    public ObjectTracker(LanguageInfo language) {
        this.language = language;
    }

    public void addListener(ObjectChangeListener l) {
        CompilerAsserts.neverPartOfCompilation();
        synchronized (this) {
            if (listeners == null) {
                listeners = new ObjectChangeListener[]{l};
            } else {
                int i = listeners.length;
                ObjectChangeListener[] newListeners = Arrays.copyOf(listeners, i + 1);
                newListeners[i] = l;
                listeners = newListeners;
            }
        }
    }

    public void removeListener(ObjectChangeListener l) {
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
                            ObjectChangeListener[] newListeners = new ObjectChangeListener[len - 1];
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
    public void notifyAssignment(Object object, Object key, Object value) {
        CompilerAsserts.partialEvaluationConstant(this);

        if (listeners != null) {
            ObjectChangeListener[] ls = listeners;
            ObjectChangeEvent e = new ObjectChangeEvent(object, key, value);
            for (ObjectChangeListener listener : ls) {
                listener.onFieldAssigned(e);
            }
        }
    }
}
