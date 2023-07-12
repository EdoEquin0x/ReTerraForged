/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Settings(WorldSettings world, ClimateSettings climate, TerrainSettings terrain, RiverSettings rivers, FilterSettings filters) {
	public static final Settings DEFAULT = new Settings(WorldSettings.DEFAULT, ClimateSettings.DEFAULT, TerrainSettings.DEFAULT, RiverSettings.DEFAULT, FilterSettings.DEFAULT);
	
	public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		WorldSettings.CODEC.fieldOf("world").forGetter(Settings::world),
		ClimateSettings.CODEC.fieldOf("climate").forGetter(Settings::climate),
		TerrainSettings.CODEC.fieldOf("terrain").forGetter(Settings::terrain),
		RiverSettings.CODEC.fieldOf("river").forGetter(Settings::rivers),
		FilterSettings.CODEC.fieldOf("filters").forGetter(Settings::filters)
	).apply(instance, Settings::new));
}

