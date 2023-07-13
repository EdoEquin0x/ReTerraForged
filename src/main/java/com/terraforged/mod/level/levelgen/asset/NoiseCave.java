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

package com.terraforged.mod.level.levelgen.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;

public record NoiseCave(WeightMap<Holder<Biome>> biomes, Module elevation, Module shape, Module floor, Module modifier, int size, int minY, int maxY) {
    @SuppressWarnings("unchecked")
	public static final Codec<NoiseCave> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		WeightMap.codec(Biome.CODEC, (size) -> (Holder<Biome>[]) new Holder[size]).fieldOf("biomes").forGetter(NoiseCave::biomes),
    	Module.CODEC.fieldOf("elevation").forGetter(NoiseCave::elevation),
    	Module.CODEC.fieldOf("shape").forGetter(NoiseCave::shape),
    	Module.CODEC.fieldOf("floor").forGetter(NoiseCave::floor),
    	Module.CODEC.fieldOf("modifier").forGetter(NoiseCave::modifier),
    	Codec.INT.fieldOf("size").forGetter(NoiseCave::size),
    	Codec.INT.optionalFieldOf("min_y", -32).forGetter(NoiseCave::minY),
    	Codec.INT.fieldOf("max_y").forGetter(NoiseCave::maxY)
    ).apply(instance, NoiseCave::new));
    public static final Codec<Holder<NoiseCave>> CODEC = RegistryFileCodec.create(TerraForged.CAVE, DIRECT_CODEC);

    public int getHeight(int x, int z) {
        return getScaleValue(x, z, 1F, this.minY, this.maxY - this.minY, this.elevation);
    }

    public int getCavernSize(int x, int z, float modifier) {
        return getScaleValue(x, z, modifier, 0, this.size, this.shape);
    }

    public int getFloorDepth(int x, int z, int size) {
        return getScaleValue(x, z, 1F, 0, size, this.floor);
    }

    private static int getScaleValue(int x, int z, float modifier, int min, int range, Module noise) {
        if (range <= 0) return 0;

        return min + NoiseUtil.floor(noise.getValue(x, z) * range * modifier);
    }
}
