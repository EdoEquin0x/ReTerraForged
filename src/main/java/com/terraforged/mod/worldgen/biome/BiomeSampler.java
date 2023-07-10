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

import java.util.function.ToDoubleFunction;

import com.google.common.collect.ImmutableList;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.noise.climate.ClimateSample;

import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class BiomeSampler extends IBiomeSampler.Sampler {
    private BiomeSource delegate;
    private Climate.Sampler sampler;
    
    public BiomeSampler(INoiseGenerator noiseGenerator, BiomeSource delegate) {
        super(noiseGenerator);
        this.delegate = delegate;
    }
    
    public BiomeSource getDelegate() {
    	return this.delegate;
    }

    public Holder<Biome> sampleBiome(int seed, int x, int y, int z, Climate.Sampler sampler) {
    	if(this.sampler == null) {
    		this.sampler = this.initSampler(seed);
    	}
    	
    	return this.delegate.getNoiseBiome(x, y, z, this.sampler);
    }
    
    private Climate.Sampler initSampler(int seed) {
    	return new Climate.Sampler(
    		new SampledDensityFunction(seed, (sample) -> sample.temperature),
    		new SampledDensityFunction(seed, (sample) -> sample.moisture),
    		new SampledDensityFunction(seed, (sample) -> sample.continentNoise),
    		// river noise is weird, 1 means no river and 0 means river
    		// we invert it to make it less confusing
    		new SampledDensityFunction(seed, (sample) -> 1 - sample.riverNoise), 
    		DensityFunctions.constant(1.0D), // TODO: depth noise
    		new SampledDensityFunction(seed, (sample) -> sample.biomeNoise), // is this right?
    		ImmutableList.of()
    	);
    }
    
    private class SampledDensityFunction implements DensityFunction {
    	private int seed;
    	private ToDoubleFunction<ClimateSample> getter;
    	
    	public SampledDensityFunction(int seed, ToDoubleFunction<ClimateSample> getter) {
    		this.seed = seed;
    		this.getter = getter;
    	}
    	
		@Override
		public double compute(FunctionContext ctx) {
			ClimateSample sample = BiomeSampler.this.getSample(this.seed, ctx.blockX(), ctx.blockZ());
			return this.getter.applyAsDouble(sample);
		}

		@Override
		public void fillArray(double[] array, ContextProvider ctx) {
			throw new UnsupportedOperationException();
		}

		@Override
		public DensityFunction mapAll(Visitor vis) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double minValue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double maxValue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			throw new UnsupportedOperationException();
		}
    }
}

