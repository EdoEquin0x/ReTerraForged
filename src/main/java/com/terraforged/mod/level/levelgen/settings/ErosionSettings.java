/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ErosionSettings(int dropletsPerChunk, int dropletLifetime, float dropletVolume, float dropletVelocity, float erosionRate, float depositeRate) {
	public static final ErosionSettings DEFAULT = new ErosionSettings(135, 12, 0.7F, 0.7F, 0.5F, 0.5F);
	
	public static final Codec<ErosionSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.intRange(10, 250).fieldOf("droplets_per_chunk").forGetter(ErosionSettings::dropletsPerChunk),
    	Codec.intRange(1, 32).fieldOf("droplet_lifetime").forGetter(ErosionSettings::dropletLifetime),
    	Codec.floatRange(0.0F, 1.0F).fieldOf("droplet_volume").forGetter(ErosionSettings::dropletVolume),
    	Codec.floatRange(0.1F, 1.0F).fieldOf("droplet_velocity").forGetter(ErosionSettings::dropletVelocity),
    	Codec.floatRange(0.0F, 1.0F).fieldOf("erosion_rate").forGetter(ErosionSettings::erosionRate),
    	Codec.floatRange(0.0F, 1.0F).fieldOf("deposite_rate").forGetter(ErosionSettings::depositeRate)
    ).apply(instance, ErosionSettings::new));
}

