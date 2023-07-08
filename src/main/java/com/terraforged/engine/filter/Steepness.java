/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.filter;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.heightmap.Levels;

public class Steepness implements Filter, Filter.Visitor {
    private final int radius;
    private final float scaler;
    private final float waterLevel;

    public Steepness(int radius, float scaler, Levels levels) {
        this.radius = radius;
        this.scaler = scaler;
        this.waterLevel = levels.water;
    }

    @Override
    public void apply(Filterable cellMap, int seedX, int seedZ, int iterations) {
        this.iterate(cellMap, this);
    }

    @Override
    public void visit(Filterable cellMap, Cell cell, int cx, int cz) {
        float totalHeightDif = 0.0f;
        for (int dz = -1; dz <= 2; ++dz) {
            for (int dx = -1; dx <= 2; ++dx) {
                Cell neighbour;
                if (dx == 0 && dz == 0 || (neighbour = cellMap.getCellRaw(cx + dx * this.radius, cz + dz * this.radius)).isAbsent()) continue;
                float height = Math.max(neighbour.value, this.waterLevel);
                totalHeightDif += Math.abs(cell.value - height) / (float)this.radius;
            }
        }
        cell.gradient = Math.min(1.0f, totalHeightDif * this.scaler);
    }
}

