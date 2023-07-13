/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.rivermap.Rivermap;

public interface Continent extends Populator {
    public float getEdgeValue(float x, float y);

    default public float getLandValue(float x, float z) {
        return this.getEdgeValue(x, z);
    }

    public long getNearestCenter(float x, float y);

    public Rivermap getRivermap(int x, int y);

    default public Rivermap getRivermap(Cell cell) {
        return this.getRivermap(cell.continentX, cell.continentZ);
    }
}

