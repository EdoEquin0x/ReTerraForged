/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.map.defaults;

public class DefaultBiomeSelector implements DefaultBiome {
    protected final float lower;
    protected final float upper;
    protected final int cold;
    protected final int medium;
    protected final int warm;

    public DefaultBiomeSelector(int cold, int medium, int warm, float lower, float upper) {
        this.cold = cold;
        this.medium = medium;
        this.warm = warm;
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public int getMedium() {
        return this.medium;
    }

    @Override
    public int getBiome(float temperature) {
        if (temperature < this.lower) {
            return this.cold;
        }
        if (temperature > this.upper) {
            return this.warm;
        }
        return this.medium;
    }
}

