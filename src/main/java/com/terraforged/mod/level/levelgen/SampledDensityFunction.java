package com.terraforged.mod.level.levelgen;

import java.util.function.ToDoubleFunction;

import com.terraforged.mod.level.levelgen.climate.ClimateSampler;
import com.terraforged.mod.level.levelgen.noise.climate.ClimateSample;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public class SampledDensityFunction implements DensityFunction {
	private ClimateSampler sampler;
	private ToDoubleFunction<ClimateSample> getter;
	
	public SampledDensityFunction(ClimateSampler sampler, ToDoubleFunction<ClimateSample> getter) {
		this.sampler = sampler;
		this.getter = getter;
	}
	
	@Override
	public double compute(FunctionContext ctx) {
		ClimateSample sample = this.sampler.getSample(ctx.blockX(), ctx.blockZ());
		return this.getter.applyAsDouble(sample);
	}

	@Override
	public void fillArray(double[] array, ContextProvider ctx) {
		for(int i = 0; i < array.length; i++) {
			array[i] = this.compute(ctx.forIndex(i));
		}
	}

	@Override
	public DensityFunction mapAll(Visitor vis) {
		vis.apply(this);
		return this;
	}

	@Override
	public double minValue() {
		return 0.0D;
	}

	@Override
	public double maxValue() {
		return 1.0D;
	}

	@Override
	public KeyDispatchDataCodec<? extends DensityFunction> codec() {
		throw new UnsupportedOperationException();
	}
}
