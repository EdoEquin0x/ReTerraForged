package com.terraforged.mod.data;

import com.google.common.collect.ImmutableMap;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.storage.WeightMap;
import com.terraforged.mod.worldgen.GeneratorPreset;
import com.terraforged.mod.worldgen.asset.ClimateType;
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

public interface ModPresets {
	public static final ResourceKey<WorldPreset> TERRAFORGED = resolve("terraforged");
	
	static void register(BootstapContext<WorldPreset> ctx) {
		ctx.register(TERRAFORGED, createDefaultPreset(ctx));
    }
	
	@SuppressWarnings("unchecked")
	private static WorldPreset createDefaultPreset(BootstapContext<WorldPreset> ctx) {
		HolderGetter<DimensionType> dimensions = ctx.lookup(Registries.DIMENSION_TYPE);
		HolderGetter<ClimateType> climates = ctx.lookup(TerraForged.CLIMATES);
		HolderGetter<TerrainNoise> terrain = ctx.lookup(TerraForged.TERRAIN);
		HolderGetter<VegetationConfig> vegetation = ctx.lookup(TerraForged.VEGETATION);
		//HolderGetter<Structure> structures = ctx.lookup(Registries.STRUCTURE);
		HolderGetter<NoiseCave> caves = ctx.lookup(TerraForged.CAVES);
		HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
		HolderGetter<NoiseGeneratorSettings> noiseSettings = ctx.lookup(Registries.NOISE_SETTINGS);
		HolderGetter<MultiNoiseBiomeSourceParameterList> parameters = ctx.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
		return new WorldPreset(
			ImmutableMap.<ResourceKey<LevelStem>, LevelStem>builder()
			.put(LevelStem.NETHER, 
				new LevelStem(
					dimensions.getOrThrow(BuiltinDimensionTypes.NETHER), 
					new NoiseBasedChunkGenerator(
						MultiNoiseBiomeSource.createFromPreset(parameters.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)),
						noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER)
					)
				)
			)
			.put(LevelStem.OVERWORLD, 
				new LevelStem(
					dimensions.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
					GeneratorPreset.build(
						TerrainLevels.DEFAULT, 
						ImmutableMap.<BiomeType, Holder<ClimateType>>builder()
							.put(BiomeType.TROPICAL_RAINFOREST, climates.getOrThrow(ModClimates.TROPICAL_RAINFOREST))
							.put(BiomeType.SAVANNA, climates.getOrThrow(ModClimates.SAVANNA))
							.put(BiomeType.DESERT, climates.getOrThrow(ModClimates.DESERT))
							.put(BiomeType.TEMPERATE_RAINFOREST, climates.getOrThrow(ModClimates.TEMPERATE_RAINFOREST))
							.put(BiomeType.TEMPERATE_FOREST, climates.getOrThrow(ModClimates.TEMPERATE_FOREST))
							.put(BiomeType.GRASSLAND, climates.getOrThrow(ModClimates.GRASSLAND))
							.put(BiomeType.COLD_STEPPE, climates.getOrThrow(ModClimates.COLD_STEPPE))
							.put(BiomeType.STEPPE, climates.getOrThrow(ModClimates.STEPPE))
							.put(BiomeType.TAIGA, climates.getOrThrow(ModClimates.TAIGA))
							.put(BiomeType.TUNDRA, climates.getOrThrow(ModClimates.TUNDRA))
							.put(BiomeType.ALPINE, climates.getOrThrow(ModClimates.ALPINE))
							.build(), 
						new WeightMap.Builder<>()
							.entry(1.75F, terrain.getOrThrow(ModTerrain.BADLANDS))
							.entry(1.5F,  terrain.getOrThrow(ModTerrain.DALES))
							.entry(1.25F, terrain.getOrThrow(ModTerrain.DOLOMITES))
							.entry(2.0F,  terrain.getOrThrow(ModTerrain.HILLS_1))
							.entry(2.0F,  terrain.getOrThrow(ModTerrain.HILLS_2))
							.entry(1.25F, terrain.getOrThrow(ModTerrain.MOUNTAINS_1))
							.entry(1.25F, terrain.getOrThrow(ModTerrain.MOUNTAINS_2))
							.entry(1.25F, terrain.getOrThrow(ModTerrain.MOUNTAINS_3))
							.entry(1.25F, terrain.getOrThrow(ModTerrain.MOUNTAINS_RIDGE_1))
							.entry(1.25F, terrain.getOrThrow(ModTerrain.MOUNTAINS_RIDGE_2))
							.entry(2.5F,  terrain.getOrThrow(ModTerrain.PLAINS))
							.entry(2.0F,  terrain.getOrThrow(ModTerrain.PLATEAU))
							.entry(1.5F,  terrain.getOrThrow(ModTerrain.STEPPE))
							.entry(2.5F,  terrain.getOrThrow(ModTerrain.TORRIDONIAN))
							.build(),
						new Holder[] {
							vegetation.getOrThrow(ModVegetation.TREES_COPSE),
							vegetation.getOrThrow(ModVegetation.TREES_HARDY),
							vegetation.getOrThrow(ModVegetation.TREES_HARDY_SLOPES),
							vegetation.getOrThrow(ModVegetation.TREES_PATCHY),
							vegetation.getOrThrow(ModVegetation.TREES_RAINFOREST),
							vegetation.getOrThrow(ModVegetation.TREES_SPARSE),
							vegetation.getOrThrow(ModVegetation.TREES_SPARSE_RAINFOREST),
							vegetation.getOrThrow(ModVegetation.TREES_TEMPERATE)
						},
						new Holder[] {
							//TODO
						}, 
						new Holder[] {
							caves.getOrThrow(ModCaves.MEGA),
							caves.getOrThrow(ModCaves.MEGA_DEEP),
							caves.getOrThrow(ModCaves.SYNAPSE_HIGH),
							caves.getOrThrow(ModCaves.SYNAPSE_LOW),
							caves.getOrThrow(ModCaves.SYNAPSE_MID)
						}, 
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
	
	private static ResourceKey<WorldPreset> resolve(String path) {
		return TerraForged.resolve(Registries.WORLD_PRESET, path);
	}
}
