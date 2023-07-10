/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.climate;

import com.terraforged.noise.Module;

public class Compressor implements Module {
    private final float lowerStart;
    private final float lowerEnd;
    private final float lowerRange;
    private final float lowerExpandRange;
    private final float upperStart;
    private final float upperEnd;
    private final float upperRange;
    private final float upperExpandedRange;
    private final float compression;
    private final float compressionRange;
    private final Module module;

    public Compressor(Module module, float inset, float amount) {
        this(module, inset, inset + amount, 1.0f - inset - amount, 1.0f - inset);
    }

    public Compressor(Module module, float lowerStart, float lowerEnd, float upperStart, float upperEnd) {
        this.module = module;
        this.lowerStart = lowerStart;
        this.lowerEnd = lowerEnd;
        this.lowerRange = lowerStart;
        this.lowerExpandRange = lowerEnd;
        this.upperStart = upperStart;
        this.upperEnd = upperEnd;
        this.upperRange = 1.0f - upperEnd;
        this.upperExpandedRange = 1.0f - upperStart;
        this.compression = upperStart - lowerEnd;
        this.compressionRange = upperEnd - lowerStart;
    }

    @Override
    public float getValue(float x, float y) {
        float value = this.module.getValue(x, y);
        if (value <= this.lowerStart) {
            float alpha = value / this.lowerRange;
            return alpha * this.lowerExpandRange;
        }
        if (value >= this.upperEnd) {
            float delta = value - this.upperEnd;
            float alpha = delta / this.upperRange;
            return this.upperStart + alpha * this.upperExpandedRange;
        }
        float delta = value - this.lowerStart;
        float alpha = delta / this.compressionRange;
        return this.lowerEnd + alpha * this.compression;
    }
}

