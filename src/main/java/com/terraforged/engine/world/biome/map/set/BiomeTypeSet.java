/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package com.terraforged.engine.world.biome.map.set;

import java.util.Map;
import java.util.function.BiConsumer;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiome;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.noise.util.NoiseUtil;

import it.unimi.dsi.fastutil.ints.IntList;

public class BiomeTypeSet extends BiomeSet {
    public BiomeTypeSet(Map<BiomeType, IntList> map, DefaultBiome defaultBiome, BiomeContext<?> context) {
        super(BiomeSet.collect(map, BiomeType.values().length, Enum::ordinal, context), defaultBiome);
    }

    public int getBiome(BiomeType type, float temperature, float identity) {
        int[] set = this.getSet(type.ordinal());
        if (set.length == 0) {
            return this.defaultBiome.getDefaultBiome(temperature);
        }
        int maxIndex = set.length - 1;
        int index = NoiseUtil.round((float)maxIndex * identity);
        if (index < 0 || index >= set.length) {
            return this.defaultBiome.getDefaultBiome(temperature);
        }
        return set[index];
    }

    @Override
    public int getIndex(Cell cell) {
        return cell.biome.ordinal();
    }

    @Override
    public void forEach(BiConsumer<String, int[]> consumer) {
        for (BiomeType type : BiomeType.values()) {
            int[] biomes = this.getSet(type.ordinal());
            consumer.accept(type.name(), biomes);
        }
    }
}

