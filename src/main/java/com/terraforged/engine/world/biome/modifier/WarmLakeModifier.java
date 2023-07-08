/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.engine.world.biome.type.BiomeType;

public class WarmLakeModifier implements BiomeModifier {
    private final int match;
    private final int replace;

    public <T> WarmLakeModifier(BiomeContext<T> context, T match, T replace) {
        this.match = context.getId(match);
        this.replace = context.getId(replace);
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean test(int seed, int biome, Cell cell) {
        return biome == this.match && cell.biome != BiomeType.DESERT && cell.terrain.isLake();
    }

    @Override
    public int modify(int seed, int in, Cell cell, int x, int z) {
        return this.replace;
    }
}

