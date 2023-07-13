/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator;

import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.level.levelgen.heightmap.Heightmap;

public class WorldGenerator {
    private final Heightmap heightmap;
    private final WorldFilters filters;

    public WorldGenerator(GeneratorContext context) {
        this.heightmap = new Heightmap(context);
        this.filters = new WorldFilters(context);
    }

    public WorldGenerator(GeneratorContext context, Heightmap heightmap) {
        this.heightmap = heightmap;
        this.filters = new WorldFilters(context);
    }

    public Heightmap getHeightmap() {
        return this.heightmap;
    }

    public Climate getClimate() {
        return this.getHeightmap().getClimate();
    }

    public WorldFilters getFilters() {
        return this.filters;
    }
}

