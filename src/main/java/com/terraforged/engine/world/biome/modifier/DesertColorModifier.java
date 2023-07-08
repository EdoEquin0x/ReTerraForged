/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.DesertBiomes;

public class DesertColorModifier implements BiomeModifier {
    private final DesertBiomes biomes;

    public DesertColorModifier(DesertBiomes biomes) {
        this.biomes = biomes;
    }

    @Override
    public boolean exitEarly() {
        return true;
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public boolean test(int seed, int biome, Cell cell) {
        return this.biomes.isDesert(biome);
    }

    @Override
    public int modify(int seed, int in, Cell cell, int x, int z) {
        if (this.biomes.isRedDesert(in)) {
            if (cell.macroBiomeId <= 0.5f) {
                return this.biomes.getWhiteDesert(cell.biomeRegionId);
            }
        } else if (cell.macroBiomeId > 0.5f) {
            return this.biomes.getRedDesert(cell.biomeRegionId);
        }
        return in;
    }
}

