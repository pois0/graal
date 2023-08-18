package com.oracle.truffle.sl.runtime.diffexec;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

@SuppressWarnings("UnstableApiUsage")
public abstract class Hashable {
    public abstract Hasher hash(Hasher hasher);

    @Override
    public int hashCode() {
        return hash(newHasher()).hash().asInt();
    }

    private static Hasher newHasher() {
        return Hashing.murmur3_32_fixed().newHasher();
    }
}
