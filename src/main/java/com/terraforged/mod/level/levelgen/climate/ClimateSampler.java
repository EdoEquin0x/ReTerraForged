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

import com.terraforged.mod.level.levelgen.noise.INoiseGenerator;
import com.terraforged.mod.level.levelgen.noise.NoiseLevels;
import com.terraforged.mod.level.levelgen.noise.climate.ClimateNoise;
import com.terraforged.mod.level.levelgen.noise.climate.ClimateSample;

public class ClimateSampler {
	protected final NoiseLevels levels;
	protected final ClimateNoise climateNoise;
	protected final INoiseGenerator noiseGenerator;
	protected final int seed;
	protected final ThreadLocal<ClimateSample> localSample = ThreadLocal.withInitial(ClimateSample::new);

	public ClimateSampler(INoiseGenerator noiseGenerator, int seed) {
		this.levels = noiseGenerator.getLevels();
		this.seed = seed;
		this.climateNoise = createClimate(noiseGenerator);
		this.noiseGenerator = noiseGenerator;
	}

	public ClimateSample getSample() {
		return localSample.get().reset();
	}

	public INoiseGenerator getNoiseGenerator() {
		return noiseGenerator;
	}

	public ClimateSample getSample(int x, int z) {
		float px = x * levels.frequency;
		float pz = z * levels.frequency;

		var sample = localSample.get().reset();
		climateNoise.sample(px, pz, sample);

		return sample;
	}

	public float getShape(int x, int z) {
		float px = x * levels.frequency;
		float pz = z * levels.frequency;

		var sample = localSample.get().reset();

		climateNoise.sample(px, pz, sample);

		return sample.biomeEdgeNoise;
	}

	public void sample(int x, int z, ClimateSample sample) {
		float px = x * levels.frequency;
		float pz = z * levels.frequency;
		climateNoise.sample(px, pz, sample);
	}

	static ClimateNoise createClimate(INoiseGenerator generator) {
		if (generator == null)
			return null;
		return new ClimateNoise(generator.getContinent().getContext());
	}
}
