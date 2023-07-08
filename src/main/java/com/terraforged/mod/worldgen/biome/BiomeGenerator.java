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

package com.terraforged.mod.worldgen.biome;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.Seeds;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.decorator.FeatureDecorator;
import com.terraforged.mod.worldgen.biome.decorator.SurfaceDecorator;
import com.terraforged.mod.worldgen.biome.surface.Surface;
import com.terraforged.mod.worldgen.cave.NoiseCaveGenerator;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;

public class BiomeGenerator {
    private final SurfaceDecorator surfaceDecorator;
    private final FeatureDecorator featureDecorator;
    private final NoiseCaveGenerator noiseCaveGenerator;

    public BiomeGenerator(Holder<VegetationConfig>[] vegetation, Holder<Structure>[] structures, Holder<NoiseCave>[] caves) {
        this.surfaceDecorator = new SurfaceDecorator();
        this.featureDecorator = new FeatureDecorator(vegetation, structures);
        this.noiseCaveGenerator = new NoiseCaveGenerator(caves); //TODO
    }

    public void surface(ChunkAccess chunk, WorldGenRegion region, RandomState state, Generator generator) {
    	this.surfaceDecorator.decorate(chunk, region, generator, state);
        this.surfaceDecorator.decoratePost(chunk, region, generator);
    }

    public void carve(int seed, ChunkAccess chunk, WorldGenRegion region, BiomeManager biomes, GenerationStep.Carving step, Generator generator) {
    	this.noiseCaveGenerator.carve(seed, chunk, generator);
    }

    public void decorate(ChunkAccess chunk, WorldGenLevel region, StructureManager structures, Generator generator) {
        int seed = Seeds.get(region.getSeed());
        var terrain = generator.getChunkDataAsync(seed, chunk.getPos());

        this.featureDecorator.decorate(chunk, region, structures, terrain, generator);
        this.noiseCaveGenerator.decorate(seed, chunk, region, generator);

        Surface.smoothWater(chunk, region, terrain.join());
        Surface.applyPost(chunk, terrain.join(), generator);
    }
}
