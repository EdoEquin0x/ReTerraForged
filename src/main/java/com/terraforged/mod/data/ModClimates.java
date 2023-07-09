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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.storage.WeightMap;
import com.terraforged.mod.worldgen.asset.ClimateType;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;

public interface ModClimates {
	ResourceKey<ClimateType> TROPICAL_RAINFOREST = resolve("tropical_rainforest");
	ResourceKey<ClimateType> SAVANNA = resolve("savanna");
	ResourceKey<ClimateType> DESERT = resolve("desert");
	ResourceKey<ClimateType> TEMPERATE_RAINFOREST = resolve("temperate_rainforest");
	ResourceKey<ClimateType> TEMPERATE_FOREST = resolve("temperate_forest");
	ResourceKey<ClimateType> GRASSLAND = resolve("grassland");
	ResourceKey<ClimateType> COLD_STEPPE = resolve("cold_steppe");
	ResourceKey<ClimateType> STEPPE = resolve("steppe");
	ResourceKey<ClimateType> TAIGA = resolve("taiga");
	ResourceKey<ClimateType> TUNDRA = resolve("tundra");
	ResourceKey<ClimateType> ALPINE = resolve("alpine");
	
    float RARE = 1F;
    float NORMAL = 5F;

    static void register(BootstapContext<ClimateType> ctx) {
        var lookup = ctx.lookup(Registries.BIOME);
        
        ctx.register(TROPICAL_RAINFOREST, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.JUNGLE))
        			.build(),
        		lookup.getOrThrow(Biomes.BEACH),
        		lookup.getOrThrow(Biomes.WARM_OCEAN),
        		lookup.getOrThrow(Biomes.DEEP_LUKEWARM_OCEAN),
        		lookup.getOrThrow(Biomes.RIVER)
        	)
        );
        
        ctx.register(SAVANNA,
           	new ClimateType(
           		new WeightMap.Builder<>()
           			.entry(NORMAL, lookup.getOrThrow(Biomes.SAVANNA))
           			.build(),
           		lookup.getOrThrow(Biomes.BEACH),
           		lookup.getOrThrow(Biomes.WARM_OCEAN),
           		lookup.getOrThrow(Biomes.DEEP_LUKEWARM_OCEAN),
           		lookup.getOrThrow(Biomes.RIVER)
            )
        );
        
        ctx.register(DESERT, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.DESERT))
        			.build(),
        		lookup.getOrThrow(Biomes.BEACH),
        		lookup.getOrThrow(Biomes.WARM_OCEAN),
        		lookup.getOrThrow(Biomes.DEEP_LUKEWARM_OCEAN),
        		lookup.getOrThrow(Biomes.RIVER)
            )
        );
        
        ctx.register(TEMPERATE_RAINFOREST, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.JUNGLE))
        			.build(),
        		lookup.getOrThrow(Biomes.BEACH),
            	lookup.getOrThrow(Biomes.OCEAN),
            	lookup.getOrThrow(Biomes.DEEP_OCEAN),
            	lookup.getOrThrow(Biomes.RIVER)
            )
        );
        
        ctx.register(TEMPERATE_FOREST, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.FOREST))
        			.entry(RARE, lookup.getOrThrow(Biomes.BIRCH_FOREST))
        			.build(),
        		lookup.getOrThrow(Biomes.BEACH),
        		lookup.getOrThrow(Biomes.OCEAN),
        		lookup.getOrThrow(Biomes.DEEP_OCEAN),
        		lookup.getOrThrow(Biomes.RIVER)
            )
        );
        
        ctx.register(GRASSLAND, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.PLAINS))
        			.build(),
        		lookup.getOrThrow(Biomes.BEACH),
        		lookup.getOrThrow(Biomes.OCEAN),
        		lookup.getOrThrow(Biomes.DEEP_OCEAN),
        		lookup.getOrThrow(Biomes.RIVER)
            )
        );
        
        ctx.register(COLD_STEPPE, 
            new ClimateType(
            	new WeightMap.Builder<>()
            		.entry(NORMAL, lookup.getOrThrow(Biomes.SAVANNA_PLATEAU))
            		.build(),
            	lookup.getOrThrow(Biomes.STONY_SHORE),
            	lookup.getOrThrow(Biomes.OCEAN),
            	lookup.getOrThrow(Biomes.DEEP_OCEAN),
            	lookup.getOrThrow(Biomes.RIVER)
            )
        );
        
        ctx.register(STEPPE, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.WINDSWEPT_SAVANNA))
        			.build(),
        		lookup.getOrThrow(Biomes.BEACH),
        		lookup.getOrThrow(Biomes.OCEAN),
        		lookup.getOrThrow(Biomes.DEEP_OCEAN),
        		lookup.getOrThrow(Biomes.RIVER)
            )
        );
        
        ctx.register(TAIGA, 
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.TAIGA))
        			.build(),
        		lookup.getOrThrow(Biomes.BEACH),
        		lookup.getOrThrow(Biomes.OCEAN),
        		lookup.getOrThrow(Biomes.DEEP_OCEAN),
        		lookup.getOrThrow(Biomes.RIVER)
            )
        );
        
        ctx.register(TUNDRA,
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.SNOWY_TAIGA))
        			.build(),
            	lookup.getOrThrow(Biomes.BEACH),
            	lookup.getOrThrow(Biomes.OCEAN),
            	lookup.getOrThrow(Biomes.DEEP_OCEAN),
            	lookup.getOrThrow(Biomes.FROZEN_RIVER)
            )
        );
        
        ctx.register(ALPINE,
        	new ClimateType(
        		new WeightMap.Builder<>()
        			.entry(NORMAL, lookup.getOrThrow(Biomes.TAIGA))
        			.build(),
        		lookup.getOrThrow(Biomes.BEACH),
        		lookup.getOrThrow(Biomes.OCEAN),
        		lookup.getOrThrow(Biomes.DEEP_OCEAN),
        		lookup.getOrThrow(Biomes.RIVER)
        	)
        );
    }
    
    private static ResourceKey<ClimateType> resolve(String name) {
		return TerraForged.resolve(TerraForged.CLIMATES, name.toLowerCase(Locale.ROOT));
	}
}
