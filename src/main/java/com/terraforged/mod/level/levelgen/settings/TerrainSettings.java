/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.noise.Module;

public record TerrainSettings(General general, Terrain steppe, Terrain plains, Terrain hills, Terrain dales, Terrain plateau, Terrain badlands, Terrain torridonian, Terrain mountains) {
	public static final TerrainSettings DEFAULT = new TerrainSettings(
		General.DEFAULT,
		new Terrain(1.0F, 1.0F, 1.0F, 1.0F),
		new Terrain(2.0F, 1.0F, 1.0F, 1.0F),
		new Terrain(2.0F, 1.0F, 1.0F, 1.0F),
		new Terrain(1.5F, 1.0F, 1.0F, 1.0F),
		new Terrain(1.5F, 1.0F, 1.0F, 1.0F),
		new Terrain(1.0F, 1.0F, 1.0F, 1.0F),
		new Terrain(2.0F, 1.0F, 1.0F, 1.0F),
		new Terrain(2.5F, 1.0F, 1.0F, 1.0F)
	);
	
	public static final Codec<TerrainSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		General.CODEC.fieldOf("general").forGetter(TerrainSettings::general),
		Terrain.CODEC.fieldOf("steppe").forGetter(TerrainSettings::steppe),
		Terrain.CODEC.fieldOf("plains").forGetter(TerrainSettings::plains),
		Terrain.CODEC.fieldOf("hills").forGetter(TerrainSettings::hills),
		Terrain.CODEC.fieldOf("dales").forGetter(TerrainSettings::dales),
		Terrain.CODEC.fieldOf("plateau").forGetter(TerrainSettings::plateau),
		Terrain.CODEC.fieldOf("badlands").forGetter(TerrainSettings::badlands),
		Terrain.CODEC.fieldOf("torridonian").forGetter(TerrainSettings::torridonian),
		Terrain.CODEC.fieldOf("mountains").forGetter(TerrainSettings::mountains)
	).apply(instance, TerrainSettings::new));

	public record Terrain(float weight, float baseScale, float verticalScale, float horizontalScale) {
		public static final Terrain DEFAULT = new Terrain(1.0F, 1.0F, 1.0F, 1.0F);
		
    	public static final Codec<Terrain> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.floatRange(0.0F, 10.0F).fieldOf("weight").forGetter(Terrain::weight),
    		Codec.floatRange(0.0F, 2.0F).fieldOf("base_scale").forGetter(Terrain::baseScale),
    		Codec.floatRange(0.0F, 10.0F).fieldOf("vertical_scale").forGetter(Terrain::horizontalScale),
    		Codec.floatRange(0.0F, 10.0F).fieldOf("horizontal_scale").forGetter(Terrain::horizontalScale)
    	).apply(instance, Terrain::new));

        public Module apply(double bias, double scale, Module module) {
            double moduleBias = bias * (double)this.baseScale;
            double moduleScale = scale * (double)this.verticalScale;
            Module outputModule = module.scale(moduleScale).bias(moduleBias);
            return clamp(outputModule);
        }
        
        private static Module clamp(Module module) {
            if (module.minValue() < 0.0f || module.maxValue() > 1.0f) {
                return module.clamp(0.0, 1.0);
            }
            return module;
        }
    }

    public record General(int seedOffset, int regionSize, float verticalScale, float horizontalScale, boolean fancyMountains) {
    	public static final General DEFAULT = new General(0, 1200, 0.98F, 1.0F, true);
    	
    	public static final Codec<General> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.INT.fieldOf("seed_offset").forGetter(General::seedOffset),
    		Codec.intRange(125, 5000).fieldOf("region_size").forGetter(General::regionSize),
    		Codec.floatRange(0.01F, 1.0F).fieldOf("vertical_scale").forGetter(General::verticalScale),
    		Codec.floatRange(0.01F, 5.0F).fieldOf("horizontal_scale").forGetter(General::horizontalScale),
    		Codec.BOOL.fieldOf("fancy_mountains").forGetter(General::fancyMountains)
    	).apply(instance, General::new));
    }
}

