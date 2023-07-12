/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator;

import com.terraforged.mod.level.levelgen.heightmap.Heightmap;

public class WorldGenerator {
    private final Heightmap heightmap;
    private final WorldFilters filters;

    public WorldGenerator(Heightmap heightmap, WorldFilters filters) {
        this.heightmap = heightmap;
        this.filters = filters;
    }

    public Heightmap getHeightmap() {
        return this.heightmap;
    }

    public WorldFilters getFilters() {
        return this.filters;
    }
}

