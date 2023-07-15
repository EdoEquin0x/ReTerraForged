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

package com.terraforged.mod.level.levelgen.cave;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.terraforged.mod.level.levelgen.asset.NoiseCave;
import com.terraforged.mod.level.levelgen.generator.TFChunkGenerator;
import com.terraforged.mod.level.levelgen.terrain.generation.TerrainData;
import com.terraforged.mod.noise.Module;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public class CarverChunk {
    private Holder<Biome> cached;
    private int cachedX, cachedZ;

    private int biomeListIndex = -1;
    private final List<Holder<Biome>>[] biomeLists;
    private final Map<NoiseCave, List<Holder<Biome>>> biomes = new IdentityHashMap<>();

    public Module mask;
    public Module modifier;
    public TerrainData terrainData;

    @SuppressWarnings("unchecked")
	public CarverChunk(int size) {
        biomeLists = new List[size];
        for (int i = 0; i < biomeLists.length; i++) {
            biomeLists[i] = new ArrayList<Holder<Biome>>();
        }
    }

    public CarverChunk reset() {
        cached = null;
        biomes.clear();
        biomeListIndex = -1;
        return this;
    }

    public List<Holder<Biome>> getBiomes(NoiseCave config) {
        return biomes.get(config);
    }

    public Holder<Biome> getBiome(int offset, int x, int z, NoiseCave cave, TFChunkGenerator generator) {
        int biomeX = x >> 2;
        int biomeZ = z >> 2;
        if (cached == null || biomeX != cachedX || biomeZ != cachedZ) {
            // pretty sure the seed passed to this should be derived from the cave seed
            cached = generator.getCaveBiomeSampler().getUnderGroundBiome(offset, x, z, cave);
            cachedX = biomeX;
            cachedZ = biomeZ;
            biomes.computeIfAbsent(cave, c -> nextList()).add(cached);
        }
        return cached;
    }

    public float getCarvingMask(int x, int z) {
        float noise = mask.getValue(x, z);
        float river = terrainData.getRiver().get(x, z);
        return 1f - noise * river;
    }

    private List<Holder<Biome>> nextList() {
        int i = biomeListIndex + 1;
        if (i < biomeLists.length) {
            biomeListIndex = i;
            var biomeList = biomeLists[i];
            biomeList.clear();
            return biomeList;
        }
        return new ArrayList<>();
    }
}
