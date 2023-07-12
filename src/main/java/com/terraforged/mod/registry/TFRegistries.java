package com.terraforged.mod.registry;

import java.util.function.Consumer;

import com.terraforged.mod.TerraForged;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryBuilder;

public interface TFRegistries {
	
	static void register(Consumer<RegistryBuilder<?>> register) {
		register.accept(createRegistry(TerraForged.MODULE));
		register.accept(createRegistry(TerraForged.DOMAIN));
		register.accept(createRegistry(TerraForged.CURVE));
		register.accept(createRegistry(TerraForged.POPULATOR));
		register.accept(createRegistry(TerraForged.VIABILITY));
	}
	
	private static <T> RegistryBuilder<T> createRegistry(ResourceKey<Registry<T>> key) {
		ResourceLocation location = key.location();
		
		RegistryBuilder<T> builder = new RegistryBuilder<>();
		builder.setName(location);
		builder.hasTags();
		return builder.onCreate((owner, stage) -> {
			TerraForged.LOG.info("Created registry {}", key.registry() + "/" + location);
		});
	}
}
