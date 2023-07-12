/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RiverSettings(int seedOffset, int riverCount, River mainRivers, River branchRivers, Lake lakes, Wetland wetlands) {
	public static final RiverSettings DEFAULT = new RiverSettings(
		0, 
		8,
		new River(5, 2, 6, 20, 8, 0.75F), 
		new River(4, 1, 4, 14, 5, 0.975F),
		Lake.DEFAULT,
		Wetland.DEFAULT
	);
	
	public static final Codec<RiverSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("seed_offset", 0).forGetter(RiverSettings::seedOffset),
		Codec.intRange(0, 30).optionalFieldOf("river_count", 8).forGetter(RiverSettings::riverCount),
		River.CODEC.optionalFieldOf("main_rivers", new River(5, 2, 6, 20, 8, 0.75f)).forGetter(RiverSettings::mainRivers),
		River.CODEC.optionalFieldOf("branch_rivers", new River(4, 1, 4, 14, 5, 0.975F)).forGetter(RiverSettings::branchRivers),
		Lake.CODEC.fieldOf("lakes").forGetter(RiverSettings::lakes),
		Wetland.CODEC.fieldOf("wetlands").forGetter(RiverSettings::wetlands)
	).apply(instance, RiverSettings::new));
	
    public record Wetland(float chance, int sizeMin, int sizeMax) {
    	public static final Wetland DEFAULT = new Wetland(0.6F, 175, 225);
    	
    	public static final Codec<Wetland> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(Wetland::chance),
    		Codec.intRange(50, 500).fieldOf("size_min").forGetter(Wetland::sizeMin),
    		Codec.intRange(50, 500).fieldOf("size_max").forGetter(Wetland::sizeMax)
    	).apply(instance, Wetland::new));
    }

    public record Lake(float chance, float minStartDistance, float maxStartDistance, int depth, int sizeMin, int sizeMax, int minBankHeight, int maxBankHeight) {
    	public static final Lake DEFAULT = new Lake(0.3F, 0.0F, 0.03F, 10, 75, 150, 2, 10);
    	
    	public static final Codec<Lake> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(Lake::chance),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("min_start_distance").forGetter(Lake::minStartDistance),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("max_start_distance").forGetter(Lake::maxStartDistance),
    		Codec.intRange(1, 20).fieldOf("depth").forGetter(Lake::depth),
    		Codec.intRange(10, 100).fieldOf("size_min").forGetter(Lake::sizeMin),
    		Codec.intRange(50, 500).fieldOf("size_max").forGetter(Lake::sizeMax),
    		Codec.intRange(1, 10).fieldOf("min_bank_height").forGetter(Lake::minBankHeight),
    		Codec.intRange(0, 10).fieldOf("max_bank_height").forGetter(Lake::maxBankHeight)
    	).apply(instance, Lake::new));
    	    }

    public record River(int bedDepth, int minBankHeight, int maxBankHeight, int bedWidth, int bankWidth, float fade) {
    	public static final Codec<River> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.intRange(1, 10).fieldOf("bed_depth").forGetter(River::bedDepth),
    		Codec.intRange(0, 10).fieldOf("min_bank_height").forGetter(River::minBankHeight),
    		Codec.intRange(1, 10).fieldOf("max_bank_height").forGetter(River::maxBankHeight),
    		Codec.intRange(1, 20).fieldOf("bed_width").forGetter(River::bedWidth),
    		Codec.intRange(1, 50).fieldOf("bank_width").forGetter(River::bankWidth),
    		Codec.floatRange(0.0F, 1.0F).fieldOf("fade").forGetter(River::fade)
    	).apply(instance, River::new));
    }
}

