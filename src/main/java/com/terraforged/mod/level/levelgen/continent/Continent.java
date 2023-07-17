/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.rivermap.Rivermap;

public interface Continent extends Populator {
    float getEdgeValue(float x, float y);

    default float getLandValue(float x, float z) {
        return this.getEdgeValue(x, z);
    }

    long getNearestCenter(float x, float y);

    Rivermap getRivermap(int x, int y);

    default Rivermap getRivermap(Cell cell) {
        return this.getRivermap(cell.continentX, cell.continentZ);
    }
}

