/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.terrain.TerrainCategory;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public class BeachModifier implements BiomeModifier {
    private final float height;
    private final Module noise;
    private final BiomeMap<?> biomes;
    private final int mushroomFields;
    private final int mushroomFieldShore;

    public BeachModifier(BiomeMap<?> biomeMap, GeneratorContext context, int mushroomFields, int mushroomFieldShore) {
        this.biomes = biomeMap;
        this.height = context.levels.water(5);
        this.noise = Source.build(context.seed.next(), 20, 1).perlin2().scale(context.levels.scale(5));
        this.mushroomFields = mushroomFields;
        this.mushroomFieldShore = mushroomFieldShore;
    }

    @Override
    public int priority() {
        return 9;
    }

    @Override
    public boolean test(int seed, int biome, Cell cell) {
        return cell.terrain.getDelegate() == TerrainCategory.BEACH && cell.biome != BiomeType.DESERT;
    }

    @Override
    public int modify(int seed, int in, Cell cell, int x, int z) {
        if (cell.value + this.noise.getValue(seed, x, z) < this.height) {
            if (in == this.mushroomFields) {
                return this.mushroomFieldShore;
            }
            return this.biomes.getBeach(cell);
        }
        return in;
    }
}

