package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.interop.TruffleObject;

public abstract sealed class CalcResult implements TruffleObject {
    protected final boolean isFresh;

    protected CalcResult(boolean isFresh) {
        this.isFresh = isFresh;
    }

    public boolean isFresh() {
        return isFresh;
    }

    public abstract CalcResult.Generic getGenericResult();

    public static final class Generic extends CalcResult {
        private final Object result;

        public static Generic cached(Object result) {
            return new Generic(result, false);
        }

        public static Generic fresh(Object result) {
            return new Generic(result, true);
        }

        public Generic(Object result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public Object getResult() {
            return result;
        }

        @Override
        public Generic getGenericResult() {
            return this;
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

        public Boolean(boolean result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public boolean getResult() {
            return result;
        }

        @Override
        public Generic getGenericResult() {
            return new Generic(result, isFresh);
        }
    }

    public static final class Long extends CalcResult {
        private final long result;

        public static Long cached(long result) {
            return new Long(result, false);
        }

        public static Long fresh(long result) {
            return new Long(result, true);
        }

        public Long(long result, boolean isFresh) {
            super(isFresh);
            this.result = result;
        }

        public long getResult() {
            return result;
        }

        @Override
        public Generic getGenericResult() {
            return new Generic(result, isFresh);
        }
    }
}