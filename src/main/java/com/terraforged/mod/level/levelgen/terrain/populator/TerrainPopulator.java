/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain.populator;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.settings.TerrainSettings;
import com.terraforged.mod.level.levelgen.terrain.Terrain;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;

public class TerrainPopulator implements Populator {
	protected final float weight;
    protected final Terrain type;
    protected final Module base;
    protected final Module variance;

    public TerrainPopulator(Terrain type, Module base, Module variance, float weight) {
        this.type = type;
        this.base = base;
        this.weight = weight;
        this.variance = variance;
    }

    public float getWeight() {
        return this.weight;
    }

    public Module getVariance() {
        return this.variance;
    }

    public Terrain getType() {
        return this.type;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float base = this.base.getValue(x, z);
        float variance = this.variance.getValue(x, z);
        cell.value = base + variance;
        if (cell.value < 0.0f) {
            cell.value = 0.0f;
        } else if (cell.value > 1.0f) {
            cell.value = 1.0f;
        }
        cell.terrain = this.type;
    }

    public static Module clamp(Module module) {
        if (module.minValue() < 0.0f || module.maxValue() > 1.0f) {
            return module.clamp(0.0, 1.0);
        }
        return module;
    }

    public static TerrainPopulator of(Terrain type, Module variance) {
        return new TerrainPopulator(type, Source.ZERO, variance, 1.0f);
    }

    public static TerrainPopulator of(Terrain type, Module base, Module variance, TerrainSettings.Terrain settings) {
        if (settings.verticalScale() == 1.0f && settings.baseScale() == 1.0f) {
            return new TerrainPopulator(type, base, variance, settings.weight());
        }
        return new ScaledPopulator(type, base, variance, settings.baseScale(), settings.verticalScale(), settings.weight());
    }
}

