package com.terraforged.mod.level.levelgen.biome.source;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.level.levelgen.climate.ClimateSampler;
import com.terraforged.mod.level.levelgen.noise.climate.ClimateSample;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class TFBiomeSource extends BiomeSource {
	private final ClimateSampler sampler;
	private final BiomeTree.ParameterList<Holder<Biome>> tree;
	
	public TFBiomeSource(ClimateSampler sampler, BiomeTree.ParameterList<Holder<Biome>> tree) {
		this.sampler = sampler;
		this.tree = tree;
	}
	
	public BiomeTree.ParameterList<Holder<Biome>> getTree() {
		return this.tree;
	}
	
	@Override
	protected Codec<TFBiomeSource> codec() {
		return TFCodecs.error("TODO");
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.tree.values().stream().map(Pair::getSecond);
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
		ClimateSample sample = this.sampler.getSample();
		this.sampler.sample(x, z, sample);
		return this.tree.findValue(sample);
	}
}
