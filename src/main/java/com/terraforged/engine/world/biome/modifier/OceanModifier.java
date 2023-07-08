/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.heightmap.Levels;

public class OceanModifier implements BiomeModifier {
    private final Levels levels;
    private final float controlPoint;
    private final BiomeMap<?> biomeMap;

    public OceanModifier(GeneratorContext context, BiomeMap<?> biomeMap) {
        this.biomeMap = biomeMap;
        this.levels = context.levels;
        this.controlPoint = context.settings.world.controlPoints.beach;
    }

    @Override
    public int priority() {
        return 15;
    }

    @Override
    public boolean test(int seed, int biome, Cell cell) {
        return cell.terrain.isOverground() && cell.value < this.levels.water && cell.continentEdge < this.controlPoint;
    }

    @Override
    public int modify(int seed, int in, Cell cell, int x, int z) {
        int ocean = this.biomeMap.getShallowOcean(cell);
        if (BiomeMap.isValid(ocean)) {
            return ocean;
        }
        return in;
    }
}

