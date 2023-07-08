/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public class MountainModifier implements BiomeModifier {
    private static final int MOUNTAIN_START_HEIGHT = 48;
    private final float chance;
    private final float height;
    private final float range;
    private final Module noise;
    private final BiomeMap<?> biomes;

    public MountainModifier(GeneratorContext context, BiomeMap<?> biomes, float usage) {
        this.biomes = biomes;
        this.chance = usage;
        this.range = context.levels.scale(10);
        this.height = context.levels.ground(MOUNTAIN_START_HEIGHT);
        this.noise = Source.perlin(context.seed.next(), 80, 2).scale(this.range);
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean exitEarly() {
        return true;
    }

    @Override
    public boolean test(int seed, int biome, Cell cell) {
        return cell.terrain.isMountain() && cell.macroBiomeId < this.chance;
    }

    @Override
    public int modify(int seed, int in, Cell cell, int x, int z) {
        int mountain;
        if (this.canModify(seed, cell, x, z) && BiomeMap.isValid(mountain = this.biomes.getMountain(cell))) {
            return mountain;
        }
        return in;
    }

    private boolean canModify(int seed, Cell cell, int x, int z) {
        if (cell.value > this.height) {
            return true;
        }
        if (cell.value + this.range < this.height) {
            return false;
        }
        return cell.value + this.noise.getValue(seed, x, z) > this.height;
    }
}

