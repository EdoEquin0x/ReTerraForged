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

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.mod.codec.FailCodec;
import com.terraforged.mod.util.storage.LongCache;
import com.terraforged.mod.util.storage.LossyCache;
import com.terraforged.mod.worldgen.cave.CaveType;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class TFBiomeSource extends BiomeSource {
    public static final Codec<TFBiomeSource> CODEC = FailCodec.fail("TODO");
    
    protected int seed;
    @Deprecated(forRemoval = true)
    protected final BiomeSampler biomeSampler;
    @Deprecated(forRemoval = true)
    protected final CaveBiomeSampler caveBiomeSampler;
    @SuppressWarnings("unchecked")
	protected final LongCache<Holder<Biome>> cache = LossyCache.concurrent(2048, i -> new Holder[i]);

    @SuppressWarnings("unchecked")
	public TFBiomeSource(INoiseGenerator noise, @Deprecated(forRemoval = true) Holder<Biome> cave, BiomeSource delegate) {
        this.biomeSampler = new BiomeSampler(noise, delegate);
        this.caveBiomeSampler = new CaveBiomeSampler(800, new Holder[] { cave });
    }
    
    public BiomeSource getDelegate() {
    	return this.biomeSampler.getDelegate();
    }
    
    @Deprecated(forRemoval = true)
    public void withSeed(long seed) {
        this.seed = (int) seed;
    }

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.biomeSampler.getDelegate().possibleBiomes().stream();
	}

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return this.cache.computeIfAbsent(this.seed, PosUtil.pack(x, z), (seed, key) -> this.biomeSampler.sampleBiome(seed, x, y, z, sampler));
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
}
