/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator;

import java.util.function.Supplier;

import com.terraforged.mod.level.levelgen.generator.climate.Climate;
import com.terraforged.mod.level.levelgen.heightmap.Heightmap;

public class WorldGeneratorFactory implements Supplier<WorldGenerator> {
    private final Heightmap heightmap;
    private final WorldFilters filters;

    public WorldGeneratorFactory(GeneratorContext context) {
        this.heightmap = new Heightmap(context);
        this.filters = new WorldFilters(context);
    }

    public WorldGeneratorFactory(GeneratorContext context, Heightmap heightmap) {
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

    @Override
    public WorldGenerator get() {
        return new WorldGenerator(this.heightmap, this.filters);
    }
}

