/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.continent;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.rivermap.Rivermap;

public interface Continent extends Populator {
    public float getEdgeValue(float var1, float var2);

    default public float getLandValue(float x, float z) {
        return this.getEdgeValue(x, z);
    }

    public long getNearestCenter(float var1, float var2);

    public Rivermap getRivermap(int var1, int var2);

    default public Rivermap getRivermap(Cell cell) {
        return this.getRivermap(cell.continentX, cell.continentZ);
    }
}

