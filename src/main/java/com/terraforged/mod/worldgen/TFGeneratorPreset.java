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

package com.terraforged.mod.worldgen;

import com.terraforged.mod.registry.data.TFBiomes;
import com.terraforged.mod.util.storage.WeightMap;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.TFBiomeSource;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public class TFGeneratorPreset {

	public static TFGenerator build(
		TerrainLevels levels,
		WeightMap<Holder<TerrainNoise>> terrain,
		Holder<VegetationConfig>[] vegetation,
		Holder<NoiseCave>[] caves,
		BiomeSource source,
		HolderGetter<Biome> biomes,
		HolderGetter<NoiseGeneratorSettings> noiseSettings
	) {
        var biomeGenerator = new BiomeGenerator(vegetation, caves);
        var noiseGenerator = new NoiseGenerator(levels, terrain).withErosion();
        var biomeSource = new TFBiomeSource(
        	noiseGenerator,
        	biomes.getOrThrow(TFBiomes.CAVE),
        	source
        );
        var vanillaGen = getVanillaGen(biomeSource, noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        return new TFGenerator(levels, vanillaGen, biomeSource, biomeGenerator, noiseGenerator, terrain, vegetation, caves);
    }

    public static VanillaGen getVanillaGen(
    	BiomeSource source, 
    	Holder<NoiseGeneratorSettings> noiseSettings
    ) {
        return new VanillaGen(source, noiseSettings);
    }

    public static boolean isTerraForgedWorld(WorldGenSettings settings) {
        var stem = settings.dimensions().get(LevelStem.OVERWORLD).orElseThrow();
        return getGenerator(stem.generator()) != null;
    }

    public static boolean isTerraForgedWorld(ServerLevel level) {
        return getGenerator(level) != null;
    }

    public static TFGenerator getGenerator(ServerLevel level) {
        return getGenerator(level.getChunkSource().getGenerator());
    }

    private static TFGenerator getGenerator(ChunkGenerator chunkGenerator) {
        return chunkGenerator instanceof TFGenerator generator ? generator : null;
    }
}
