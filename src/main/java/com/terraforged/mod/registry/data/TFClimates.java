package com.terraforged.mod.registry.data;

import static com.terraforged.mod.TerraForged.registryKey;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public interface TFClimates {
	ResourceKey<Registry<Climate>> REGISTRY = registryKey("worldgen/climate");
	
	ResourceKey<Climate> MID_ALTITUDE = resolve("mid_altitude");
	ResourceKey<Climate> HIGH_ALTITUDE = resolve("high_altitude");
	ResourceKey<Climate> RIVER = resolve("river");
	ResourceKey<Climate> BEACH = resolve("beach");
	ResourceKey<Climate> OCEAN = resolve("ocean");
	ResourceKey<Climate> CAVE = resolve("cave");
	
	static void register(BootstapContext<Climate> ctx) {
		HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
		
		ctx.register(MID_ALTITUDE, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.PLAINS))
				.build()
		));
		ctx.register(HIGH_ALTITUDE, new Climate(
			new WeightMap.Builder<>()
				.entry(0.5F, biomes.getOrThrow(Biomes.SNOWY_SLOPES))
				.entry(0.5F, biomes.getOrThrow(Biomes.FROZEN_PEAKS))
				.build()
		));
		ctx.register(RIVER, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.RIVER))
				.build()
		));
		ctx.register(BEACH, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.BEACH))
				.build()
		));
		ctx.register(OCEAN, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.OCEAN))
				.build()
		));
		ctx.register(CAVE, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.DRIPSTONE_CAVES))
				.build()
		));
	}
	
	private static ResourceKey<Climate> resolve(String path) {
		return TerraForged.resolve(REGISTRY, path);
	}
}
