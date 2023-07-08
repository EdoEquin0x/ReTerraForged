/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package com.terraforged.engine.world.biome.map;

import java.util.function.BiConsumer;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.map.set.BiomeSet;
import com.terraforged.engine.world.biome.map.set.BiomeTypeSet;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.heightmap.Levels;

import it.unimi.dsi.fastutil.ints.IntList;

public interface BiomeMap<T> {
    public static final int NULL_BIOME = Integer.MIN_VALUE;

    public BiomeContext<T> getContext();

    public int getBeach(Cell var1);

    public int getCoast(Cell var1);

    public int getRiver(Cell var1);

    public int getLake(Cell var1);

    public int getWetland(Cell var1);

    public int getShallowOcean(Cell var1);

    public int getDeepOcean(Cell var1);

    public int getLand(Cell var1);

    public int getMountain(Cell var1);

    public int getVolcano(Cell var1);

    public int provideBiome(Cell var1, Levels var2);

    public BiomeTypeSet getLandSet();

    public IntList getAllBiomes(BiomeType var1);

    public void forEach(BiConsumer<String, BiomeSet> var1);

    public static boolean isValid(int id) {
        return id != Integer.MIN_VALUE;
    }

    public static interface Builder<T> {
        public Builder<T> addOcean(T var1, int var2);

        public Builder<T> addBeach(T var1, int var2);

        public Builder<T> addCoast(T var1, int var2);

        public Builder<T> addRiver(T var1, int var2);

        public Builder<T> addWetland(T var1, int var2);

        public Builder<T> addLake(T var1, int var2);

        public Builder<T> addMountain(T var1, int var2);

        public Builder<T> addVolcano(T var1, int var2);

        public Builder<T> addLand(BiomeType var1, T var2, int var3);

        public BiomeMap<T> build();
    }
}

