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

import java.util.List;
import java.util.Locale;

import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.asset.ClimateType;
import com.terraforged.mod.worldgen.biome.util.BiomeUtil;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public interface ModClimates {
    float RARE = 1F;
    float NORMAL = 5F;

    static void register(BootstapContext<ClimateType> ctx) {
        var lookup = ctx.lookup(Registries.BIOME);
        var biomes = BiomeUtil.getOverworldBiomes(lookup);
        for (var type : BiomeType.values()) {
        	ctx.register(resolve(type.name().toLowerCase(Locale.ROOT)), Factory.create(type, biomes));
        }
    }
    
    private static ResourceKey<ClimateType> resolve(String path) {
		return TerraForged.resolve(TerraForged.CLIMATES, path);
	}

    class Factory {
    	
        static ClimateType create(BiomeType type, List<Holder<Biome>> biomes) {
            var weights = new Object2FloatOpenHashMap<ResourceLocation>();

            for (var biome : biomes) {
                var biomeType = BiomeUtil.getType(biome);
                if (biomeType == null || biomeType != type) continue;

                var key = biome.unwrapKey().orElseThrow();

                weights.put(key.location(), getWeight(key, biome));
            }

            return new ClimateType(weights);
        }

        static float getWeight(ResourceKey<Biome> key, Holder<Biome> biome) {
//            if (Biome.getBiomeCategory(biome) == Biome.BiomeCategory.MUSHROOM) return RARE;
            if (key == Biomes.ICE_SPIKES) return RARE;
            return NORMAL;
        }
    }
}
