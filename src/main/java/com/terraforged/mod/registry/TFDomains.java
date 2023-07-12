package com.terraforged.mod.registry;

import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.noise.domain.AddWarp;
import com.terraforged.mod.noise.domain.CacheWarp;
import com.terraforged.mod.noise.domain.CompoundWarp;
import com.terraforged.mod.noise.domain.CumulativeWarp;
import com.terraforged.mod.noise.domain.DirectionWarp;
import com.terraforged.mod.noise.domain.Domain;
import com.terraforged.mod.noise.domain.DomainWarp;

import net.minecraft.resources.ResourceKey;

public interface TFDomains {
	ResourceKey<Codec<? extends Domain>> CACHE_WARP = resolve("cache_warp");
	ResourceKey<Codec<? extends Domain>> ADD_WARP = resolve("add_warp");
	ResourceKey<Codec<? extends Domain>> COMPOUND_WARP = resolve("compound_warp");
	ResourceKey<Codec<? extends Domain>> CUMULATIVE_WARP = resolve("cumulative_warp");
	ResourceKey<Codec<? extends Domain>> DIRECTION_WARP = resolve("direction_warp");
	ResourceKey<Codec<? extends Domain>> DOMAIN_WARP = resolve("domain_warp");
	
	static void register(BiConsumer<ResourceKey<Codec<? extends Domain>>, Codec<? extends Domain>> register) {
		register.accept(CACHE_WARP, CacheWarp.CODEC);
		register.accept(ADD_WARP, AddWarp.CODEC);
		register.accept(COMPOUND_WARP, CompoundWarp.CODEC);
		register.accept(CUMULATIVE_WARP, CumulativeWarp.CODEC);
		register.accept(DIRECTION_WARP, DirectionWarp.CODEC);
		register.accept(DOMAIN_WARP, DomainWarp.CODEC);
	}
	
	private static ResourceKey<Codec<? extends Domain>> resolve(String path) {
		return TerraForged.resolve(TerraForged.DOMAIN, path);
	}
}
