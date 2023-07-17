package com.terraforged.mod.registry.data;

import static com.terraforged.mod.TerraForged.registryKey;

import java.util.function.Consumer;

import com.terraforged.mod.level.levelgen.biome.vegetation.VegetationConfig;
import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.noise.Module;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface TFDataRegistries {
	ResourceKey<Registry<NoiseCave>> CAVE = registryKey("worldgen/cave");
	ResourceKey<Registry<VegetationConfig>> VEGETATION = registryKey("worldgen/vegetation");
	ResourceKey<Registry<Module>> NOISE = registryKey("worldgen/noise");

	static void register(Consumer<ResourceKey<?>> register) {
		register.accept(CAVE);
		register.accept(VEGETATION);
		register.accept(NOISE);
	}
}
