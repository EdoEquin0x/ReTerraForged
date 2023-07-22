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
	
	ResourceKey<Climate> TEMPERATE = resolve("temperate");
	ResourceKey<Climate> TEMPERATE_RIVER = resolve("temperate_river");
	ResourceKey<Climate> TEMPERATE_BEACH = resolve("temperate_beach");
	ResourceKey<Climate> TEMPERATE_OCEAN = resolve("temperate_ocean");

	ResourceKey<Climate> COLD_DRY = resolve("cold_dry");
	ResourceKey<Climate> COLD_WET = resolve("cold_wet");
	ResourceKey<Climate> COLD_RIVER = resolve("cold_river");
	ResourceKey<Climate> COLD_BEACH = resolve("cold_beach");
	ResourceKey<Climate> COLD_OCEAN = resolve("cold_ocean");
	
	ResourceKey<Climate> FROZEN = resolve("frozen");
	ResourceKey<Climate> FROZEN_RIVER = resolve("frozen_river");
	ResourceKey<Climate> FROZEN_BEACH = resolve("frozen_beach");
	ResourceKey<Climate> FROZEN_OCEAN = resolve("frozen_ocean");
	
	ResourceKey<Climate> HIGH_LOW = resolve("high_low");
	ResourceKey<Climate> HIGH_MID = resolve("high_mid");
	ResourceKey<Climate> PEAK = resolve("peak");
	
	ResourceKey<Climate> CAVE = resolve("cave");
	ResourceKey<Climate> CAVE_DEEP = resolve("cave_deep");
	
	static void register(BootstapContext<Climate> ctx) {
		HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
		
		registerTemperate(ctx, biomes);
		registerHigh(ctx, biomes);
		registerCave(ctx, biomes);
	}
	
	private static void registerTemperate(BootstapContext<Climate> ctx, HolderGetter<Biome> biomes) {
		ctx.register(TEMPERATE, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.PLAINS))
				.entry(0.5F, biomes.getOrThrow(Biomes.SUNFLOWER_PLAINS))
				.entry(1.0F, biomes.getOrThrow(Biomes.FOREST))
				.entry(0.75F, biomes.getOrThrow(Biomes.BIRCH_FOREST))
				.entry(0.5F, biomes.getOrThrow(Biomes.FLOWER_FOREST))
				.build()
		));
		ctx.register(TEMPERATE_RIVER, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.RIVER))
				.build()
		));
		ctx.register(TEMPERATE_BEACH, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.BEACH))
				.build()
		));
		ctx.register(TEMPERATE_OCEAN, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.OCEAN))
				.build()
		));
	}
	
	private static void registerHigh(BootstapContext<Climate> ctx, HolderGetter<Biome> biomes) {
		ctx.register(HIGH_LOW, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.MEADOW))
				.entry(1.0F, biomes.getOrThrow(Biomes.FOREST))
				.build()
		));
		ctx.register(HIGH_MID, new Climate(
			new WeightMap.Builder<>()
				.entry(0.7F, biomes.getOrThrow(Biomes.GROVE))
				.entry(0.8F, biomes.getOrThrow(Biomes.WINDSWEPT_FOREST))
				.entry(1.0F, biomes.getOrThrow(Biomes.SNOWY_SLOPES))
				.build()
		));
		ctx.register(PEAK, new Climate(
			new WeightMap.Builder<>()
				.entry(0.75F, biomes.getOrThrow(Biomes.FROZEN_PEAKS))
				.entry(0.25F, biomes.getOrThrow(Biomes.SNOWY_SLOPES))
				.entry(0.25F, biomes.getOrThrow(Biomes.STONY_PEAKS))
				.build()
		));
	}
	
	private static void registerCave(BootstapContext<Climate> ctx, HolderGetter<Biome> biomes) {
		ctx.register(CAVE, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.DRIPSTONE_CAVES))
				.entry(0.5F, biomes.getOrThrow(Biomes.LUSH_CAVES))
				.build()
		));
		ctx.register(CAVE_DEEP, new Climate(
			new WeightMap.Builder<>()
				.entry(1.0F, biomes.getOrThrow(Biomes.DRIPSTONE_CAVES))
				.entry(0.25F, biomes.getOrThrow(Biomes.DEEP_DARK))
				.build()
		));
	}
	
	private static ResourceKey<Climate> resolve(String path) {
		return TerraForged.resolve(REGISTRY, path);
	}
}
