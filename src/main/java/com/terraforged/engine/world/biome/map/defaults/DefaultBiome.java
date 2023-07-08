/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.map.defaults;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.map.BiomeContext;

public interface DefaultBiome {
    public int getBiome(float var1);

    default public int getNone() {
        return Integer.MIN_VALUE;
    }

    default public int getMedium() {
        return this.getNone();
    }

    default public int getDefaultBiome(Cell cell) {
        return this.getBiome(cell.temperature);
    }

    default public int getDefaultBiome(float temperature) {
        return this.getBiome(temperature);
    }

    public static interface Factory<Biome> {
        public DefaultBiome create(BiomeContext<Biome> var1);
    }
}

