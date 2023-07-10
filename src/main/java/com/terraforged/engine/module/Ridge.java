/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.module;

import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.value.DataObject;
import com.terraforged.cereal.value.DataValue;
import com.terraforged.noise.Module;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.noise.util.Vec2f;
import java.util.Arrays;

public class Ridge {
    private final int seed;
    private final int octaves;
    private final float strength;
    private final float gridSize;
    private final float amplitude;
    private final float lacunarity;
    private final float distanceFallOff;
    private final Mode blendMode;

    public Ridge(int seed, float strength, float gridSize, Mode blendMode) {
        this(seed, 1, strength, gridSize, blendMode);
    }

    public Ridge(int seed, int octaves, float strength, float gridSize, Mode blendMode) {
        this(seed, octaves, strength, gridSize, 1.0f / (float)(octaves + 1), 2.25f, 0.75f, blendMode);
    }

    public Ridge(int seed, int octaves, float strength, float gridSize, float amplitude, float lacunarity, float distanceFallOff, Mode blendMode) {
        this.seed = seed;
        this.octaves = octaves;
        this.strength = strength;
        this.gridSize = gridSize;
        this.amplitude = amplitude;
        this.lacunarity = lacunarity;
        this.distanceFallOff = distanceFallOff;
        this.blendMode = blendMode;
    }

    public Noise wrap(Module source) {
        return new Noise(this, source);
    }

    public float getValue(float x, float y, Module source) {
        return this.getValue(x, y, source, new float[25]);
    }

    public float getValue(float x, float y, Module source, float[] cache) {
        float value = source.getValue(x, y);
        float erosion = this.getErosionValue(x, y, source, cache);
        return NoiseUtil.lerp(erosion, value, this.blendMode.blend(value, erosion, this.strength));
    }

    public float getErosionValue(float x, float y, Module source, float[] cache) {
        float sum = 0.0f;
        float max = 0.0f;
        float gain = 1.0f;
        float distance = this.gridSize;
        for (int i = 0; i < this.octaves; ++i) {
            float value = this.getSingleErosionValue(x, y, distance, source, cache);
            sum += (value *= gain);
            max += gain;
            gain *= this.amplitude;
            distance *= this.distanceFallOff;
            x *= this.lacunarity;
            y *= this.lacunarity;
        }
        return sum / max;
    }

    public float getSingleErosionValue(float x, float y, float gridSize, Module source, float[] cache) {
        Arrays.fill(cache, -1.0f);
        int pix = NoiseUtil.floor(x / gridSize);
        int piy = NoiseUtil.floor(y / gridSize);
        float minHeight2 = Float.MAX_VALUE;
        for (int dy1 = -1; dy1 <= 1; ++dy1) {
            for (int dx1 = -1; dx1 <= 1; ++dx1) {
                int pax = pix + dx1;
                int pay = piy + dy1;
                Vec2f vec1 = NoiseUtil.cell(this.seed, pax, pay);
                float ax = ((float)pax + vec1.x) * gridSize;
                float ay = ((float)pay + vec1.y) * gridSize;
                float bx = ax;
                float by = ay;
                float lowestNeighbour = Float.MAX_VALUE;
                for (int dy2 = -1; dy2 <= 1; ++dy2) {
                    for (int dx2 = -1; dx2 <= 1; ++dx2) {
                        int pbx = pax + dx2;
                        int pby = pay + dy2;
                        Vec2f vec2 = pbx == pax && pby == pay ? vec1 : NoiseUtil.cell(this.seed, pbx, pby);
                        float candidateX = ((float)pbx + vec2.x) * gridSize;
                        float candidateY = ((float)pby + vec2.y) * gridSize;
                        float height = Ridge.getNoiseValue(dx1 + dx2, dy1 + dy2, candidateX, candidateY, source, cache);
                        if (!(height < lowestNeighbour)) continue;
                        lowestNeighbour = height;
                        bx = candidateX;
                        by = candidateY;
                    }
                }
                float height2 = Ridge.sd(x, y, ax, ay, bx, by);
                if (!(height2 < minHeight2)) continue;
                minHeight2 = height2;
            }
        }
        return NoiseUtil.clamp(Ridge.sqrt(minHeight2) / gridSize, 0.0f, 1.0f);
    }

