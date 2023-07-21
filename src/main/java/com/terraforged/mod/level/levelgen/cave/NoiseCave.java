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

package com.terraforged.mod.level.levelgen.cave;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.util.Noise;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.registry.data.TFCaves;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public record NoiseCave(BlockState filler, RuleTest fillTest, Optional<? extends Holder<Climate>> climate, Holder<Module> elevation, Holder<Module> shape, Holder<Module> floor, Holder<Module> modifier, int size, int minY, int maxY) {
	@SuppressWarnings("unchecked")
	public static final Codec<NoiseCave> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockState.CODEC.fieldOf("filler").forGetter(NoiseCave::filler),
		RuleTest.CODEC.optionalFieldOf("fill_test", AlwaysTrueTest.INSTANCE).forGetter(NoiseCave::fillTest),
		Climate.CODEC.optionalFieldOf("climate").forGetter((c) -> (Optional<Holder<Climate>>) c.climate()),
    	Module.CODEC.fieldOf("elevation").forGetter(NoiseCave::elevation),
    	Module.CODEC.fieldOf("shape").forGetter(NoiseCave::shape),
    	Module.CODEC.fieldOf("floor").forGetter(NoiseCave::floor),
    	Module.CODEC.fieldOf("modifier").forGetter(NoiseCave::modifier),
    	Codec.INT.fieldOf("size").forGetter(NoiseCave::size),
    	Codec.INT.optionalFieldOf("min_y", -32).forGetter(NoiseCave::minY),
    	Codec.INT.fieldOf("max_y").forGetter(NoiseCave::maxY)
    ).apply(instance, NoiseCave::new));
    public static final Codec<Holder<NoiseCave>> CODEC = RegistryFileCodec.create(TFCaves.REGISTRY, DIRECT_CODEC);
    public static final Codec<HolderSet<NoiseCave>> LIST_CODEC = RegistryCodecs.homogeneousList(TFCaves.REGISTRY, DIRECT_CODEC);

    public int getHeight(int x, int z) {
        return getScaleValue(x, z, 1F, this.minY, this.maxY - this.minY, this.elevation);
    }

    public int getCavernSize(int x, int z, float modifier) {
        return getScaleValue(x, z, modifier, 0, this.size, this.shape);
    }

    public int getFloorDepth(int x, int z, int size) {
        return getScaleValue(x, z, 1F, 0, size, this.floor);
    }

    private static final int BIOME_SEED_OFFSET = 124897;
    private static final float FREQUENCY = 1.0F / 800.0F;

    public Holder<Biome> getBiome(int seed, int x, int z) {
        float noise = sample(seed + BIOME_SEED_OFFSET, x, z, FREQUENCY);
        return this.climate.get().get().biomes().getValue(noise);
    }

    protected static float sample(int seed, int x, int z, float frequency) {
        float nx = x * frequency;
        float nz = z * frequency;
        float noise = (1 + Noise.singleSimplex(nx, nz, seed)) * 0.5F;
        return NoiseUtil.clamp(noise, 0F, 1F);
    }

    private static int getScaleValue(int x, int z, float modifier, int min, int range, Holder<Module> noise) {
        if (range <= 0) return 0;

        return min + NoiseUtil.floor(noise.get().getValue(x, z) * range * modifier);
    }
}
