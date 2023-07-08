/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.worldgen.biome;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.data.codec.ErrorCodec;
import com.terraforged.mod.util.storage.LongCache;
import com.terraforged.mod.util.storage.LossyCache;
import com.terraforged.mod.worldgen.asset.ClimateType;
import com.terraforged.mod.worldgen.cave.CaveType;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class Source extends BiomeSource {
    public static final Codec<Source> CODEC = new ErrorCodec<>(() -> "unsupported");
    public static final Climate.Sampler NOOP_CLIMATE_SAMPLER = Climate.empty();

    protected int seed;
    protected final Map<BiomeType, Holder<ClimateType>> climates;
    protected final BiomeSampler biomeSampler;
    protected final CaveBiomeSampler caveBiomeSampler;
    @SuppressWarnings("unchecked")
	protected final LongCache<Holder<Biome>> cache = LossyCache.concurrent(2048, i -> new Holder[i]);

    @SuppressWarnings("unchecked")
	public Source(
    	INoiseGenerator noise, 
    	Map<BiomeType, Holder<ClimateType>> climates,
    	Holder<Biome> cave,
    	Holder<Biome> plains,
    	Holder<Biome> deepColdOcean,
    	Holder<Biome> deepFrozenOcean,
    	Holder<Biome> deepLukewarmOcean,
    	Holder<Biome> deepOcean,
    	Holder<Biome> coldOcean,
    	Holder<Biome> frozenOcean,
    	Holder<Biome> warmOcean,
    	Holder<Biome> ocean,
    	Holder<Biome> snowyBeach,
    	Holder<Biome> stonyShore,
    	Holder<Biome> beach,
    	Holder<Biome> frozenRiver,
    	Holder<Biome> river
    ) {
    	this.climates = climates;
        this.biomeSampler = new BiomeSampler(noise, climates, plains, deepColdOcean, deepFrozenOcean, deepLukewarmOcean, deepOcean, coldOcean, frozenOcean, warmOcean, ocean, snowyBeach, stonyShore, beach, frozenRiver, river);
        this.caveBiomeSampler = new CaveBiomeSampler(800, new Holder[] { cave });
    }

    public void withSeed(long seed) {
        this.seed = (int) seed;
    }

//	  i think its safe to disable this, not sure though
    
//    /**
//     * Note: We provide the super-class an empty list to avoid the biome feature
//     * order dependency exceptions (wtf mojang). We do not use the featuresByStep
//     * list so order does not matter to us (thank god! Biome mods are going to
//     * get this very wrong).
//     * <p>
//     * We instead maintain our own set with the actual biomes and override here :)
//     */
//    @Override
//    public Set<Holder<Biome>> possibleBiomes() {
//        return possibleBiomes;
//    }

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return extractBiomes(this.climates.values()).stream();
	}

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return this.cache.computeIfAbsent(this.seed, PosUtil.pack(x, z), this::compute);
    }

    public BiomeSampler getBiomeSampler() {
        return this.biomeSampler;
    }

    public CaveBiomeSampler getCaveBiomeSampler() {
        return this.caveBiomeSampler;
    }

    public Holder<Biome> getUnderGroundBiome(int seed, int x, int z, CaveType type) {
        return this.caveBiomeSampler.getUnderGroundBiome(this.seed + seed, x, z, type);
    }

    protected Holder<Biome> compute(int seed, long index) {
        int x = PosUtil.unpackLeft(index) << 2;
        int z = PosUtil.unpackRight(index) << 2;
        return this.biomeSampler.sampleBiome(seed, x, z);
    }
    
    private static Set<Holder<Biome>> extractBiomes(Collection<Holder<ClimateType>> climates) {
    	Set<Holder<Biome>> biomes = new HashSet<>();
    	for(Holder<ClimateType> climate : climates) {
    		for(Holder<Biome> holder : climate.value().biomes()) {
    			biomes.add(holder);
    		}
    	}
    	return biomes;
    }
}
