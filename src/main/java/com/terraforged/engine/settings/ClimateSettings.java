/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.settings;

import com.terraforged.engine.serialization.annotation.Comment;
import com.terraforged.engine.serialization.annotation.Rand;
import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Serializable;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;

@Serializable
public class ClimateSettings {
    public RangeValue temperature = new RangeValue(6, 2, 0.0f, 0.98f, 0.05f);
    public RangeValue moisture = new RangeValue(6, 1, 0.0f, 1.0f, 0.0f);
    public BiomeShape biomeShape = new BiomeShape();
    public BiomeNoise biomeEdgeShape = new BiomeNoise();

    @Serializable
    public static class BiomeNoise {
        @Comment(value={"The noise type"})
        public Source type = Source.SIMPLEX;
        @Range(min=1.0f, max=500.0f)
        @Comment(value={"Controls the scale of the noise"})
        public int scale = 24;
        @Range(min=1.0f, max=5.0f)
        @Comment(value={"Controls the number of noise octaves"})
        public int octaves = 2;
        @Range(min=0.0f, max=5.5f)
        @Comment(value={"Controls the gain subsequent noise octaves"})
        public float gain = 0.5f;
        @Range(min=0.0f, max=10.5f)
        @Comment(value={"Controls the lacunarity of subsequent noise octaves"})
        public float lacunarity = 2.65f;
        @Range(min=1.0f, max=500.0f)
        @Comment(value={"Controls the strength of the noise"})
        public int strength = 14;

        public Module build(int seed) {
            return Source.build(seed, this.scale, this.octaves).gain(this.gain).lacunarity(this.lacunarity).build(this.type).bias(-0.5);
        }
    }

    @Serializable
    public static class BiomeShape {
        public static final int DEFAULT_BIOME_SIZE = 225;
        @Range(min=50.0f, max=2000.0f)
        @Comment(value={"Controls the size of individual biomes"})
        public int biomeSize = 225;
        @Range(min=1.0f, max=20.0f)
        @Comment(value={"Macro noise is used to group large areas of biomes into a single type (such as deserts)"})
        public int macroNoiseSize = 8;
        @Range(min=1.0f, max=500.0f)
        @Comment(value={"Controls the scale of shape distortion for biomes"})
        public int biomeWarpScale = 150;
        @Range(min=1.0f, max=500.0f)
        @Comment(value={"Controls the strength of shape distortion for biomes"})
        public int biomeWarpStrength = 80;
    }

    @Serializable
    public static class RangeValue {
        @Rand
        @Comment(value={"A seed offset used to randomise climate distribution"})
        public int seedOffset = 0;
        @Range(min=1.0f, max=20.0f)
        @Comment(value={"The horizontal scale"})
        public int scale = 7;
        @Range(min=1.0f, max=10.0f)
        @Comment(value={"How quickly values transition from an extremity"})
        public int falloff = 2;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"The lower limit of the range"})
        public float min;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"The upper limit of the range"})
        public float max;
        @Range(min=-1.0f, max=1.0f)
        @Comment(value={"The bias towards either end of the range"})
        public float bias = -0.1f;

        public RangeValue() {
            this(1, 0.0f, 1.0f, 0.0f);
        }

        public RangeValue(int falloff, float min, float max, float bias) {
            this(7, falloff, min, max, bias);
        }

        public RangeValue(int scale, int falloff, float min, float max, float bias) {
            this.min = min;
            this.max = max;
            this.bias = bias;
            this.scale = scale;
            this.falloff = falloff;
        }

        public float getMin() {
            return NoiseUtil.clamp(Math.min(this.min, this.max), 0.0f, 1.0f);
        }

        public float getMax() {
            return NoiseUtil.clamp(Math.max(this.min, this.max), this.getMin(), 1.0f);
        }

        public float getBias() {
            return NoiseUtil.clamp(this.bias, -1.0f, 1.0f);
        }

        public Module apply(Module module) {
            float min = this.getMin();
            float max = this.getMax();
            float bias = this.getBias() / 2.0f;
            return module.bias(bias).clamp(min, max);
        }
    }
}

