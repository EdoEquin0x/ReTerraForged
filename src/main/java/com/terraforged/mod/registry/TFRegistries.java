package com.terraforged.mod.registry;

import static com.terraforged.mod.TerraForged.registryKey;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.biome.viability.Viability;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.domain.Domain;
import com.terraforged.mod.noise.func.CurveFunc;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryBuilder;

public interface TFRegistries {
	ResourceKey<Registry<Codec<? extends Module>>> MODULE_TYPE = registryKey("noise/module_type");
	ResourceKey<Registry<Codec<? extends Domain>>> DOMAIN_TYPE = registryKey("noise/domain_type");
	ResourceKey<Registry<Codec<? extends CurveFunc>>> CURVE_TYPE = registryKey("noise/curve_type");
	ResourceKey<Registry<Codec<? extends Viability>>> VIABILITY_TYPE = registryKey("worldgen/viability_type");
	
	static void register(Consumer<RegistryBuilder<?>> register) {
		register.accept(createRegistry(MODULE_TYPE));
		register.accept(createRegistry(DOMAIN_TYPE));
		register.accept(createRegistry(CURVE_TYPE));
		register.accept(createRegistry(VIABILITY_TYPE));
	}
	
	private static <T> RegistryBuilder<T> createRegistry(ResourceKey<Registry<T>> key) {
		ResourceLocation location = key.location();
		
		RegistryBuilder<T> builder = new RegistryBuilder<>();
		builder.setName(location);
		builder.hasTags();
		return builder.onCreate((owner, stage) -> {
			TerraForged.LOG.info("Created registry {}", location);
		});
	}
}
