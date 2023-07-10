package com.terraforged.mod.registry.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.storage.WeightMap;
import com.terraforged.mod.worldgen.TFGeneratorPreset;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.ParameterList;
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

public interface TFPresets {
	public static final ResourceKey<WorldPreset> TERRAFORGED = resolve("terraforged");
	
	static void register(BootstapContext<WorldPreset> ctx) {
		ctx.register(TERRAFORGED, createDefaultPreset(ctx));
    }
	
	@SuppressWarnings("unchecked")
	private static WorldPreset createDefaultPreset(BootstapContext<WorldPreset> ctx) {
		HolderGetter<DimensionType> dimensions = ctx.lookup(Registries.DIMENSION_TYPE);
		HolderGetter<TerrainNoise> terrain = ctx.lookup(TerraForged.TERRAIN);
		HolderGetter<VegetationConfig> vegetation = ctx.lookup(TerraForged.VEGETATION);
		HolderGetter<NoiseCave> caves = ctx.lookup(TerraForged.CAVE);
		HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
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
					TFGeneratorPreset.build(
						TerrainLevels.DEFAULT,
						new WeightMap.Builder<>()
							.entry(1.75F, terrain.getOrThrow(TFTerrain.BADLANDS))
							.entry(1.5F,  terrain.getOrThrow(TFTerrain.DALES))
							.entry(1.25F, terrain.getOrThrow(TFTerrain.DOLOMITES))
							.entry(2.0F,  terrain.getOrThrow(TFTerrain.HILLS_1))
							.entry(2.0F,  terrain.getOrThrow(TFTerrain.HILLS_2))
							.entry(1.25F, terrain.getOrThrow(TFTerrain.MOUNTAINS_1))
							.entry(1.25F, terrain.getOrThrow(TFTerrain.MOUNTAINS_2))
							.entry(1.25F, terrain.getOrThrow(TFTerrain.MOUNTAINS_3))
							.entry(1.25F, terrain.getOrThrow(TFTerrain.MOUNTAINS_RIDGE_1))
							.entry(1.25F, terrain.getOrThrow(TFTerrain.MOUNTAINS_RIDGE_2))
							.entry(2.5F,  terrain.getOrThrow(TFTerrain.PLAINS))
							.entry(2.0F,  terrain.getOrThrow(TFTerrain.PLATEAU))
							.entry(1.5F,  terrain.getOrThrow(TFTerrain.STEPPE))
							.entry(2.5F,  terrain.getOrThrow(TFTerrain.TORRIDONIAN))
							.build(),
						new Holder[] {
							vegetation.getOrThrow(TFVegetation.TREES_COPSE),
							vegetation.getOrThrow(TFVegetation.TREES_HARDY),
							vegetation.getOrThrow(TFVegetation.TREES_HARDY_SLOPES),
							vegetation.getOrThrow(TFVegetation.TREES_PATCHY),
							vegetation.getOrThrow(TFVegetation.TREES_RAINFOREST),
							vegetation.getOrThrow(TFVegetation.TREES_SPARSE),
							vegetation.getOrThrow(TFVegetation.TREES_SPARSE_RAINFOREST),
							vegetation.getOrThrow(TFVegetation.TREES_TEMPERATE)
						},
						new Holder[] {
							caves.getOrThrow(TFCaves.MEGA),
							caves.getOrThrow(TFCaves.MEGA_DEEP),
							caves.getOrThrow(TFCaves.SYNAPSE_HIGH),
							caves.getOrThrow(TFCaves.SYNAPSE_LOW),
							caves.getOrThrow(TFCaves.SYNAPSE_MID)
						}, 
						MultiNoiseBiomeSource.createFromList(createBiomes(biomes)),
						biomes,
						noiseSettings
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
	
	private static ParameterList<Holder<Biome>> createBiomes(HolderGetter<Biome> biomes) {
		List<Pair<Climate.ParameterPoint, Holder<Biome>>> params = new ArrayList<>();
		Collections.addAll(params, 
			Pair.of(
				Climate.parameters(
					Climate.Parameter.span(0.0F, 1.0F),
					Climate.Parameter.span(0.0F, 1.0F),
					Climate.Parameter.span(0.0F, 1.0F), 
					Climate.Parameter.span(0.0F, 0.95F),
					Climate.Parameter.span(0.0F, 1.0F),
					Climate.Parameter.span(0.0F, 1.0F),
					0.0F
				),
				biomes.getOrThrow(Biomes.PLAINS)
			),
			Pair.of(
				Climate.parameters(
					Climate.Parameter.span(0.0F, 1.0F),
					Climate.Parameter.span(0.0F, 1.0F),
					Climate.Parameter.span(0.0F, 1.0F), 
					Climate.Parameter.span(0.95F, 1.0F),
					Climate.Parameter.span(0.0F, 1.0F),
					Climate.Parameter.span(0.0F, 1.0F),
					0.0F
				),
				biomes.getOrThrow(Biomes.RIVER)
			)
		);
		return new ParameterList<>(params);
	}

	private static ResourceKey<WorldPreset> resolve(String path) {
		return TerraForged.resolve(Registries.WORLD_PRESET, path);
	}
}
