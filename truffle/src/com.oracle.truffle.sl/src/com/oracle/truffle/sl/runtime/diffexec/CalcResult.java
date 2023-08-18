package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.interop.TruffleObject;

public abstract sealed class CalcResult implements TruffleObject {
    private final boolean isFresh;

    protected CalcResult(boolean isFresh) {
        this.isFresh = isFresh;
    }

    public boolean isFresh() {
        return isFresh;
    }

    public abstract Object getGenericResult();

    public static final class Generic<T> extends CalcResult {
        private final T result;

        public static <T> Generic<T> cached(T result) {
            return new Generic<>(result, false);
        }

        public static <T> Generic<T> fresh(T result) {
            return new Generic<>(result, true);
        }

        private Generic(T result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public T getResult() {
            return result;
        }

        @Override
        public Object getGenericResult() {
            return result;
        }
    }

    public static final class Boolean extends CalcResult {
        private final boolean result;

        public static Boolean cached(boolean result) {
            return new Boolean(result, false);
        }

        public static Boolean fresh(boolean result) {
            return new Boolean(result, true);
        }

        private Boolean(boolean result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public boolean getResult() {
            return result;
        }

        @Override
        public Object getGenericResult() {
            return result;
        }
    }

    public static final class Long extends CalcResult {
        private final long result;

        public static Long cached(long result) {
            return new Long (result, false);
        }

        public static Long fresh(long result) {
            return new Long(result, true);
        }

        private Long(long result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public long getResult() {
            return result;
        }

        @Override
        public Object getGenericResult() {
            return result;
        }
    }
}
