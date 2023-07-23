package com.terraforged.mod.level.levelgen.biome.source;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.level.levelgen.climate.ClimateSample;
import com.terraforged.mod.level.levelgen.climate.ClimateSampler;
import com.terraforged.mod.level.levelgen.util.NoiseTree;
import com.terraforged.mod.util.codec.TFCodecs;
import com.terraforged.mod.util.pos.PosUtil;
import com.terraforged.mod.util.storage.LongCache;
import com.terraforged.mod.util.storage.LossyCache;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate.Sampler;

//TODO move this behavior to ClimateSampler
public class TFBiomeSource extends BiomeSource {
	private final LongCache<Holder<Biome>> cache = LossyCache.concurrent(2048, Holder[]::new);
	private final HolderSet<NoiseCave> caves;
	private final Supplier<ClimateSampler> climateSampler;
	private final NoiseTree.ParameterList<Holder<Climate>, ClimateSampler.ParameterPoint> params;
	
	public TFBiomeSource(HolderSet<NoiseCave> caves, Supplier<ClimateSampler> climateSampler, NoiseTree.ParameterList<Holder<Climate>, ClimateSampler.ParameterPoint> params) {
		this.caves = caves;
		this.climateSampler = climateSampler; 
		this.params = params;
	}
	
	public NoiseTree.ParameterList<Holder<Climate>, ClimateSampler.ParameterPoint> getParams() {
		return this.params;
	}
	
	public Holder<Climate> getClimate(ClimateSample sample) {
		return this.params.findValue(sample.temperature, sample.moisture, sample.continentNoise, sample.heightNoise, sample.riverNoise);
	}

	@Override
	protected Codec<TFBiomeSource> codec() {
		return TFCodecs.forError("TODO");
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.params.values().stream().map(ClimateSampler.ParameterPoint::climate).flatMap((climate) -> {
			return Stream.concat(climate.get().biomes().streamValues(), getBiomes(this.caves));
		});
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z, Sampler sampler) {
		return this.cache.computeIfAbsent(PosUtil.pack(x, z), (i) -> {
        	ClimateSample sample = this.climateSampler.get().sample(QuartPos.toBlock(x), QuartPos.toBlock(z));
        	return this.getClimate(sample).get().biomes().getValue(sample.biomeNoise);
        });
	}
	
	private static Stream<Holder<Biome>> getBiomes(HolderSet<NoiseCave> caves) {
		Stream<Holder<Biome>> stream = Stream.empty();
		for(Holder<NoiseCave> cave : caves) {
			Optional<? extends Holder<Climate>> climate = cave.get().climate();
			if(climate.isPresent()) {
				stream = Stream.concat(stream, climate.get().get().biomes().streamValues());
			}
		}
		return stream;
	}
	
}
