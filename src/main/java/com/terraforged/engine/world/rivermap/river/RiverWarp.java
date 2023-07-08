/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.rivermap.river;

import java.util.Random;

import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.noise.util.Noise;
import com.terraforged.noise.util.NoiseUtil;

public class RiverWarp {
    public static final RiverWarp NONE = new RiverWarp(0, 0.0f, 0.0f, 0.0f, 0.0f);
    private static final float WIGGLE_MIN = 2.0f;
    private static final float WIGGLE_MAX = 45.0f;
    private static final float WIGGLE_DIST = 25.0f;
    private static final float WIGGLE_FADE = 0.075f;
    private static final float WIGGLE_FREQUENCY = 8.0f;
    private static final float LEN_FACTOR_INV = 4.0E-4f;
    private final int seed;
    private final float lower;
    private final float upper;
    private final float lowerRange;
    private final float upperRange;
    private final float frequency;
    private final float scale;

    public RiverWarp(int seed, float lower, float upper, float frequency, float scale) {
        this.seed = seed;
        this.frequency = frequency;
        this.scale = scale;
        this.lower = lower;
        this.upper = upper;
        this.lowerRange = 1.0f / lower;
        this.upperRange = 1.0f / (1.0f - upper);
    }

    public RiverWarp createChild(float lower, float upper, float factor, Random random) {
        return new RiverWarp(random.nextInt(), lower, upper, this.frequency * factor, this.scale * factor);
    }

    public boolean test(float t) {
        return this != NONE && t >= 0.0f && t <= 1.0f;
    }

    public long getOffset(float x, float z, float t, River river) {
        float alpha1 = this.getWarpAlpha(t);
        float px = x * this.frequency;
        float pz = z * this.frequency;
        float distance = alpha1 * this.scale;
        float noise = Noise.singleSimplex(px, pz, this.seed);
        float dx = river.normX * noise * distance;
        float dz = river.normZ * noise * distance;
        float alpha2 = this.getWiggleAlpha(t);
        float factor = river.length * LEN_FACTOR_INV;
        float wiggleFreq = WIGGLE_FREQUENCY * factor;
        float wiggleDist = NoiseUtil.clamp(alpha2 * WIGGLE_DIST * factor, WIGGLE_MIN, WIGGLE_MAX);
        float rads = noise + t * ((float)Math.PI * 2) * wiggleFreq;
        return PosUtil.packf(dx += NoiseUtil.cos(rads) * river.normX * wiggleDist, dz += NoiseUtil.sin(rads) * river.normZ * wiggleDist);
    }

    private float getWarpAlpha(float t) {
        if (t < 0.0f || t > 1.0f) {
            return 0.0f;
        }
        if (t < this.lower) {
            return t * this.lowerRange;
        }
        if (t > this.upper) {
            return (1.0f - t) * this.upperRange;
        }
        return 1.0f;
    }

    private float getWiggleAlpha(float t) {
        return NoiseUtil.map(t, 0.0f, WIGGLE_FADE, WIGGLE_FADE);
    }

    public static RiverWarp create(float fade, Random random) {
        return RiverWarp.create(fade, 1.0f - fade, random);
    }

    public static RiverWarp create(float lower, float upper, Random random) {
        float scale = 125.0f + (float)random.nextInt(50);
        float frequency = 5.0E-4f + random.nextFloat() * 5.0E-4f;
        return new RiverWarp(random.nextInt(), lower, upper, frequency, scale);
    }
}

