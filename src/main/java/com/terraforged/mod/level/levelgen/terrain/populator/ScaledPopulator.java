/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain.populator;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.terrain.Terrain;
import com.terraforged.mod.noise.Module;

public class ScaledPopulator extends TerrainPopulator {
    private final float baseScale;
    private final float varianceScale;

    public ScaledPopulator(Terrain type, Module base, Module variance, float baseScale, float varianceScale, float weight) {
        super(type, base, variance, weight);
        this.baseScale = baseScale;
        this.varianceScale = varianceScale;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float base = this.base.getValue(x, z) * this.baseScale;
        float variance = this.variance.getValue(x, z) * this.varianceScale;
        cell.value = base + variance;
        if (cell.value < 0.0f) {
            cell.value = 0.0f;
        } else if (cell.value > 1.0f) {
            cell.value = 1.0f;
        }
        cell.terrain = this.type;
    }
}

