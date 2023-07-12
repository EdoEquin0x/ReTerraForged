/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator;

import java.util.function.IntFunction;

import com.terraforged.mod.level.levelgen.filter.BeachDetect;
import com.terraforged.mod.level.levelgen.filter.Erosion;
import com.terraforged.mod.level.levelgen.filter.Filterable;
import com.terraforged.mod.level.levelgen.filter.Smoothing;
import com.terraforged.mod.level.levelgen.filter.Steepness;
import com.terraforged.mod.level.levelgen.settings.FilterSettings;
import com.terraforged.mod.level.levelgen.tile.Tile;

public class WorldFilters {
    private final Smoothing smoothing;
    private final Steepness steepness;
    private final BeachDetect beach;
    private final FilterSettings settings;
    private final WorldErosion<Erosion> erosion;
    private final int erosionIterations;
    private final int smoothingIterations;

    public WorldFilters(GeneratorContext context) {
        IntFunction<Erosion> factory = Erosion.factory(context);
        this.settings = context.settings.filters();
        this.beach = new BeachDetect(context);
        this.smoothing = new Smoothing(context.settings, context.levels);
        this.steepness = new Steepness(1, 10.0f, context.levels);
        this.erosion = new WorldErosion<Erosion>(factory, (e, size) -> e.getSize() == size);
        this.erosionIterations = context.settings.filters().erosion().dropletsPerChunk();
        this.smoothingIterations = context.settings.filters().smoothing().iterations();
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

    public void applyRequiredFilters(Filterable map, int seedX, int seedZ) {
        this.steepness.apply(map, seedX, seedZ, 1);
        this.beach.apply(map, seedX, seedZ, 1);
    }

    public void applyOptionalFilters(Filterable map, int seedX, int seedZ) {
        Erosion erosion = this.erosion.get(map.getSize().total);
        erosion.apply(map, seedX, seedZ, this.erosionIterations);
        this.smoothing.apply(map, seedX, seedZ, this.smoothingIterations);
    }
}

