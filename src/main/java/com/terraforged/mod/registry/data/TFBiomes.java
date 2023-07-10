/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.registry.data;

import javax.annotation.Nullable;

import com.terraforged.mod.TerraForged;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public interface TFBiomes {
	ResourceKey<Biome> CAVE = resolve("cave");
	ResourceKey<Biome> FOREST = resolve("forest");

	static void register(BootstapContext<Biome> ctx) {
		HolderGetter<PlacedFeature> featureGetter = ctx.lookup(Registries.PLACED_FEATURE);
		HolderGetter<ConfiguredWorldCarver<?>> carverGetter = ctx.lookup(Registries.CONFIGURED_CARVER);

		ctx.register(CAVE, createCave(featureGetter, carverGetter));
		ctx.register(FOREST, createForest(featureGetter, carverGetter));
	}
	
	private static ResourceKey<Biome> resolve(String path) {
		return TerraForged.resolve(Registries.BIOME, path);
	}

	private static Biome createCave(HolderGetter<PlacedFeature> featureGetter, HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.dripstoneCavesSpawns(spawns);
		BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(featureGetter, carverGetter);
		generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.LARGE_DRIPSTONE);
		generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.POINTED_DRIPSTONE);
		globalOverworldGeneration(generation);
		BiomeDefaultFeatures.addPlainGrass(generation);
		BiomeDefaultFeatures.addDefaultOres(generation, true);
		BiomeDefaultFeatures.addDefaultSoftDisks(generation);
		BiomeDefaultFeatures.addPlainVegetation(generation);
		BiomeDefaultFeatures.addDefaultMushrooms(generation);
		BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
		BiomeDefaultFeatures.addDripstone(generation);
		Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DRIPSTONE_CAVES);
		generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.LARGE_DRIPSTONE);
		generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.POINTED_DRIPSTONE);
		return biome(true, 0.8F, 0.4F, spawns, generation, music);
	}

	private static Biome createForest(HolderGetter<PlacedFeature> featureGetter, HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
		BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(featureGetter, carverGetter);
		globalOverworldGeneration(biomegenerationsettings$builder);
		BiomeDefaultFeatures.plainsSpawns(mobspawnsettings$builder);
		BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
		
		BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
		BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
		BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings$builder);
		BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
		return biome(true, 0.8F, 0.4F, mobspawnsettings$builder, biomegenerationsettings$builder, null);
	}

	private static void globalOverworldGeneration(BiomeGenerationSettings.Builder generation) {
		BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
		BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
		BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
		BiomeDefaultFeatures.addDefaultSprings(generation);
		BiomeDefaultFeatures.addSurfaceFreezing(generation);
	}

	private static Biome biome(boolean hasPrecipitation, float skyColor, float downfall, MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder generation, @Nullable Music music) {
		return biome(hasPrecipitation, skyColor, downfall, 4159204, 329011, (Integer) null, (Integer) null, spawns, generation, music);
	}

	private static Biome biome(boolean hasPrecipitation, float skyColor, float downfall, int waterColor, int waterFogColor, @Nullable Integer grassColorOverride, @Nullable Integer foliageColorOverride, MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder generation, @Nullable Music music) {
		BiomeSpecialEffects.Builder biomespecialeffects$builder = (new BiomeSpecialEffects.Builder())
			.waterColor(waterColor).waterFogColor(waterFogColor).fogColor(12638463)
			.skyColor(calculateSkyColor(skyColor)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
			.backgroundMusic(music);
		if (grassColorOverride != null) {
			biomespecialeffects$builder.grassColorOverride(grassColorOverride);
		}

		if (foliageColorOverride != null) {
			biomespecialeffects$builder.foliageColorOverride(foliageColorOverride);
		}

		return (new Biome.BiomeBuilder()).hasPrecipitation(hasPrecipitation).temperature(skyColor).downfall(downfall)
				.specialEffects(biomespecialeffects$builder.build()).mobSpawnSettings(spawns.build())
				.generationSettings(generation.build()).build();
	}

	private static int calculateSkyColor(float color) {
		float f = color / 3.0F;
		f = Mth.clamp(f, -1.0F, 1.0F);
		return Mth.hsvToRgb(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1.0F);
	}
}
