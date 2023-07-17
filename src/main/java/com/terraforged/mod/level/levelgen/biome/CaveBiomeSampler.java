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

package com.terraforged.mod.level.levelgen.biome;

import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.noise.util.Noise;
import com.terraforged.mod.noise.util.NoiseUtil;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public class CaveBiomeSampler {
    public static final int OFFSET = 124897;
    
    private final int scale;
    private final float frequency;

	public CaveBiomeSampler(int scale) {
		this.scale = scale;
        this.frequency = 1F / scale;
    }

    public CaveBiomeSampler(CaveBiomeSampler other) {
    	this.scale = other.scale;
        this.frequency = other.frequency;
    }
    
    public Holder<Biome> getUnderGroundBiome(int seed, int x, int z, NoiseCave cave) {
        float noise = sample(seed + OFFSET, x, z, this.frequency);
        return cave.biomes().getValue(noise);
    }

    protected static float sample(int seed, int x, int z, float frequency) {
        float nx = x * frequency;
        float nz = z * frequency;
        float noise = (1 + Noise.singleSimplex(nx, nz, seed)) * 0.5F;
        return NoiseUtil.clamp(noise, 0F, 1F);
    }
}
