/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.noise.poisson;

import java.util.Random;

import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;

public class PoissonContext {
    public int offsetX;
    public int offsetZ;
    public int startX;
    public int startZ;
    public int endX;
    public int endZ;
    public Module density = Source.ONE;
    public final int seed;
    public final Random random;

    public PoissonContext(long seed, Random random) {
        this.seed = (int)seed;
        this.random = random;
    }
}

