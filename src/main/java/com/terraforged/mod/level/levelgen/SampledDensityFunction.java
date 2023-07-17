package com.terraforged.mod.level.levelgen;

import java.util.function.ToDoubleFunction;

import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.level.levelgen.climate.ClimateSample;
import com.terraforged.mod.level.levelgen.climate.ClimateSampler;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record SampledDensityFunction(ClimateSampler sampler, ToDoubleFunction<ClimateSample> getter) implements DensityFunction.SimpleFunction {

	@Override
	public double compute(FunctionContext ctx) {
		return -1;//this.getter.applyAsDouble(this.sampler.sample(ctx.blockX(), ctx.blockZ()));
	}

	@Override
	public double minValue() {
		return -1.0D;
	}

	@Override
	public double maxValue() {
		return 1.0D;
	}
	
	@Override
	public KeyDispatchDataCodec<? extends DensityFunction> codec() {
		return new KeyDispatchDataCodec<>(TFCodecs.error("not serializable"));
	}
}