    private static float getNoiseValue(int dx, int dy, float px, float py, Module module, float[] cache) {
        int index = (dy + 2) * 5 + (dx + 2);
        float value = cache[index];
        if (value == -1.0f) {
            cache[index] = value = module.getValue(px, py);
        }
        return value;
    }

    private static float sd(float px, float py, float ax, float ay, float bx, float by) {
        float padx = px - ax;
        float pady = py - ay;
        float badx = bx - ax;
        float bady = by - ay;
        float paba = padx * badx + pady * bady;
        float baba = badx * badx + bady * bady;
        float h = NoiseUtil.clamp(paba / baba, 0.0f, 1.0f);
        return Ridge.len2(padx, pady, badx * h, bady * h);
    }

    private static float len2(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    private static float sqrt(float value) {
        return (float)Math.sqrt(value);
    }

    public static DataSpec<?> spec() {
        return DataSpec.builder("Valley", Noise.class, Ridge::create).add("seed", (Object)1337, f -> ((Noise)f).ridge.seed).add("octaves", (Object)1, f -> ((Noise)f).ridge.octaves).add("strength", (Object)1, f -> Float.valueOf(((Noise)f).ridge.strength)).add("grid_size", (Object)100, f -> Float.valueOf(((Noise)f).ridge.gridSize)).add("amplitude", (Object)Float.valueOf(0.5f), f -> Float.valueOf(((Noise)f).ridge.amplitude)).add("lacunarity", (Object)Float.valueOf(2.25f), f -> Float.valueOf(((Noise)f).ridge.lacunarity)).add("fall_off", (Object)Float.valueOf(0.75f), f -> Float.valueOf(((Noise)f).ridge.distanceFallOff)).add("blend", (Object)Mode.CONSTANT, f -> ((Noise)f).ridge.blendMode).addObj("source", f -> ((Noise)f).source).build();
    }

    private static Noise create(DataObject data, DataSpec<Noise> spec, Context context) {
        Ridge ridge = new Ridge(spec.get("seed", data, DataValue::asInt), spec.get("octaves", data, DataValue::asInt), spec.get("strength", data, DataValue::asFloat).floatValue(), spec.get("grid_size", data, DataValue::asFloat).floatValue(), spec.get("amplitude", data, DataValue::asFloat).floatValue(), spec.get("lacunarity", data, DataValue::asFloat).floatValue(), spec.get("fall_off", data, DataValue::asFloat).floatValue(), spec.getEnum("blend", data, Mode.class));
        Module source = spec.get("source", data, Module.class);
        return ridge.wrap(source);
    }

    public static enum Mode {
        CONSTANT{

            @Override
            public float blend(float value, float erosion, float strength) {
                return 1.0f - strength;
            }
        }
        ,
        INPUT_LINEAR{

            @Override
            public float blend(float value, float erosion, float strength) {
                return 1.0f - strength * value;
            }
        }
        ,
        OUTPUT_LINEAR{

            @Override
            public float blend(float value, float erosion, float strength) {
                return 1.0f - strength * erosion;
            }
        };


        public abstract float blend(float var1, float var2, float var3);
    }

    public static class Noise  implements Module {
        private final Ridge ridge;
        private final Module source;
        private final ThreadLocal<float[]> cache = ThreadLocal.withInitial(() -> new float[25]);

        private Noise(Ridge ridge, Module source) {
            this.ridge = ridge;
            this.source = source;
        }

        @Override
        public String getSpecName() {
            return "Valley";
        }

        @Override
        public float getValue(float x, float y) {
            return this.ridge.getValue(x, y, this.source, this.cache.get());
        }
    }
}

