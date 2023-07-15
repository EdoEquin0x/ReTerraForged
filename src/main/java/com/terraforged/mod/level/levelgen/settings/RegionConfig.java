/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.terraforged.mod.noise.Module;

public class RegionConfig {
    public final int seed;
    public final int scale;
    public final Module warpX;
    public final Module warpZ;
    public final double warpStrength;

    public RegionConfig(int seed, int scale, Module warpX, Module warpZ, double warpStrength) {
        this.seed = seed;
        this.scale = scale;
        this.warpX = warpX;
        this.warpZ = warpZ;
        this.warpStrength = warpStrength;
    }
}

