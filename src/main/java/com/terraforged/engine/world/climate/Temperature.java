/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.climate;

import com.terraforged.noise.Module;
import com.terraforged.noise.util.NoiseUtil;

public class Temperature implements Module {
    private final int power;
    private final float frequency;

    public Temperature(float frequency, int power) {
        this.frequency = frequency;
        this.power = power;
    }

    @Override
    public float getValue(int seed, float x, float y) {
        float sin = NoiseUtil.sin(y *= this.frequency);
        sin = NoiseUtil.clamp(sin, -1.0f, 1.0f);
        float value = NoiseUtil.pow(sin, this.power);
        value = NoiseUtil.copySign(value, sin);
        return NoiseUtil.map(value, -1.0f, 1.0f, 2.0f);
    }
}

