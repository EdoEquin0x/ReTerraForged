package com.terraforged.mod.registry;

import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.biome.viability.BiomeEdgeViability;
import com.terraforged.mod.level.levelgen.biome.viability.HeightViability;
import com.terraforged.mod.level.levelgen.biome.viability.MulViability;
import com.terraforged.mod.level.levelgen.biome.viability.NoiseViability;
import com.terraforged.mod.level.levelgen.biome.viability.SaturationViability;
import com.terraforged.mod.level.levelgen.biome.viability.SlopeViability;
import com.terraforged.mod.level.levelgen.biome.viability.SumViability;
import com.terraforged.mod.level.levelgen.biome.viability.Viability;

import net.minecraft.resources.ResourceKey;

public interface TFViabilities {
	ResourceKey<Codec<? extends Viability>> BIOME_EDGE = resolve("biome_edge");
	ResourceKey<Codec<? extends Viability>> HEIGHT = resolve("height");
	ResourceKey<Codec<? extends Viability>> MUL = resolve("mul");
	ResourceKey<Codec<? extends Viability>> NOISE = resolve("noise");
	ResourceKey<Codec<? extends Viability>> SATURATION = resolve("saturation");
	ResourceKey<Codec<? extends Viability>> SLOPE = resolve("slope");
	ResourceKey<Codec<? extends Viability>> SUM = resolve("sum");
	
	static void register(BiConsumer<ResourceKey<Codec<? extends Viability>>, Codec<? extends Viability>> register) {
		register.accept(BIOME_EDGE, BiomeEdgeViability.CODEC);
		register.accept(HEIGHT, HeightViability.CODEC);
		register.accept(MUL, MulViability.CODEC);
		register.accept(NOISE, NoiseViability.CODEC);
		register.accept(SATURATION, SaturationViability.CODEC);
		register.accept(SLOPE, SlopeViability.CODEC);
		register.accept(SUM, SumViability.CODEC);
	}
	
	private static ResourceKey<Codec<? extends Viability>> resolve(String path) {
		return TerraForged.resolve(TFRegistries.VIABILITY_TYPE, path);
	}
}
