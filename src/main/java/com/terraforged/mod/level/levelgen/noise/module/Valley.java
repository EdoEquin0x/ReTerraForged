/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.noise.module;

import java.util.Arrays;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.noise.util.Vec2f;

// im pretty sure this is supposed to be named Valley, but change it back to Ridge if not
public class Valley {
	public static final Codec<Valley> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("seed").forGetter((v) -> v.seed),
		Codec.INT.fieldOf("octaves").forGetter((v) -> v.octaves),
		Codec.FLOAT.fieldOf("strength").forGetter((v) -> v.strength),
		Codec.FLOAT.fieldOf("grid_size").forGetter((v) -> v.gridSize),
		Codec.FLOAT.fieldOf("amplitude").forGetter((v) -> v.amplitude),
		Codec.FLOAT.fieldOf("lacunarity").forGetter((v) -> v.lacunarity),
		Codec.FLOAT.fieldOf("falloff").forGetter((v) -> v.distanceFallOff),
		Mode.CODEC.fieldOf("blend_mode").forGetter((v) -> v.blendMode)
	).apply(instance, Valley::new));
	
    private final int seed;
    private final int octaves;
    private final float strength;
    private final float gridSize;
    private final float amplitude;
    private final float lacunarity;
    private final float distanceFallOff;
    private final Mode blendMode;

    public Valley(int seed, float strength, float gridSize, Mode blendMode) {
        this(seed, 1, strength, gridSize, blendMode);
    }

    public Valley(int seed, int octaves, float strength, float gridSize, Mode blendMode) {
        this(seed, octaves, strength, gridSize, 1.0f / (float)(octaves + 1), 2.25f, 0.75f, blendMode);
    }

    public Valley(int seed, int octaves, float strength, float gridSize, float amplitude, float lacunarity, float distanceFallOff, Mode blendMode) {
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
                        float height = Valley.getNoiseValue(dx1 + dx2, dy1 + dy2, candidateX, candidateY, source, cache);
                        if (!(height < lowestNeighbour)) continue;
                        lowestNeighbour = height;
                        bx = candidateX;
                        by = candidateY;
                    }
                }
                float height2 = Valley.sd(x, y, ax, ay, bx, by);
                if (!(height2 < minHeight2)) continue;
                minHeight2 = height2;
            }
        }
        return NoiseUtil.clamp(Valley.sqrt(minHeight2) / gridSize, 0.0f, 1.0f);
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
        return Valley.len2(padx, pady, badx * h, bady * h);
    }

    private static float len2(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    private static float sqrt(float value) {
        return (float)Math.sqrt(value);
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

    	public static final Codec<Mode> CODEC = TFCodecs.forEnum(Mode::valueOf);
 
        public abstract float blend(float var1, float var2, float var3);
    }

    public static class Noise implements Module {
    	public static final Codec<Noise> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Valley.CODEC.fieldOf("ridge").forGetter((n) -> n.ridge),
    		Module.CODEC.fieldOf("sourc").forGetter((n) -> n.source)
    	).apply(instance, Noise::new));
    	
        private final Valley ridge;
        private final Module source;
        private final ThreadLocal<float[]> cache = ThreadLocal.withInitial(() -> new float[25]);

        private Noise(Valley ridge, Module source) {
            this.ridge = ridge;
            this.source = source;
        }

        @Override
        public float getValue(float x, float y) {
            return this.ridge.getValue(x, y, this.source, this.cache.get());
        }

		@Override
		public Codec<Noise> codec() {
			return CODEC;
		}
    }
}
