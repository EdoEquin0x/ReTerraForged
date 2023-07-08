/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util.poisson;

import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import java.util.Random;

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

