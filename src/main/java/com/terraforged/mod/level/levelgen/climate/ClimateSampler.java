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

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.continent.ContinentNoise;
import com.terraforged.mod.level.levelgen.noise.TerrainNoise;
import com.terraforged.mod.level.levelgen.util.NoiseTree;
import com.terraforged.mod.level.levelgen.util.NoiseTree.Point;

import net.minecraft.core.Holder;

public class ClimateSampler {
	protected final ClimateNoise climate;
	protected final TerrainNoise terrain;
	protected final ThreadLocal<ClimateSample> localSample = ThreadLocal.withInitial(ClimateSample::new);

	public ClimateSampler(TerrainNoise terrainNoise) {
		this.climate = createClimate(terrainNoise);
		this.terrain = terrainNoise;
	}

	public ClimateSample sample(int x, int z) {
		var sample = this.localSample.get().reset();
		this.terrain.sample(x, z, sample);
		this.climate.sample(x, z, sample);
		return sample;
	}
	
	private static ClimateNoise createClimate(TerrainNoise noise) {
		ContinentNoise continent = noise.getContinent();
		return new ClimateNoise(continent.getSeed(), continent.getSettings(), noise.getLevels());
	}
	
	public record ParameterPoint(Holder<Climate> climate, NoiseTree.Parameter temperature, NoiseTree.Parameter moisture, NoiseTree.Parameter continentalness, NoiseTree.Parameter height, NoiseTree.Parameter river) implements Point<Holder<Climate>> {
		public static final Codec<ClimateSampler.ParameterPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Climate.CODEC.fieldOf("climate").forGetter(ClimateSampler.ParameterPoint::climate), 
			NoiseTree.Parameter.CODEC.fieldOf("temperature").forGetter(ClimateSampler.ParameterPoint::temperature), 
			NoiseTree.Parameter.CODEC.fieldOf("moisture").forGetter(ClimateSampler.ParameterPoint::moisture), 
			NoiseTree.Parameter.CODEC.fieldOf("continentalness").forGetter(ClimateSampler.ParameterPoint::continentalness), 
			NoiseTree.Parameter.CODEC.fieldOf("height").forGetter(ClimateSampler.ParameterPoint::height), 
			NoiseTree.Parameter.CODEC.fieldOf("river").forGetter(ClimateSampler.ParameterPoint::river)
		).apply(instance, ClimateSampler.ParameterPoint::new));

		@Override
		public Holder<Climate> value() {
			return this.climate;
		}

		@Override
		public List<NoiseTree.Parameter> parameterSpace() {
			return ImmutableList.of(this.temperature, this.moisture, this.continentalness, this.height, this.river);
		}

		public static ClimateSampler.ParameterPoint of(Holder<Climate> climate, float temperature, float moisture, float continentalness, float height, float river) {
			return new ClimateSampler.ParameterPoint(climate, NoiseTree.Parameter.point(temperature), NoiseTree.Parameter.point(moisture), NoiseTree.Parameter.point(continentalness), NoiseTree.Parameter.point(height), NoiseTree.Parameter.point(river));
		}

		public static ClimateSampler.ParameterPoint of(Holder<Climate> climate, NoiseTree.Parameter temperature, NoiseTree.Parameter moisture, NoiseTree.Parameter continentalness, NoiseTree.Parameter height, NoiseTree.Parameter river) {
			return new ClimateSampler.ParameterPoint(climate, temperature, moisture, continentalness, height, river);
		}
	}
}
