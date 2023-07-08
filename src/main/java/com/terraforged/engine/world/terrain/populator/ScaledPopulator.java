/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.terrain.populator;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.noise.Module;

public class ScaledPopulator extends TerrainPopulator {
    private final float baseScale;
    private final float varianceScale;

    public ScaledPopulator(Terrain type, Module base, Module variance, float baseScale, float varianceScale, float weight) {
        super(type, base, variance, weight);
        this.baseScale = baseScale;
        this.varianceScale = varianceScale;
    }

    @Override
    public void apply(int seed, Cell cell, float x, float z) {
        float base = this.base.getValue(seed, x, z) * this.baseScale;
        float variance = this.variance.getValue(seed, x, z) * this.varianceScale;
        cell.value = base + variance;
        if (cell.value < 0.0f) {
            cell.value = 0.0f;
        } else if (cell.value > 1.0f) {
            cell.value = 1.0f;
        }
        cell.terrain = this.type;
    }
}

