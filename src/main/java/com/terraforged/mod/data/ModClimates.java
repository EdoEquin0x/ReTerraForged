/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.data;

import java.util.Locale;

import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.storage.WeightMap;
import com.terraforged.mod.worldgen.asset.ClimateType;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;

public interface ModClimates {
	ResourceKey<ClimateType> TROPICAL_RAINFOREST = resolve(BiomeType.TROPICAL_RAINFOREST);
	ResourceKey<ClimateType> SAVANNA = resolve(BiomeType.SAVANNA);
	ResourceKey<ClimateType> DESERT = resolve(BiomeType.DESERT);
	ResourceKey<ClimateType> TEMPERATE_RAINFOREST = resolve(BiomeType.TEMPERATE_RAINFOREST);
	ResourceKey<ClimateType> TEMPERATE_FOREST = resolve(BiomeType.TEMPERATE_FOREST);
	ResourceKey<ClimateType> GRASSLAND = resolve(BiomeType.GRASSLAND);
	ResourceKey<ClimateType> COLD_STEPPE = resolve(BiomeType.COLD_STEPPE);
	ResourceKey<ClimateType> STEPPE = resolve(BiomeType.STEPPE);
	ResourceKey<ClimateType> TAIGA = resolve(BiomeType.TAIGA);
	ResourceKey<ClimateType> TUNDRA = resolve(BiomeType.TUNDRA);
	ResourceKey<ClimateType> ALPINE = resolve(BiomeType.ALPINE);
	
    float RARE = 1F;
    float NORMAL = 5F;

    static void register(BootstapContext<ClimateType> ctx) {
        var lookup = ctx.lookup(Registries.BIOME);
        
        ctx.register(TROPICAL_RAINFOREST, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.JUNGLE))
        			.build()
        	)
        );
        
        ctx.register(SAVANNA,
           	new ClimateType(
           		new WeightMap.Builder<>()
           			.entry(NORMAL, lookup.getOrThrow(Biomes.SAVANNA))
           			.build()
            )
        );
        
        ctx.register(DESERT, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.DESERT))
        			.build()
            )
        );
        
        ctx.register(TEMPERATE_RAINFOREST, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.JUNGLE))
        			.build()
            )
        );
        
        ctx.register(TEMPERATE_FOREST, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.FOREST))
        			.entry(RARE, lookup.getOrThrow(Biomes.BIRCH_FOREST))
        			.build()
            )
        );
        
        ctx.register(GRASSLAND, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.PLAINS))
        			.build()
            )
        );
        
        ctx.register(COLD_STEPPE, 
            new ClimateType(
            	new WeightMap.Builder<>()
            		.entry(NORMAL, lookup.getOrThrow(Biomes.SAVANNA_PLATEAU))
            		.build()
            )
        );
        
        ctx.register(STEPPE, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.WINDSWEPT_SAVANNA))
        			.build()
            )
        );
        
        ctx.register(TAIGA, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.TAIGA))
        			.build()
            )
        );
        
        ctx.register(TUNDRA,
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.SNOWY_TAIGA))
        			.build()
            )
        );
        
        ctx.register(ALPINE,
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.TAIGA))
        			.build()
        	)
        );
    }
    
    private static ResourceKey<ClimateType> resolve(BiomeType biomeType) {
		return TerraForged.resolve(TerraForged.CLIMATES, biomeType.name().toLowerCase(Locale.ROOT));
	}
}
