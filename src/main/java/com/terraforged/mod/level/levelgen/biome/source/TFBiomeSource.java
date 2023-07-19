package com.terraforged.mod.level.levelgen.biome.source;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.level.levelgen.climate.ClimateSample;
import com.terraforged.mod.level.levelgen.climate.ClimateSampler;
import com.terraforged.mod.util.codec.TFCodecs;
import com.terraforged.mod.util.pos.PosUtil;
import com.terraforged.mod.util.storage.LongCache;
import com.terraforged.mod.util.storage.LossyCache;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate.Sampler;

public class TFBiomeSource extends BiomeSource {
	private final LongCache<Holder<Biome>> cache = LossyCache.concurrent(2048, Holder[]::new);
	private final ClimateSampler climateSampler;
	private final ClimateTree.ParameterList params;
	
	public TFBiomeSource(ClimateSampler climateSampler, ClimateTree.ParameterList params) {
		this.climateSampler = climateSampler; 
		this.params = params;
	}
	
	public ClimateTree.ParameterList getParams() {
		return this.params;
	}
	
	public Holder<Climate> getClimate(ClimateSample sample) {
		return this.params.findValue(sample);
	}
	
	@Override
	protected Codec<TFBiomeSource> codec() {
		return TFCodecs.error("TODO");
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.params.values().stream().map(Pair::getSecond).flatMap((climate) -> {
			return climate.get().biomes().streamValues();
		});
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z, Sampler sampler) {
		return this.cache.computeIfAbsent(PosUtil.pack(x, z), (i) -> {
        	ClimateSample sample = this.climateSampler.sample(QuartPos.toBlock(x), QuartPos.toBlock(z));
        	return this.getClimate(sample).get().biomes().getValue(sample.biomeNoise);
        });
	}
}
