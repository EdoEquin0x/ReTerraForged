/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.level.levelgen.biome.decorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.terraforged.mod.level.levelgen.asset.VegetationConfig;
import com.terraforged.mod.level.levelgen.biome.vegetation.BiomeVegetation;
import com.terraforged.mod.level.levelgen.biome.vegetation.VegetationFeatures;
import com.terraforged.mod.level.levelgen.biome.viability.ViabilityContext;
import com.terraforged.mod.level.levelgen.generator.TFChunkGenerator;
import com.terraforged.mod.level.levelgen.terrain.generation.TerrainData;
import com.terraforged.mod.util.storage.FloatMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class SamplerContext {
    private static final ThreadLocal<SamplerContext> LOCAL_CONTEXT = ThreadLocal.withInitial(SamplerContext::new);

    public ChunkAccess chunk;
    public WorldGenLevel region;
    public TFChunkGenerator generator;
    public WorldgenRandom random;

    public Biome biome;
    public Optional<VegetationConfig> vegetation;
    public VegetationFeatures features;
    public float maxViability = 0F;

    public final FloatMap viability = new FloatMap();

    public final List<Holder<Biome>> biomeList = new ArrayList<>();
    public final ViabilityContext viabilityContext = new ViabilityContext();
    public final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public int getHeight(int x, int z) {
        return chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
    }

    public Holder<Biome> getBiome(int x, int y, int z) {
        return region.getBiome(pos.set(x, y, z));
    }

    public TerrainData terrainData() {
        return viabilityContext.getTerrain();
    }

    public SamplerContext reset() {
        biomeList.clear();
        return this;
    }

    public void push(Biome biome, BiomeVegetation vegetation) {
        this.maxViability = 0F;
        this.biome = biome;
        this.vegetation = vegetation.config;
        this.features = vegetation.features;
    }

    public static SamplerContext get() {
        return LOCAL_CONTEXT.get().reset();
    }
}
