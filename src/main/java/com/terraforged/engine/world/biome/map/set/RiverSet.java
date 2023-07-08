/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntSet
 */
package com.terraforged.engine.world.biome.map.set;

import java.util.Map;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.TempCategory;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiome;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class RiverSet extends TemperatureSet {
    private final BiomeMap<?> biomes;
    private final IntSet overrides;

    public RiverSet(Map<TempCategory, IntList> map, BiomeMap<?> biomes, DefaultBiome defaultBiome, BiomeContext<?> context) {
        super(map, defaultBiome, context);
        this.biomes = biomes;
        this.overrides = context.getRiverOverrides();
    }

    @Override
    public int getBiome(Cell cell) {
        int biome = this.biomes.getLand(cell);
        if (biome != Integer.MIN_VALUE && this.overrides.contains(biome)) {
            return biome;
        }
        return super.getBiome(cell);
    }
}

