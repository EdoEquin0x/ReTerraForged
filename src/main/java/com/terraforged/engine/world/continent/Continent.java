/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.continent;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.cell.Populator;
import com.terraforged.engine.world.rivermap.Rivermap;

public interface Continent extends Populator {
    public float getEdgeValue(int seed, float var1, float var2);

    default public float getLandValue(int seed, float x, float z) {
        return this.getEdgeValue(seed, x, z);
    }

    public long getNearestCenter(int seed, float var1, float var2);

    public Rivermap getRivermap(int seed, int var1, int var2);

    default public Rivermap getRivermap(int seed, Cell cell) {
        return this.getRivermap(seed, cell.continentX, cell.continentZ);
    }
}

