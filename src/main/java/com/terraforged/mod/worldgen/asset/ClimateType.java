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

package com.terraforged.mod.worldgen.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;

public record ClimateType(WeightMap<Holder<Biome>> biomes, Holder<Biome> beach, Holder<Biome> ocean, Holder<Biome> deepOcean, Holder<Biome> river) {
    public static final Codec<ClimateType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
    	WeightMap.codec(Biome.CODEC, Holder[]::new).fieldOf("biomes").forGetter(ClimateType::biomes),
    	Biome.CODEC.fieldOf("beach").forGetter(ClimateType::beach),
    	Biome.CODEC.fieldOf("ocean").forGetter(ClimateType::ocean),
    	Biome.CODEC.fieldOf("deep_ocean").forGetter(ClimateType::deepOcean),
    	Biome.CODEC.fieldOf("river").forGetter(ClimateType::river)
    ).apply(instance, ClimateType::new));
    public static final Codec<Holder<ClimateType>> CODEC = RegistryFileCodec.create(TerraForged.CLIMATES, DIRECT_CODEC);
    
    public boolean isEmpty() {
    	return this.biomes.isEmpty();
    }
    
    public Holder<Biome> getValue(float noise) {
    	return this.biomes.getValue(noise);
    }
}
