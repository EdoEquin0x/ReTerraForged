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

import java.util.Comparator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.terrain.Terrain;
import com.terraforged.mod.noise.Module;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

public class TerrainNoise {
    public static final Comparator<TerrainNoise> COMPARATOR = Comparator.comparing(t -> t.terrain().getName());

    public static final Codec<TerrainNoise> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
    	TerrainType.CODEC.fieldOf("type").forGetter(TerrainNoise::type),
    	Module.CODEC.fieldOf("noise").forGetter(TerrainNoise::noise)
    ).apply(instance, TerrainNoise::new));
    public static final Codec<Holder<TerrainNoise>> CODEC = RegistryFileCodec.create(TerraForged.TERRAIN, DIRECT_CODEC);

    private static final double MIN_NOISE = 5.0 / 255.0;

    private final Holder<TerrainType> type;
    private final Module noise;

    public TerrainNoise(Holder<TerrainType> type, Module noise) {
        this.type = type;
        this.noise = noise.minValue() < MIN_NOISE ? noise.bias(MIN_NOISE).clamp(0, 1) : noise;
    }

    public Holder<TerrainType> type() {
        return this.type;
    }

    public Terrain terrain() {
        return this.type.value().terrain();
    }

    public Module noise() {
        return this.noise;
    }

    @Override
    public String toString() {
        return "TerrainConfig{" +
                "type=" + this.type +
                ", noise=" + this.noise +
                '}';
    }
}
