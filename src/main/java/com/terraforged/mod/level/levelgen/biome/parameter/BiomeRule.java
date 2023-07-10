package com.terraforged.mod.level.levelgen.biome.parameter;

public interface BiomeRule<T> {
	float fitness(T config, float temperature, float moisture, float continent, float river);
}