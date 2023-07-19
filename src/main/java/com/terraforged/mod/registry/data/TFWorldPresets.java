package com.terraforged.mod.registry.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.biome.source.ClimateTree;
import com.terraforged.mod.level.levelgen.biome.vegetation.VegetationConfig;
import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.level.levelgen.generator.TFChunkGenerator;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.terrain.TerrainLevels;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public interface TFWorldPresets {
	public static final ResourceKey<WorldPreset> TERRAFORGED = resolve("terraforged");
	
	static void register(BootstapContext<WorldPreset> ctx) {
		ctx.register(TERRAFORGED, createDefaultPreset(ctx));
    }

	private static WorldPreset createDefaultPreset(BootstapContext<WorldPreset> ctx) {
		HolderGetter<DimensionType> dimensions = ctx.lookup(Registries.DIMENSION_TYPE);
		HolderGetter<Module> terrain = ctx.lookup(TFNoise.REGISTRY);
		HolderGetter<VegetationConfig> vegetation = ctx.lookup(TFVegetation.REGISTRY);
		HolderGetter<NoiseCave> caves = ctx.lookup(TFCaves.REGISTRY);
		HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
		HolderGetter<Climate> climates = ctx.lookup(TFClimates.REGISTRY);
		HolderGetter<NoiseGeneratorSettings> noiseSettings = ctx.lookup(Registries.NOISE_SETTINGS);
		HolderGetter<MultiNoiseBiomeSourceParameterList> presets = ctx.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
		return new WorldPreset(
			ImmutableMap.<ResourceKey<LevelStem>, LevelStem>builder()
			.put(LevelStem.NETHER, 
				new LevelStem(
					dimensions.getOrThrow(BuiltinDimensionTypes.NETHER), 
					new NoiseBasedChunkGenerator(
						MultiNoiseBiomeSource.createFromPreset(presets.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)),
						noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER)
					)
				)
			)
			.put(LevelStem.OVERWORLD, 
				new LevelStem(
					dimensions.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
					TFChunkGenerator.create(
						Settings.DEFAULT,
						TerrainLevels.DEFAULT,
						new WeightMap.Builder<>()
							.entry(1.45F, terrain.getOrThrow(TFNoise.MOUNTAINS_1))
							.entry(1.5F,  terrain.getOrThrow(TFNoise.PLAINS))
							.build(),
						HolderSet.direct(
							vegetation.getOrThrow(TFVegetation.TREES_COPSE),
							vegetation.getOrThrow(TFVegetation.TREES_HARDY),
							vegetation.getOrThrow(TFVegetation.TREES_HARDY_SLOPES),
							vegetation.getOrThrow(TFVegetation.TREES_PATCHY),
							vegetation.getOrThrow(TFVegetation.TREES_RAINFOREST),
							vegetation.getOrThrow(TFVegetation.TREES_SPARSE),
							vegetation.getOrThrow(TFVegetation.TREES_SPARSE_RAINFOREST),
							vegetation.getOrThrow(TFVegetation.TREES_TEMPERATE)
						),
						HolderSet.direct(
//							caves.getOrThrow(TFCaves.MEGA),
//							caves.getOrThrow(TFCaves.MEGA_DEEP),
							caves.getOrThrow(TFCaves.SYNAPSE_HIGH),
							caves.getOrThrow(TFCaves.SYNAPSE_LOW),
							caves.getOrThrow(TFCaves.SYNAPSE_MID)
						),
						new ClimateTree.ParameterList(ImmutableList.of(
							Pair.of(
								ClimateTree.parameters(
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.span(0.51F, 1.0F),
									ClimateTree.Parameter.span(0.0F, 0.41F),
									ClimateTree.Parameter.span(0.05F, 1.0F)
								),
								climates.getOrThrow(TFClimates.MID_ALTITUDE)
							),
							Pair.of(
								ClimateTree.parameters(
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.span(0.51F, 1.0F),
									ClimateTree.Parameter.span(0.41F, 1.0F),
									ClimateTree.Parameter.span(0.05F, 1.0F)
								),
								climates.getOrThrow(TFClimates.HIGH_ALTITUDE)
							),
							Pair.of(
								ClimateTree.parameters(
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.span(0.51F, 1.0F),
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.span(0.0F, 0.05F)
								),
								climates.getOrThrow(TFClimates.RIVER)
							),
							Pair.of(
								ClimateTree.parameters(
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.span(0.5F, 0.51F),
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.any()
								),
								climates.getOrThrow(TFClimates.BEACH)
							),
							Pair.of(
								ClimateTree.parameters(
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.span(0.0F, 0.5F),
									ClimateTree.Parameter.any(),
									ClimateTree.Parameter.any()
								),
								climates.getOrThrow(TFClimates.OCEAN)
							)
						))
					)
				)
			)
			.put(LevelStem.END,
				new LevelStem(
					dimensions.getOrThrow(BuiltinDimensionTypes.END), 
					new NoiseBasedChunkGenerator(
						TheEndBiomeSource.create(biomes),
						noiseSettings.getOrThrow(NoiseGeneratorSettings.END)
					)
				)
			)
			.build());
	}
	
	private static ResourceKey<WorldPreset> resolve(String path) {
		return TerraForged.resolve(Registries.WORLD_PRESET, path);
	}
}
