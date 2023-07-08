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
import com.terraforged.engine.world.biome.TempCategory;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiome;

import it.unimi.dsi.fastutil.ints.IntList;

public class TemperatureSet extends BiomeSet {
    public TemperatureSet(Map<TempCategory, IntList> map, DefaultBiome defaultBiome, BiomeContext<?> context) {
        super(BiomeSet.collect(map, 3, Enum::ordinal, context), defaultBiome);
    }

    @Override
    public int getIndex(Cell cell) {
        if (cell.temperature < 0.25f) {
            return 0;
        }
        if (cell.temperature > 0.75f) {
            return 2;
        }
        return 1;
    }

    @Override
    public void forEach(BiConsumer<String, int[]> consumer) {
        for (TempCategory temp : TempCategory.values()) {
            int[] biomes = this.getSet(temp.ordinal());
            consumer.accept(temp.name(), biomes);
        }
    }
}

