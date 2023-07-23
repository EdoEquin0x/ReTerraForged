package com.terraforged.mod.registry.data;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.biome.vegetation.VegetationConfig;
import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.level.levelgen.climate.ClimateSampler;
import com.terraforged.mod.level.levelgen.generator.TFChunkGenerator;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.terrain.TerrainLevels;
import com.terraforged.mod.level.levelgen.util.NoiseTree;
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
	ResourceKey<WorldPreset> TERRAFORGED = resolve("terraforged");
	ResourceKey<WorldPreset> VANILLA = resolve("vanilla");
	
	static void register(BootstapContext<WorldPreset> ctx) {
		ctx.register(TERRAFORGED, createTFPreset(ctx));
//		ctx.register(VANILLA, createVanillaPreset(ctx));
    }

	static WorldPreset createVanillaPreset(BootstapContext<WorldPreset> ctx) {
		HolderGetter<DimensionType> dimensions = ctx.lookup(Registries.DIMENSION_TYPE);
		HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
		HolderGetter<NoiseGeneratorSettings> noiseSettings = ctx.lookup(Registries.NOISE_SETTINGS);
		return new WorldPreset(
			ImmutableMap.<ResourceKey<LevelStem>, LevelStem>builder()
			.put(LevelStem.NETHER, 
				new LevelStem(
					dimensions.getOrThrow(BuiltinDimensionTypes.NETHER), 
					new NoiseBasedChunkGenerator(
						MultiNoiseBiomeSource.createFromList(new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.NETHER, biomes).parameters()),
						noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER)
					)
				)
			)
			.put(LevelStem.OVERWORLD, 
				new LevelStem(
					dimensions.getOrThrow(BuiltinDimensionTypes.OVERWORLD), 
					new NoiseBasedChunkGenerator(
						MultiNoiseBiomeSource.createFromList(new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, biomes).parameters()),
						noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
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
	
	private static WorldPreset createTFPreset(BootstapContext<WorldPreset> ctx) {
		HolderGetter<DimensionType> dimensions = ctx.lookup(Registries.DIMENSION_TYPE);
		HolderGetter<Module> modules = ctx.lookup(TFNoise.REGISTRY);
		HolderGetter<VegetationConfig> vegetation = ctx.lookup(TFVegetation.REGISTRY);
		HolderGetter<NoiseCave> caves = ctx.lookup(TFCaves.REGISTRY);
		HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
		HolderGetter<Climate> climates = ctx.lookup(TFClimates.REGISTRY);
		HolderGetter<NoiseGeneratorSettings> noiseSettings = ctx.lookup(Registries.NOISE_SETTINGS);
		HolderGetter<MultiNoiseBiomeSourceParameterList> biomeLists = ctx.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
		return new WorldPreset(
			ImmutableMap.<ResourceKey<LevelStem>, LevelStem>builder()
			.put(LevelStem.NETHER, 
				new LevelStem(
					dimensions.getOrThrow(BuiltinDimensionTypes.NETHER), 
					new NoiseBasedChunkGenerator(
						MultiNoiseBiomeSource.createFromPreset(biomeLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)),
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
							.entry(0.55F, modules.getOrThrow(TFNoise.STEPPE))
							.entry(0.65F, modules.getOrThrow(TFNoise.PLAINS))
							.entry(0.55F, modules.getOrThrow(TFNoise.HILLS_1))
							.entry(0.55F, modules.getOrThrow(TFNoise.HILLS_2))
							.entry(0.65F, modules.getOrThrow(TFNoise.DALES))
							.entry(0.45F, modules.getOrThrow(TFNoise.PLATEAU))
							.entry(0.65F, modules.getOrThrow(TFNoise.BADLANDS))
							.entry(0.65F, modules.getOrThrow(TFNoise.TORRIDONIAN))
							.entry(0.55F, modules.getOrThrow(TFNoise.MOUNTAINS_1))
							.entry(0.55F, modules.getOrThrow(TFNoise.MOUNTAINS_2))
							.entry(0.55F, modules.getOrThrow(TFNoise.MOUNTAINS_3))
							.entry(0.65F, modules.getOrThrow(TFNoise.DOLOMITES))
							.entry(0.55F, modules.getOrThrow(TFNoise.MOUNTAINS_RIDGE_1))
							.entry(0.55F, modules.getOrThrow(TFNoise.MOUNTAINS_RIDGE_2))
//							.entry(1.0F, modules.getOrThrow(TFNoise.TEST))
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
							caves.getOrThrow(TFCaves.MEGA),
							caves.getOrThrow(TFCaves.MEGA_DEEP),
							caves.getOrThrow(TFCaves.SYNAPSE_HIGH),
							caves.getOrThrow(TFCaves.SYNAPSE_LOW),
							caves.getOrThrow(TFCaves.SYNAPSE_MID),
							caves.getOrThrow(TFCaves.IRON_VEIN),
							caves.getOrThrow(TFCaves.COPPER_VEIN),
							caves.getOrThrow(TFCaves.DEEP_LAVA_GEN)
						),
						new NoiseTree.ParameterList<>(5, getClimates(climates))
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
	
	private static List<ClimateSampler.ParameterPoint> getClimates(HolderGetter<Climate> climates) {
		final float beachSize = 0.1F;
		final float inlandThreshold = 0.51F;
		final float beachThreshold = inlandThreshold - beachSize;
		//rivers noise goes from 1 - 0 with 0 being a river and 1 being normal land
		//this implies that rivers are default and land is special
		//this was inherited from terraforged and is likely gonna change as it doesn't make much sense (terrain can exist w/o rivers but not vice versa)
		final float nonRiverThreshold = 0.05F;
		final float baseAltitudePoint = 0.3F;
		final float highLowAltitudePoint = 0.4F;
		final float highMidAltitudePoint = 0.45F;
		final float peakPoint = 0.5F;
		return ImmutableList.of(
			ClimateSampler.ParameterPoint.of(
				climates.getOrThrow(TFClimates.TEMPERATE),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.min(inlandThreshold),
				NoiseTree.Parameter.point(baseAltitudePoint),
				NoiseTree.Parameter.min(nonRiverThreshold)
			),
			ClimateSampler.ParameterPoint.of(
				climates.getOrThrow(TFClimates.HIGH_LOW),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.min(inlandThreshold),
				NoiseTree.Parameter.point(highLowAltitudePoint),
				NoiseTree.Parameter.min(nonRiverThreshold)
			),
			ClimateSampler.ParameterPoint.of(
				climates.getOrThrow(TFClimates.HIGH_MID),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.min(inlandThreshold),
				NoiseTree.Parameter.point(highMidAltitudePoint),
				NoiseTree.Parameter.min(nonRiverThreshold)
			),
			ClimateSampler.ParameterPoint.of(
				climates.getOrThrow(TFClimates.PEAK),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.min(inlandThreshold),
				NoiseTree.Parameter.point(peakPoint),
				NoiseTree.Parameter.min(nonRiverThreshold)
			),
			ClimateSampler.ParameterPoint.of(
				climates.getOrThrow(TFClimates.TEMPERATE_RIVER),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.min(inlandThreshold),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.max(nonRiverThreshold)
			),
			ClimateSampler.ParameterPoint.of(
				climates.getOrThrow(TFClimates.TEMPERATE_BEACH),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.span(beachThreshold, inlandThreshold),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore()
			),
			ClimateSampler.ParameterPoint.of(
				climates.getOrThrow(TFClimates.TEMPERATE_OCEAN),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.max(beachThreshold),
				NoiseTree.Parameter.ignore(),
				NoiseTree.Parameter.ignore()
			)
		);
	}
	
	private static ResourceKey<WorldPreset> resolve(String path) {
		return TerraForged.resolve(Registries.WORLD_PRESET, path);
	}
}
