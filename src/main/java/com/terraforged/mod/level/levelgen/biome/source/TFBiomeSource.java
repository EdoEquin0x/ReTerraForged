package com.terraforged.mod.level.levelgen.biome.source;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.level.levelgen.climate.ClimateSampler;
import com.terraforged.mod.level.levelgen.noise.climate.ClimateSample;
import com.terraforged.mod.util.pos.PosUtil;
import com.terraforged.mod.util.storage.LongCache;
import com.terraforged.mod.util.storage.LossyCache;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class TFBiomeSource extends BiomeSource {
	private final ClimateSampler sampler;
	private final BiomeTree.ParameterList<Holder<Biome>> tree;
	@SuppressWarnings("unchecked")
	private final LongCache<Holder<Biome>> cache = LossyCache.concurrent(2048, i -> (Holder<Biome>[]) new Holder[i]);
	
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
        return this.cache.computeIfAbsent(PosUtil.pack(x, z), (i) -> this.compute(x, z));
	}
	
	private Holder<Biome> compute(int x, int z) {
		ClimateSample sample = this.sampler.getSample();
		this.sampler.sample(QuartPos.toBlock(x), QuartPos.toBlock(z), sample);
		return this.tree.findValue(sample);
	}
}
