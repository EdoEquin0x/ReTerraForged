/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.geology;

import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public class Stratum<T> {
    private final T value;
    private final Module depth;

    public Stratum(T value, double depth) {
        this(value, Source.constant(depth));
    }

    public Stratum(T value, Module depth) {
        this.depth = depth;
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public float getDepth(int seed, float x, float z) {
        return this.depth.getValue(seed, x, z);
    }

    public static <T> Stratum<T> of(T t, double depth) {
        return new Stratum<T>(t, depth);
    }

    public static <T> Stratum<T> of(T t, Module depth) {
        return new Stratum<T>(t, depth);
    }

    public static interface Visitor<T, Context> {
        public boolean visit(int var1, T var2, Context var3);
    }
}

