package com.terraforged.mod.level.levelgen;

import java.util.function.ToDoubleFunction;

import com.terraforged.mod.util.codec.TFCodecs;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ProvidedDensityFunction(ToDoubleFunction<FunctionContext> getter) implements DensityFunction.SimpleFunction {

	@Override
	public double compute(FunctionContext ctx) {
		return this.getter.applyAsDouble(ctx);
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
		return new KeyDispatchDataCodec<>(TFCodecs.forError("not serializable"));
	}
}
