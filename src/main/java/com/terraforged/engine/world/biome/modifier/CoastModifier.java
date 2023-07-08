/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.biome.map.BiomeMap;

public class CoastModifier implements BiomeModifier {
    private final float seaLevel;
    private final BiomeMap<?> biomeMap;

    public CoastModifier(GeneratorContext context, BiomeMap<?> biomeMap) {
        this.seaLevel = context.levels.water;
        this.biomeMap = biomeMap;
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean test(int seed, int biome, Cell cell) {
        return cell.terrain.isCoast() || cell.terrain.isShallowOcean() && cell.value > this.seaLevel;
    }

    @Override
    public int modify(int seed, int in, Cell cell, int x, int z) {
        int coast = this.biomeMap.getCoast(cell);
        if (BiomeMap.isValid(coast)) {
            return coast;
        }
        return in;
    }
}

