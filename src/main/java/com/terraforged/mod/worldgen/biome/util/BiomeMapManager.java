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

package com.terraforged.mod.worldgen.biome.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.util.storage.WeightMap;

import it.unimi.dsi.fastutil.objects.Object2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class BiomeMapManager {
//    private static final BiomeType[] TYPES = BiomeType.values();
//    private static final BiomeTypeHolder[] HOLDERS = Stream.of(TYPES).map(BiomeTypeHolder::new).toArray(BiomeTypeHolder[]::new);

    private final HolderLookup<Biome> biomes;
    private final List<Holder<Biome>> overworldBiomes;
    private final Map<BiomeType, WeightMap<Holder<Biome>>> biomeMap;

    public BiomeMapManager(HolderLookup<Biome> biomes) {
    	this.biomes = biomes;
    	this.overworldBiomes = BiomeUtil.getOverworldBiomes(biomes);
        this.biomeMap = this.buildBiomeMap(biomes);
    }
    
    public Holder<Biome> get(ResourceKey<Biome> key) {
    	return this.biomes.getOrThrow(key);
    }
    
    @SuppressWarnings("unchecked")
	public Optional<Holder<Biome>> getOptional(ResourceKey<Biome> key) {
    	return (Optional<Holder<Biome>>) (Object) this.biomes.get(key);
    }

    public List<Holder<Biome>> getOverworldBiomes() {
        return overworldBiomes;
    }

    public Map<BiomeType, WeightMap<Holder<Biome>>> getBiomeMap() {
        return biomeMap;
    }

    private Map<BiomeType, WeightMap<Holder<Biome>>> buildBiomeMap(HolderLookup<Biome> biomes) {
        var map = this.getWeightsMap(biomes);

        var result = new EnumMap<BiomeType, WeightMap<Holder<Biome>>>(BiomeType.class);
        for (var entry : map.entrySet()) {
            @SuppressWarnings("unchecked")
			var values = (Holder<Biome>[]) entry.getValue().keySet().toArray(Holder[]::new);
            var weights = entry.getValue().values().toFloatArray();
            result.put(entry.getKey(), WeightMap.of(values, weights));
        }

        return result;
    }

    private Map<BiomeType, Object2FloatMap<Holder<Biome>>> getWeightsMap(HolderLookup<Biome> biomes) {
        var map = new HashMap<BiomeType, Object2FloatMap<Holder<Biome>>>();
        var registered = new ObjectOpenHashSet<Holder<Biome>>();
        
//        for (var typeHolder : HOLDERS) {
//            var biomeType = climates.get(ResourceKey.create(TerraForged.CLIMATES, typeHolder.name)).orElseThrow().get();
//            if (biomeType == null) {
//                map.put(typeHolder.type(), newMutableWeightMap());
//            } else {
//                var typeMap = getBiomeWeights(biomeType, biomes, registered::add);
//                map.put(typeHolder.type(), typeMap);
//            }
//        }
//        
        for (var biome : overworldBiomes) {
            if (registered.contains(biome)) continue;

            var type = BiomeUtil.getType(biome);
            if (type == null) {
            	continue;
            }

            map.computeIfAbsent(type, t -> {
            	return new Object2FloatLinkedOpenHashMap<>(); 
            }).put(biome, 1F);
        }

        return map;
    }

    // ClimateType.getWeights() always returns empty, not really sure why
//    private static Object2FloatMap<Holder<Biome>> getBiomeWeights(ClimateType type, HolderLookup<Biome> biomes, Consumer<Holder<Biome>> registered) {
//        var map = newMutableWeightMap();
//
//        for (var entry : type.getWeights().object2FloatEntrySet()) {
//            var key = ResourceKey.create(Registries.BIOME, entry.getKey());
//            var biome = biomes.get(key).orElseThrow();
//            map.put(biome, entry.getFloatValue());
//            registered.accept(biome);
//        }
//
//        return map;
//    }

//    private static List<Holder<Biome>> getOverworldBiomes(HolderLookup<Biome> biomes, ClimateType[] climates) {
//        var list = BiomeUtil.getOverworldBiomes(biomes);
//        var added = new ObjectOpenHashSet<>(list);
//
//        for(ClimateType climate : climates) {
//        	for (var name : climate.getWeights().keySet()) {
//                var key = ResourceKey.create(Registries.BIOME, name);
//                var biome = biomes.get(key).orElseThrow();
//
//                if (added.add(biome)) {
//                    list.add(biome);
//                }
//            }
//        }
//        list.sort(BiomeUtil.BIOME_SORTER);
//        return list;
//    }

//    private static Object2FloatMap<Holder<Biome>> newMutableWeightMap() {
//        return new Object2FloatLinkedOpenHashMap<>();
//    }
//
//    private record BiomeTypeHolder(BiomeType type, ResourceLocation name) {
//    	
//        public BiomeTypeHolder(BiomeType type) {
//            this(type, TerraForged.location(type.name().toLowerCase(Locale.ROOT)));
//        }
//    }
}
