/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain.region;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.noise.util.NoiseUtil;

public class RegionLerper implements Populator {
    private final Populator lower;
    private final Populator upper;

    public RegionLerper(Populator lower, Populator upper) {
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        float alpha = cell.terrainRegionEdge;
        if (alpha == 0.0f) {
            this.lower.apply(cell, x, y);
            return;
        }
        if (alpha == 1.0f) {
            this.upper.apply(cell, x, y);
            return;
        }
        this.lower.apply(cell, x, y);
        float lowerValue = cell.value;
        this.upper.apply(cell, x, y);
        float upperValue = cell.value;
        cell.value = NoiseUtil.lerp(lowerValue, upperValue, alpha);
    }
}

