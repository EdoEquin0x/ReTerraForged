/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.map;

import com.terraforged.engine.world.biome.type.BiomeType;

public interface BiomeCollector<T> extends BiomeMap.Builder<T> {
    public BiomeCollector<T> add(T var1);

    @Override
    default public BiomeMap.Builder<T> addBeach(T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap.Builder<T> addCoast(T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap.Builder<T> addLake(T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap.Builder<T> addLand(BiomeType type, T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap.Builder<T> addMountain(T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap.Builder<T> addOcean(T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap.Builder<T> addRiver(T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap.Builder<T> addVolcano(T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap.Builder<T> addWetland(T biome, int count) {
        return this.add(biome);
    }

    @Override
    default public BiomeMap<T> build() {
        throw new UnsupportedOperationException();
    }
}

