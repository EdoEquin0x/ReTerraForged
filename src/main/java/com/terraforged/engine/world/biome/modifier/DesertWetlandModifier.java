/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.terrain.TerrainCategory;

public class DesertWetlandModifier implements BiomeModifier {
    private final BiomeMap<?> biomes;

    public DesertWetlandModifier(BiomeMap<?> biomes) {
        this.biomes = biomes;
    }

    @Override
    public int priority() {
        return 6;
    }

    @Override
    public boolean exitEarly() {
        return true;
    }

    @Override
    public boolean test(int seed, int biome, Cell cell) {
        return cell.terrain.getDelegate() == TerrainCategory.WETLAND && cell.biome == BiomeType.DESERT;
    }

    @Override
    public int modify(int seed, int in, Cell cell, int x, int z) {
        return this.biomes.getLandSet().getBiome(DesertWetlandModifier.getBiomeType(cell), cell.temperature, cell.biomeRegionId);
    }

    private static BiomeType getBiomeType(Cell cell) {
        return cell.biomeRegionId < 0.5f ? BiomeType.SAVANNA : BiomeType.STEPPE;
    }
}

