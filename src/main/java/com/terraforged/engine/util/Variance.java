/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util;

import java.util.Random;

public class Variance {
    private final float min;
    private final float range;

    private Variance(float min, float range) {
        this.min = min;
        this.range = range;
    }

    public float apply(float value) {
        return this.min + value * this.range;
    }

    public float apply(float value, float scaler) {
        return this.apply(value) * scaler;
    }

    public float next(FastRandom random) {
        return this.apply(random.nextFloat());
    }

    public float next(Random random) {
        return this.apply(random.nextFloat());
    }

    public float next(FastRandom random, float scalar) {
        return this.apply(random.nextFloat(), scalar);
    }

    public float next(Random random, float scalar) {
        return this.apply(random.nextFloat(), scalar);
    }

    public static Variance min(double min) {
        return new Variance((float)min, 1.0f - (float)min);
    }

    public static Variance range(double range) {
        return new Variance(1.0f - (float)range, (float)range);
    }

    public static Variance of(double min, double range) {
        return new Variance((float)min, (float)range);
    }
}

