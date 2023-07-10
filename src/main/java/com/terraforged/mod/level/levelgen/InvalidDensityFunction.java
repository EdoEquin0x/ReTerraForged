package com.terraforged.mod.level.levelgen;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

@Deprecated(forRemoval = true)
public class InvalidDensityFunction implements DensityFunction {
	public static final InvalidDensityFunction INSTANCE = new InvalidDensityFunction();
	
	private InvalidDensityFunction() {
	}
	
	@Override
	public double compute(FunctionContext ctx) {
		throw exception();
	}

	@Override
	public void fillArray(double[] array, ContextProvider ctx) {
		throw exception();
	}
	
	@Override
	public DensityFunction mapAll(Visitor vis) {
		return this;
	}

	@Override
	public double minValue() {
		return 0.0D;
	}

	@Override
	public double maxValue() {
		return 0.0D;
	}

	@Override
	public KeyDispatchDataCodec<? extends DensityFunction> codec() {
		throw exception();
	}
	
	private static RuntimeException exception() {
		throw new UnsupportedOperationException("Invalid");
	}
}
