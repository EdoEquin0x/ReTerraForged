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

package com.terraforged.mod.level.levelgen.biome;

import com.terraforged.mod.level.levelgen.asset.NoiseCave;
import com.terraforged.mod.level.levelgen.asset.VegetationConfig;
import com.terraforged.mod.level.levelgen.biome.decorator.FeatureDecorator;
import com.terraforged.mod.level.levelgen.biome.decorator.SurfaceDecorator;
import com.terraforged.mod.level.levelgen.biome.surface.Surface;
import com.terraforged.mod.level.levelgen.cave.NoiseCaveGenerator;
import com.terraforged.mod.level.levelgen.generator.TFChunkGenerator;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomState;

public class ChunkPopulator {
    private final FeatureDecorator featureDecorator;
    private final NoiseCaveGenerator noiseCaveGenerator;

    public ChunkPopulator(Holder<VegetationConfig>[] vegetation, Holder<NoiseCave>[] caves) {
        this.featureDecorator = new FeatureDecorator(vegetation);
        this.noiseCaveGenerator = new NoiseCaveGenerator(caves);
    }
    
    public FeatureDecorator getDecorator() {
    	return this.featureDecorator;
    }
    
    public NoiseCaveGenerator getCaveGenerator() {
    	return this.noiseCaveGenerator;
    }

    public void surface(ChunkAccess chunk, WorldGenRegion region, RandomState state, TFChunkGenerator generator) {
    	SurfaceDecorator.decorate(chunk, region, generator, state);
    	SurfaceDecorator.decoratePost(chunk, region, generator);
    }

    public void carve(int seed, ChunkAccess chunk, WorldGenRegion region, BiomeManager biomes, GenerationStep.Carving step, TFChunkGenerator generator) {
    	//this.noiseCaveGenerator.carve(seed, chunk, generator);
    }

    public void decorate(int seed, ChunkAccess chunk, WorldGenLevel region, StructureManager structures, TFChunkGenerator generator) {
        var terrain = generator.getChunkDataAsync(chunk.getPos());

        this.featureDecorator.decorate(chunk, region, structures, terrain, generator);
        //this.noiseCaveGenerator.decorate(seed, chunk, region, generator);

        Surface.smoothWater(chunk, region, terrain.join());
        Surface.applyPost(chunk, terrain.join(), generator);
    }
}
