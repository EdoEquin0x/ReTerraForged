/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WorldSettings(int continentScale, ControlPoints controlPoints) {
	public static final WorldSettings DEFAULT = new WorldSettings(1000, ControlPoints.DEFAULT);
	
	public static final Codec<WorldSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("continent_scale").forGetter(WorldSettings::continentScale),
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
}

