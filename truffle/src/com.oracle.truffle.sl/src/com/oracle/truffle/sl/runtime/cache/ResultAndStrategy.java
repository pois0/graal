package com.oracle.truffle.sl.runtime.cache;

import com.oracle.truffle.api.interop.TruffleObject;

public abstract class ResultAndStrategy implements TruffleObject {
    private final boolean isFresh;

    protected ResultAndStrategy(boolean isFresh) {
        this.isFresh = isFresh;
    }

    public boolean isFresh() {
        return isFresh;
    }

    public abstract Object getGenericResult();

    public final static class Generic<T> extends ResultAndStrategy {
        private final T result;

        public static <T> ResultAndStrategy.Generic<T> cached(T result) {
            return new ResultAndStrategy.Generic<>(result, false);
        }

        public static <T> ResultAndStrategy.Generic<T> fresh(T result) {
            return new ResultAndStrategy.Generic<>(result, true);
        }

        public Generic(T result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public T getResult() {
            return result;
        }

        @Override
        public Object getGenericResult() {
            return getResult();
        }
    }

    public final static class Boolean extends ResultAndStrategy {
        private final boolean result;

        public static ResultAndStrategy.Boolean cached(boolean result) {
            return new ResultAndStrategy.Boolean(result, false);
        }

        public static ResultAndStrategy.Boolean fresh(boolean result) {
            return new ResultAndStrategy.Boolean(result, true);
        }

        public Boolean(boolean result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public boolean getResult() {
            return result;
        }

        @Override
        public Object getGenericResult() {
            return getResult();
        }

        public Generic<Object> generify() {
            if (isFresh()) {
                return Generic.fresh(getResult());
            } else {
                return Generic.cached(getResult());
            }
        }
    }

    public final static class Long extends ResultAndStrategy {
        private final long result;

        public static ResultAndStrategy.Long cached(long result) {
            return new ResultAndStrategy.Long(result, false);
        }

        public static ResultAndStrategy.Long fresh(long result) {
            return new ResultAndStrategy.Long (result, true);
        }

        public Long(long result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public long getResult() {
            return result;
        }

        @Override
        public Object getGenericResult() {
            return getResult();
        }

        public Generic<Object> generify() {
            if (isFresh()) {
                return Generic.fresh(getResult());
            } else {
                return Generic.cached(getResult());
            }
        }
    }
}
