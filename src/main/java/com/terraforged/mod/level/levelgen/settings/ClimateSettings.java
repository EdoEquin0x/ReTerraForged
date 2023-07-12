/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.util.NoiseUtil;

public record ClimateSettings(RangeValue temperature, RangeValue moisture, BiomeShape biomeShape, BiomeNoise biomeEdgeShape) {
	public static final ClimateSettings DEFAULT = new ClimateSettings(
		new RangeValue(7, 6, 2, 0.0f, 0.98f, 0.05f), 
		new RangeValue(7, 6, 1, 0.0f, 1.0f, 0.0f), 
		BiomeShape.DEFAULT, 
		BiomeNoise.DEFAULT
	);
	
	public static final Codec<ClimateSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		RangeValue.CODEC.optionalFieldOf("temperature", new RangeValue(0, 6, 2, 0.0F, 0.98F, 0.05F)).forGetter(ClimateSettings::temperature),
		RangeValue.CODEC.optionalFieldOf("moisture", new RangeValue(0, 6, 1, 0.0F, 1.0F, 0.0F)).forGetter(ClimateSettings::moisture),
		BiomeShape.CODEC.fieldOf("biome_shape").forGetter(ClimateSettings::biomeShape),
		BiomeNoise.CODEC.fieldOf("biome_edge_shape").forGetter(ClimateSettings::biomeEdgeShape)
	).apply(instance, ClimateSettings::new));
	
    public record BiomeNoise(Source type, int scale, int octaves, float gain, float lacunarity, int strength) {
    	public static final BiomeNoise DEFAULT = new BiomeNoise(Source.SIMPLEX, 24, 2, 0.5F, 2.65F, 14);
    	
    	public static final Codec<BiomeNoise> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Source.CODEC.fieldOf("type").forGetter(BiomeNoise::type),
    		Codec.intRange(1, 500).fieldOf("scale").forGetter(BiomeNoise::scale),
    		Codec.intRange(1, 5).fieldOf("octaves").forGetter(BiomeNoise::octaves),
    		Codec.floatRange(0.0F, 5.5F).fieldOf("gain").forGetter(BiomeNoise::gain),
    		Codec.floatRange(0.0F, 10.5F).fieldOf("lacunarity").forGetter(BiomeNoise::lacunarity),
    		Codec.intRange(1, 500).fieldOf("strength").forGetter(BiomeNoise::strength)
    	).apply(instance, BiomeNoise::new));
    	
        public Module build(int seed) {
            return Source.build(seed, this.scale, this.octaves).gain(this.gain).lacunarity(this.lacunarity).build(this.type).bias(-0.5);
        }
    }

    public record BiomeShape(int biomeSize, int macroNoiseSize, int biomeWarpScale, int biomeWarpStrength) {
    	public static final BiomeShape DEFAULT = new BiomeShape(225, 8, 150, 80);
    	
    	public static final Codec<BiomeShape> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.intRange(50, 2000).fieldOf("biome_size").forGetter(BiomeShape::biomeSize),
    		Codec.intRange(1, 20).fieldOf("macro_noise_size").forGetter(BiomeShape::macroNoiseSize),
    		Codec.intRange(1, 500).fieldOf("biome_warp_scale").forGetter(BiomeShape::biomeWarpScale),
    		Codec.intRange(1, 500).fieldOf("biome_warp_strength").forGetter(BiomeShape::biomeWarpStrength)
    	).apply(instance, BiomeShape::new));
    }
    
    public record RangeValue(int seedOffset, int scale, int falloff, float min, float max, float bias) {
    	public static final Codec<RangeValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.INT.fieldOf("seed_offset").forGetter(RangeValue::seedOffset),
    		Codec.intRange(1, 20).fieldOf("scale").forGetter(RangeValue::scale),
    		Codec.intRange(1, 10).fieldOf("falloff").forGetter(RangeValue::falloff),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("min").forGetter(RangeValue::min),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("max").forGetter(RangeValue::max),
    		Codec.floatRange(-1.0F, 1.0F).fieldOf("bias").forGetter(RangeValue::bias)
    	).apply(instance, RangeValue::new));

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

