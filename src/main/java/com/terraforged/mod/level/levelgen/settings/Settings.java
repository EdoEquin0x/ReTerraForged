/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Settings(WorldSettings world, ClimateSettings climate, ErosionSettings erosion) {
	public static final Settings DEFAULT = new Settings(WorldSettings.DEFAULT, ClimateSettings.DEFAULT, ErosionSettings.DEFAULT);
	
	public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		WorldSettings.CODEC.fieldOf("world").forGetter(Settings::world),
		ClimateSettings.CODEC.fieldOf("climate").forGetter(Settings::climate),
		ErosionSettings.CODEC.fieldOf("erosion").forGetter(Settings::erosion)
	).apply(instance, Settings::new));
}

