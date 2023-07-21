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

package com.terraforged.mod.level.levelgen.climate;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.terraforged.mod.level.levelgen.continent.ContinentNoise;
import com.terraforged.mod.level.levelgen.noise.NoiseLevels;
import com.terraforged.mod.level.levelgen.noise.TerrainNoise;

public class ClimateSampler {
	protected final Supplier<ClimateNoise> climateNoise;
	protected final Supplier<TerrainNoise> terrainNoise;
	protected final ThreadLocal<ClimateSample> localSample = ThreadLocal.withInitial(ClimateSample::new);

	public ClimateSampler(Supplier<TerrainNoise> terrainNoise) {
		this.climateNoise = Suppliers.memoize(() -> createClimate(terrainNoise.get()));
		this.terrainNoise = terrainNoise;
	}

	public ClimateSample sample(int x, int z) {
		TerrainNoise noise = this.terrainNoise.get();
		NoiseLevels levels = noise.getLevels();
		
		float px = x * levels.frequency;
		float pz = z * levels.frequency;

		var sample = this.localSample.get().reset();
		noise.sample(x, z, sample);
		this.climateNoise.get().sample(px, pz, sample);
		return sample;
	}
	
	static ClimateNoise createClimate(TerrainNoise noise) {
		ContinentNoise continent = noise.getContinent();
		return new ClimateNoise(continent.getSeed(), continent.getSettings());
	}
}
