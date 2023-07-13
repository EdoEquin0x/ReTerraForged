/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator;

import com.terraforged.mod.level.levelgen.filter.Erosion;
import com.terraforged.mod.level.levelgen.filter.Filterable;
import com.terraforged.mod.level.levelgen.filter.Smoothing;
import com.terraforged.mod.level.levelgen.filter.Steepness;
import com.terraforged.mod.level.levelgen.settings.FilterSettings;
import com.terraforged.mod.level.levelgen.tile.Tile;

public class WorldFilters {
    private final Smoothing smoothing;
    private final Steepness steepness;
    private final FilterSettings settings;
    private final WorldErosion erosion;
    private final int erosionIterations;
    private final int smoothingIterations;

    public WorldFilters(GeneratorContext ctx) {
        this.settings = ctx.settings.filters();
        this.smoothing = new Smoothing(ctx.settings, ctx.levels);
        this.steepness = new Steepness(1, 10.0f, ctx.levels);
        this.erosion = new WorldErosion(ctx);
        this.erosionIterations = ctx.settings.filters().erosion().dropletsPerChunk();
        this.smoothingIterations = ctx.settings.filters().smoothing().iterations();
    }

    public FilterSettings getSettings() {
        return this.settings;
    }

    public void apply(Tile tile, boolean optionalFilters) {
        Filterable map = tile.filterable();
        if (optionalFilters) {
            this.applyOptionalFilters(map, tile.getRegionX(), tile.getRegionZ());
        }
        this.applyRequiredFilters(map, tile.getRegionX(), tile.getRegionZ());
    }

    private void applyRequiredFilters(Filterable map, int seedX, int seedZ) {
        this.steepness.apply(map, seedX, seedZ, 1);
    }

    private void applyOptionalFilters(Filterable map, int seedX, int seedZ) {
        Erosion erosion = this.erosion.get(map.getSize().total);
        erosion.apply(map, seedX, seedZ, this.erosionIterations);
        this.smoothing.apply(map, seedX, seedZ, this.smoothingIterations);
    }
}

