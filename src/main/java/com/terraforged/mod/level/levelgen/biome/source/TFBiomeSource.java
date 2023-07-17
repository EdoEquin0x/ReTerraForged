package com.terraforged.mod.level.levelgen.biome.source;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.util.pos.PosUtil;
import com.terraforged.mod.util.storage.LongCache;
import com.terraforged.mod.util.storage.LossyCache;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class TFBiomeSource extends BiomeSource {
	@SuppressWarnings("unchecked")
	private final LongCache<Holder<Biome>> cache = LossyCache.concurrent(2048, i -> (Holder<Biome>[]) new Holder[i]);
	private final BiomeSource delegate;
	
	public TFBiomeSource(BiomeSource delegate) {
		this.delegate = delegate;
	}
	
	public BiomeSource getDelegate() {
		return this.delegate;
	}
	
	@Override
	protected Codec<TFBiomeSource> codec() {
		return TFCodecs.error("TODO");
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.delegate.possibleBiomes().stream();
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return this.cache.computeIfAbsent(PosUtil.pack(x, z), (i) -> this.delegate.getNoiseBiome(x, y, z, sampler));
	}
}
