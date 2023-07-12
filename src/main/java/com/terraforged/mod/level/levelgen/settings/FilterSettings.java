/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FilterSettings(Erosion erosion, Smoothing smoothing) {
	public static final FilterSettings DEFAULT = new FilterSettings(Erosion.DEFAULT, Smoothing.DEFAULT);
	
	public static final Codec<FilterSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Erosion.CODEC.fieldOf("erosion").forGetter(FilterSettings::erosion),
		Smoothing.CODEC.fieldOf("smoothing").forGetter(FilterSettings::smoothing)
	).apply(instance, FilterSettings::new));
	
    public record Smoothing(int iterations, float smoothingRadius, float smoothingRate) {
    	public static final Smoothing DEFAULT = new Smoothing(1, 1.8F, 0.9F);
    	
    	public static final Codec<Smoothing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.intRange(0, 5).fieldOf("iterations").forGetter(Smoothing::iterations),
    		Codec.floatRange(0.0F, 5.0F).fieldOf("smoothingRadius").forGetter(Smoothing::smoothingRadius),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("smoothingRate").forGetter(Smoothing::smoothingRate)
    	).apply(instance, Smoothing::new));
    }

    public record Erosion(int dropletsPerChunk, int dropletLifetime, float dropletVolume, float dropletVelocity, float erosionRate, float depositeRate) {
    	public static final Erosion DEFAULT = new Erosion(135, 12, 0.7F, 0.7F, 0.5F, 0.5F);
    	
    	public static final Codec<Erosion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.intRange(10, 250).fieldOf("droplets_per_chunk").forGetter(Erosion::dropletsPerChunk),
    		Codec.intRange(1, 32).fieldOf("droplet_lifetime").forGetter(Erosion::dropletLifetime),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("droplet_volume").forGetter(Erosion::dropletVolume),
    		Codec.floatRange(0.1F, 1.0F).fieldOf("droplet_velocity").forGetter(Erosion::dropletVelocity),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("erosion_rate").forGetter(Erosion::erosionRate),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("deposite_rate").forGetter(Erosion::depositeRate)
    	).apply(instance, Erosion::new));
    }
}

