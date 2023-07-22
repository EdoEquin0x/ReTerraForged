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

package com.terraforged.mod.level.levelgen.noise;

import java.util.function.Consumer;

import com.terraforged.mod.level.levelgen.continent.ContinentNoise;
import com.terraforged.mod.level.levelgen.continent.ContinentPoints;
import com.terraforged.mod.level.levelgen.settings.ControlPoints;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.terrain.TerrainBlender;
import com.terraforged.mod.level.levelgen.terrain.TerrainBlender.LocalBlender;
import com.terraforged.mod.level.levelgen.terrain.TerrainLevels;
import com.terraforged.mod.level.levelgen.util.Seed;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.Holder;

public class TerrainNoise {
    protected static final int OCEAN_OFFSET = 8763214;
    protected static final int TERRAIN_OFFSET = 45763218;
    protected static final int CONTINENT_OFFSET = 18749560;
    protected final float heightMultiplier = 1.2F;

    protected final TerrainLevels levels;
    protected final Module ocean;
    protected final TerrainBlender land;
    protected final ContinentNoise continent;
    protected final ControlPoints controlPoints;
    protected final ThreadLocal<NoiseData> localChunk = ThreadLocal.withInitial(NoiseData::new);
    protected final ThreadLocal<NoiseSample> localSample = ThreadLocal.withInitial(NoiseSample::new);

    public TerrainNoise(int seed, Settings settings, TerrainLevels levels, WeightMap<Holder<Module>> terrain) {
    	this.levels = levels;
        this.ocean = createOceanTerrain(seed);
        this.land = createLandTerrain(seed, terrain);
        this.continent = createContinentNoise(seed, settings, levels);
        this.controlPoints = this.continent.getControlPoints();
    }

    public TerrainNoise(TerrainLevels levels, TerrainNoise other) {
        this.levels = levels;
        this.land = other.land;
        this.ocean = other.ocean;
        this.continent = other.continent;
        this.controlPoints = other.continent.getControlPoints();
    }

    public NoiseLevels getLevels() {
        return levels.noiseLevels;
    }

    public TerrainLevels getTerrainLevels() {
        return levels;
    }

    public ContinentNoise getContinent() {
        return continent;
    }
    
    public Holder<Module> getTerrain(int x, int z, LocalBlender blender) {
    	return this.land.getTerrain(blender);
    }

    public void generate(int chunkX, int chunkZ, Consumer<NoiseData> consumer) {
        var noiseData = localChunk.get();
        var blender = land.getBlenderResource();
        var sample = noiseData.sample;

        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int dz = -1; dz < 17; dz++) {
            for (int dx = -1; dx < 17; dx++) {
                int x = startX + dx;
                int z = startZ + dz;

                sample(x, z, sample, blender);

                noiseData.setNoise(dx, dz, sample);
            }
        }

        consumer.accept(noiseData);
    }

    public TerrainBlender.LocalBlender getBlenderResource() {
        return land.getBlenderResource();
    }

    public Holder<Module> getTerrain(int x, int z) {
    	var blender = land.getBlenderResource();
    	return this.land.getTerrain(blender);
    }
    
    public NoiseSample getNoiseSample(int x, int z) {
        var sample = localSample.get().reset();
        sample(x, z, sample);
        return sample;
    }

    public void sample(int x, int z, NoiseSample sample) {
        var blender = land.getBlenderResource();
        sample(x, z, sample, blender);
    }
    
    public NoiseSample sample(int x, int z, NoiseSample sample, TerrainBlender.LocalBlender blender) {
        float nx = getNoiseCoord(x);
        float nz = getNoiseCoord(z);
        sampleTerrain(nx, nz, sample, blender);
        this.continent.sampleRiver(nx, nz, sample);
        return sample;
    }

    protected NoiseSample sampleTerrain(float nx, float nz, NoiseSample sample, TerrainBlender.LocalBlender blender) {
    	this.continent.sampleContinent(nx, nz, sample);
    	
        float continentNoise = sample.continentNoise;
        if (continentNoise < ContinentPoints.SHALLOW_OCEAN) {
            getOcean(nx, nz, sample, blender);
        } else if (continentNoise < ContinentPoints.COAST) {
            getBlend(nx, nz, sample, blender);
        } else {
            getInland(nx, nz, sample, blender);
        }

        return sample;
    }

    private void getOcean(float x, float z, NoiseSample sample, TerrainBlender.LocalBlender blender) {
        float rawNoise = ocean.getValue(x, z);

        sample.heightNoise = levels.noiseLevels.toDepthNoise(rawNoise);
//        sample.terrainType = TerrainType.DEEP_OCEAN;
    }

    private void getInland(float x, float z, NoiseSample sample, TerrainBlender.LocalBlender blender) {
        float baseNoise = sample.baseNoise;
        float heightNoise = land.getValue(x, z, blender) * heightMultiplier;

        sample.heightNoise = levels.noiseLevels.toHeightNoise(baseNoise, heightNoise);
//        sample.terrainType = land.getTerrain(blender);
    }

    private void getBlend(float x, float z, NoiseSample sample, TerrainBlender.LocalBlender blender) {
        if (sample.continentNoise < ContinentPoints.BEACH) {
            float lowerRaw = ocean.getValue(x, z);
            float lower = levels.noiseLevels.toDepthNoise(lowerRaw);

            float upper = levels.noiseLevels.heightMin;
            float alpha = (sample.continentNoise - ContinentPoints.SHALLOW_OCEAN) / (ContinentPoints.BEACH - ContinentPoints.SHALLOW_OCEAN);

            sample.heightNoise = NoiseUtil.lerp(lower, upper, alpha);
        } else if (sample.continentNoise < ContinentPoints.COAST) {
            float lower = levels.noiseLevels.heightMin;

            float baseNoise = sample.baseNoise;
            float upperRaw = land.getValue(x, z, blender) * heightMultiplier;
            float upper = levels.noiseLevels.toHeightNoise(baseNoise, upperRaw);

            float alpha = (sample.continentNoise - ContinentPoints.BEACH) / (ContinentPoints.COAST - ContinentPoints.BEACH);

            sample.heightNoise = NoiseUtil.lerp(lower, upper, alpha);
        }
    }

//    protected Terrain getTerrain(float value, TerrainBlender.Blender blender) {
//        if (value < levels.noiseLevels.heightMin) return TerrainType.SHALLOW_OCEAN;
//
//        return land.getTerrain(blender);
//    }
//    
    public float getNoiseCoord(int coord) {
        return coord * this.getLevels().frequency;
    }

    private static Module createOceanTerrain(int seed) {
        return Source.simplex(seed + OCEAN_OFFSET, 64, 3).scale(0.4);
    }
    
    private static TerrainBlender createLandTerrain(int seed, WeightMap<Holder<Module>> terrains) {
        return new TerrainBlender(seed + TERRAIN_OFFSET, 800, 0.8F, 0.4F, terrains);
    }

    private static ContinentNoise createContinentNoise(int seed, Settings settings, TerrainLevels levels) {
        return new ContinentNoise(new Seed(seed + CONTINENT_OFFSET), levels, settings);
    }
}
