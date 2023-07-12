package com.terraforged.mod.registry;

import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.TFChunkGenerator;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;

public interface TFChunkGenerators {
	ResourceKey<Codec<? extends ChunkGenerator>> TERRAFORGED = resolve("terraforged");
	
	static void register(BiConsumer<ResourceKey<Codec<? extends ChunkGenerator>>, Codec<? extends ChunkGenerator>> register) {
    	register.accept(TERRAFORGED, TFChunkGenerator.CODEC);
	}
	
	private static ResourceKey<Codec<? extends ChunkGenerator>> resolve(String path) {
		return TerraForged.resolve(Registries.CHUNK_GENERATOR, path);
	}
}
