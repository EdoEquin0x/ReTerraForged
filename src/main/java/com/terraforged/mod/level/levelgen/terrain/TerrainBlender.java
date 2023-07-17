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

package com.terraforged.mod.level.levelgen.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.domain.Domain;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.util.storage.Object2FloatCache;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.Holder;

public class TerrainBlender implements Module {
	@SuppressWarnings("unchecked")
	public static final Codec<TerrainBlender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("seed").forGetter((m) -> m.seed),
		Codec.INT.fieldOf("scale").forGetter((m) -> m.scale),
		Codec.FLOAT.fieldOf("jitter").forGetter((m) -> m.jitter),
		Codec.FLOAT.fieldOf("blending").forGetter((m) -> m.blending),
		WeightMap.codec(Module.CODEC, (size) -> (Holder<Module>[]) new Holder[size]).fieldOf("terrains").forGetter((m) -> m.terrains)
	).apply(instance, TerrainBlender::new));
	
    private static final int REGION_SEED_OFFSET = 21491124;
    private static final int WARP_SEED_OFFSET = 12678;

    private final int seed;
    private final int scale;
    private final float frequency;
    private final float jitter;
    private final float blending;

    private final Domain warp;
    private final WeightMap<Holder<Module>> terrains;
    private final ThreadLocal<Blender> localBlender = ThreadLocal.withInitial(Blender::new);

    public TerrainBlender(int seed, int scale, float jitter, float blending, WeightMap<Holder<Module>> terrains) {
    	this.seed = seed;
    	this.scale = scale;
        this.frequency = 1F / scale;
        this.jitter = jitter;
        this.blending = blending;
        this.terrains = terrains;
        this.warp = Domain.warp(Source.SIMPLEX, seed + WARP_SEED_OFFSET, scale, 3,scale / 2.5F);
    }

    @Override
    public float getValue(float x, float z) {
        var blender = this.localBlender.get();
        return this.getValue(x, z, blender);
    }
    
	@Override
	public Codec<TerrainBlender> codec() {
		return CODEC;
	}

    public float getValue(float x, float z, Blender blender) {
        float rx = this.warp.getX(x, z) * this.frequency;
        float rz = this.warp.getY(x, z) * this.frequency;    	
        getCell(this.seed + REGION_SEED_OFFSET, rx, rz, this.jitter, blender);
        return blender.getValue(x, z, this.blending, this.terrains);
    }

    public Blender getBlenderResource() {
        return this.localBlender.get();
    }

    private static void getCell(int seed, float x, float z, float jitter, Blender blender) {
        int maxX = NoiseUtil.floor(x) + 1;
        int maxZ = NoiseUtil.floor(z) + 1;

        blender.closestIndex = 0;
        blender.closestIndex2 = 0;

        int nearestIndex = -1;
        int nearestIndex2 = -1;

        float nearestDistance = Float.MAX_VALUE;
        float nearestDistance2 = Float.MAX_VALUE;

        for (int cz = maxZ - 2, i = 0; cz <= maxZ; cz++) {
            for (int cx = maxX - 2; cx <= maxX; cx++, i++) {
                int hash = NoiseUtil.hash2D(seed, cx, cz);

                float dx = MathUtil.rand(hash, NoiseUtil.X_PRIME);
                float dz = MathUtil.rand(hash, NoiseUtil.Y_PRIME);
                
                float px = cx + dx * jitter;
                float pz = cz + dz * jitter;
                float dist = NoiseUtil.dist2(x, z, px, pz);
                
                blender.hashes[i] = hash;
                blender.distances[i] = dist;

                if (dist < nearestDistance) {
                    nearestDistance2 = nearestDistance;
                    nearestDistance = dist;
                    nearestIndex2 = nearestIndex;
                    nearestIndex = i;
                } else if (dist < nearestDistance2) {
                    nearestDistance2 = dist;
                    nearestIndex2 =  i;
                }
            }
        }

        blender.closestIndex = nearestIndex;
        blender.closestIndex2 = nearestIndex2;
    }

    public static class Blender {
        protected int closestIndex;
        protected int closestIndex2;

        protected final int[] hashes = new int[9];
        protected final float[] distances = new float[9];
        protected final Object2FloatCache<Holder<Module>> cache = new Object2FloatCache<>(9);

        public float getCentreNoiseIndex() {
            return getNoiseIndex(this.closestIndex);
        }

        public float getDistance(int index) {
            return NoiseUtil.sqrt(this.distances[index]);
        }

        public float getCentreValue(float x, float z, WeightMap<Holder<Module>> terrains) {
            float noise = getCentreNoiseIndex();
            return terrains.getValue(noise).value().getValue(x, z);
        }

        public float getValue(float x, float z, float blending, WeightMap<Holder<Module>> terrains) {
            float dist0 = getDistance(closestIndex);
            float dist1 = getDistance(closestIndex2);

            float borderDistance = (dist0 + dist1) * 0.5F;
            float blendRadius = borderDistance * blending;
            float blendStart = borderDistance - blendRadius;

            if (dist0 <= blendStart) {
                return getCentreValue(x, z, terrains);
            } else {
                return getBlendedValue(x, z, dist0, dist1, blendRadius, terrains);
            }
        }

        public float getBlendedValue(float x, float z, float nearest, float nearest2, float blendRange, WeightMap<Holder<Module>> terrains) {
            cache.clear();

            float sumNoise = getCacheValue(closestIndex, x, z, terrains);
            float sumWeight = getWeight(nearest, nearest, blendRange);

            float nearestWeight2 = getWeight(nearest2, nearest, blendRange);
            if (nearestWeight2 > 0) {
                sumNoise += getCacheValue(closestIndex2, x, z, terrains) * nearestWeight2;
                sumWeight += nearestWeight2;
            }

            for (int i = 0; i < 9; i++) {
                if (i == closestIndex || i == closestIndex2) continue;

                float weight = getWeight(getDistance(i), nearest, blendRange);
                if (weight > 0) {
                    sumNoise += getCacheValue(i, x, z, terrains) * weight;
                    sumWeight += weight;
                }
            }

            return NoiseUtil.clamp(sumNoise / sumWeight, 0, 1);
        }

        private float getCacheValue(int index, float x, float z, WeightMap<Holder<Module>> terrains) {
            float noiseIndex = getNoiseIndex(index);
            var terrain = terrains.getValue(noiseIndex);

            float value = cache.get(terrain);
            if (Float.isNaN(value)) {
                value = terrain.value().getValue(x, z);
                cache.put(terrain, value);
            }

            return value;
        }
        
        private float getNoiseIndex(int index) {
            return MathUtil.rand(this.hashes[index]);
        }

        private static float getWeight(float dist, float origin, float blendRange) {
            float delta = dist - origin;
            if (delta <= 0) return 1F;
            if (delta >= blendRange) return 0F;

            float weight = 1 - (delta / blendRange);
            return weight * weight;
        }
    }
}
