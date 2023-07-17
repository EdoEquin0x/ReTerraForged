/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.continent.ContinentType;
import com.terraforged.mod.noise.func.DistanceFunc;

public record WorldSettings(Continent continent, ControlPoints controlPoints) {
	public static final WorldSettings DEFAULT = new WorldSettings(Continent.DEFAULT, ControlPoints.DEFAULT);
	
	public static final Codec<WorldSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Continent.CODEC.fieldOf("continent").forGetter(WorldSettings::continent),
		ControlPoints.CODEC.fieldOf("control_points").forGetter(WorldSettings::controlPoints)
	).apply(instance, WorldSettings::new));
	
    public record ControlPoints(float deepOcean, float shallowOcean, float beach, float coast, float inland) {
    	public static final ControlPoints DEFAULT = new ControlPoints(0.1F, 0.25F, 0.327F, 0.448F, 0.502F);
    	
    	public static final Codec<ControlPoints> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.floatRange(0.0F, 1.0F).fieldOf("deep_ocean").forGetter(ControlPoints::deepOcean),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("shallow_ocean").forGetter(ControlPoints::shallowOcean),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("beach").forGetter(ControlPoints::beach),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("coast").forGetter(ControlPoints::coast),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("inland").forGetter(ControlPoints::inland)
    	).apply(instance, ControlPoints::new));
    }

    public record Continent(ContinentType type, DistanceFunc shape, int scale, float jitter, float skipping, float variance, int octaves, float gain, float lacunarity) {
    	public static final Continent DEFAULT = new Continent(ContinentType.MULTI_IMPROVED, DistanceFunc.EUCLIDEAN, 1000, 0.7F, 0.25F, 0.25F, 5, 0.26F, 4.33F);
    	
    	public static final Codec<Continent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		ContinentType.CODEC.fieldOf("type").forGetter(Continent::type),
    		DistanceFunc.CODEC.fieldOf("shape").forGetter(Continent::shape),
    		Codec.intRange(100, 100000).fieldOf("scale").forGetter(Continent::scale),
    		Codec.floatRange(0.5F, 1.0F).fieldOf("jitter").forGetter(Continent::jitter),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("skipping").forGetter(Continent::skipping),
    		Codec.floatRange(0.0F, 0.75F).fieldOf("variance").forGetter(Continent::variance),
    		Codec.intRange(1, 5).fieldOf("octave").forGetter(Continent::octaves),
    		Codec.floatRange(0.0F, 0.5F).fieldOf("gain").forGetter(Continent::gain),
    		Codec.floatRange(1.0F, 10.0F).fieldOf("lacunarity").forGetter(Continent::lacunarity)
    	).apply(instance, Continent::new));
    }
}

