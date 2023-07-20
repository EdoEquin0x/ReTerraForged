package com.terraforged.mod.level.levelgen.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.registry.data.TFClimates;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;

public record Climate(WeightMap<Holder<Biome>> biomes) {
	public static final Codec<Climate> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		WeightMap.codec(Biome.CODEC).fieldOf("biomes").forGetter(Climate::biomes)
	).apply(instance, Climate::new));
	public static final Codec<Holder<Climate>> CODEC = RegistryFileCodec.create(TFClimates.REGISTRY, DIRECT_CODEC);
}
