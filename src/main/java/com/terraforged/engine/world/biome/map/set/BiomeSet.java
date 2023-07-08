/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntComparator
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntListIterator
 */
package com.terraforged.engine.world.biome.map.set;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiome;
import com.terraforged.noise.util.NoiseUtil;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;

public abstract class BiomeSet {
    private static final int[] EMPTY = new int[0];
    private static final IntList EMPTY_LIST = new IntArrayList();
    protected final int[][] biomes;
    protected final DefaultBiome defaultBiome;

    public BiomeSet(int[][] biomes, DefaultBiome defaultBiome) {
        this.biomes = biomes;
        this.defaultBiome = defaultBiome;
    }

    public int getSize(int index) {
        return this.biomes[index].length;
    }

    public int getSize(Cell cell) {
        return this.biomes[this.getIndex(cell)].length;
    }

    public int[] getSet(int index) {
        return this.biomes[index];
    }

    public int[] getSet(Cell cell) {
        return this.biomes[this.getIndex(cell)];
    }

    public int getBiome(Cell cell) {
        int[] set = this.biomes[this.getIndex(cell)];
        if (set.length == 0) {
            return this.defaultBiome.getDefaultBiome(cell);
        }
        int maxIndex = set.length - 1;
        int index = NoiseUtil.round((float)maxIndex * cell.biomeRegionId);
        if (index < 0 || index >= set.length) {
            return this.defaultBiome.getDefaultBiome(cell);
        }
        return set[index];
    }

    public abstract int getIndex(Cell var1);

    public abstract void forEach(BiConsumer<String, int[]> var1);

    protected static int[][] collect(Map<? extends Enum<?>, IntList> map, int size, Function<Enum<?>, Integer> indexer, IntComparator comparator) {
        int[][] biomes = new int[size][];
        for (Enum<?> type : map.keySet()) {
            int index = indexer.apply(type);
            if (index < 0 || index >= size) continue;
            IntList list = map.getOrDefault(type, EMPTY_LIST);
            list = BiomeSet.minimize(list);
            list.sort(comparator);
            biomes[index] = list.toIntArray();
        }
        for (int i = 0; i < size; ++i) {
            if (biomes[i] != null) continue;
            biomes[i] = EMPTY;
        }
        return biomes;
    }

    @SuppressWarnings("deprecation")
    private static IntList minimize(IntList list) {
        Int2IntMap counts = BiomeSet.count(list);
        IntArrayList result = new IntArrayList(list.size());
        int min = counts.values().stream().min(Integer::compareTo).orElse(1);
        IntListIterator intListIterator = list.iterator();
        while (intListIterator.hasNext()) {
			int t = (Integer)intListIterator.next();
            int count = counts.get(t);
            int amount = count / min;
            for (int i = 0; i < amount; ++i) {
                result.add(t);
            }
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private static Int2IntMap count(IntList list) {
        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        IntListIterator intListIterator = list.iterator();
        while (intListIterator.hasNext()) {
            int t = (Integer)intListIterator.next();
            int count = map.getOrDefault(t, 0);
            map.put(t, ++count);
        }
        return map;
    }
}

