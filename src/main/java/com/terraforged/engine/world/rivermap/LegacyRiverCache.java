/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.rivermap;

import com.terraforged.engine.cell.Cell;
import com.terraforged.noise.util.NoiseUtil;

public class LegacyRiverCache extends RiverCache {
    public LegacyRiverCache(RiverGenerator generator) {
        super(generator);
    }

    @Override
    public Rivermap getRivers(int seed, Cell cell) {
        return this.getRivers(seed, cell.continentX, cell.continentZ);
    }

    @Override
    public Rivermap getRivers(int seed, int x, int z) {
        return this.cache.computeIfAbsent(NoiseUtil.seed(x, z), id -> this.generator.generateRivers(seed, x, z, id));
    }
}

