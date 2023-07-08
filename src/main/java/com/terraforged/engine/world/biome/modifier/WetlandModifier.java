/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.engine.world.biome.type.BiomeType;

public class WetlandModifier implements BiomeModifier {
    private final int wetland;
    private final int coldWetland;
    private final int frozenWetland;

    public <T> WetlandModifier(BiomeContext<T> context, T normal, T cold, T frozen) {
        this.wetland = context.getId(normal);
        this.coldWetland = context.getId(cold);
        this.frozenWetland = context.getId(frozen);
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean test(int seed, int biome, Cell cell) {
        if (cell.biome == BiomeType.TAIGA) {
            return biome == this.wetland || biome == this.frozenWetland;
        }
        if (cell.biome == BiomeType.TUNDRA) {
            return biome == this.coldWetland;
        }
        return false;
    }

    @Override
    public int modify(int seed, int in, Cell cell, int x, int z) {
        if (cell.biome == BiomeType.TAIGA) {
            return this.coldWetland;
        }
        return this.frozenWetland;
    }
}

