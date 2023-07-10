package com.terraforged.mod.worldgen.asset.builtin;

import com.terraforged.mod.worldgen.noise.climate.ClimateSample;

public interface BiomeFitness<C> {
	float apply(C config, ClimateSample climate);
}
