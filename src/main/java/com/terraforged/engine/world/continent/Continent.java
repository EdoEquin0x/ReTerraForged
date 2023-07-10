/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.continent;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.cell.Populator;
import com.terraforged.engine.world.rivermap.Rivermap;

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

