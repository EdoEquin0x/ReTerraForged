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

package com.terraforged.mod.worldgen.biome;

import java.util.Arrays;
import java.util.Comparator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.worldgen.asset.ClimateType;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.noise.climate.ClimateSample;
import com.terraforged.mod.worldgen.noise.continent.ContinentPoints;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

//TODO move all these fields to a config or something
public class BiomeSampler extends IBiomeSampler.Sampler implements IBiomeSampler {
	private final BiomeSampler.Climate[] climates;
    private final Holder<Biome> fallback;
    protected final float beachSize = 0.005f;

    public BiomeSampler(
    	INoiseGenerator noiseGenerator, 
    	BiomeSampler.Climate[] climates,
    	Holder<Biome> fallback
    ) {
        super(noiseGenerator);
        this.climates = climates;
        this.fallback = fallback;
    }

    public Holder<Biome> sampleBiome(int seed, int x, int z) {
        var sample = this.getSample(seed, x, z);
    	//TODO don't stream here, too expensive
        Climate climate = Arrays.stream(this.climates).min(Comparator.comparing((cl) -> {
			return cl.fitness(sample.temperature, sample.moisture);
		})).orElse(null); //TODO bad orElse(null)
        if (climate == null || climate.type.get().isEmpty()) {
            return this.fallback;
        }
        var biome = this.getInitialBiome(climate.type.get(), sample.biomeNoise);
        return this.getBiomeOverride(biome, climate.type.get(), sample);
    }

    private Holder<Biome> getInitialBiome(ClimateType type, float noise) {
        return type.getValue(noise);
    }

    protected Holder<Biome> getBiomeOverride(Holder<Biome> input, ClimateType type, ClimateSample sample) {
        if (sample.continentNoise <= ContinentPoints.SHALLOW_OCEAN) {
//            return switch (climateType) {
//                case TAIGA, COLD_STEPPE -> this.deepOcean;
//                case TUNDRA -> this.deepFrozenOcean;
//                case DESERT, SAVANNA, TROPICAL_RAINFOREST -> this.deepLukewarmOcean;
//                default -> this.deepOcean;
//            };
        	return type.deepOcean();
        }

        if (sample.continentNoise <= ContinentPoints.BEACH) {
//            return switch (climateType) {
//                case TAIGA, COLD_STEPPE -> this.coldOcean;
//                case TUNDRA -> this.frozenOcean;
//                case DESERT, SAVANNA, TROPICAL_RAINFOREST -> this.warmOcean;
//                default -> this.ocean;
//            };
        	return type.ocean();
        }

        if (sample.continentNoise <= ContinentPoints.BEACH + this.beachSize) {
//            return switch (climateType) {
//                case TUNDRA -> this.snowyBeach;
//                case COLD_STEPPE -> this.stonyBeach;
//                default -> this.beach;
//            };
        	return type.beach();
        }

        if ((sample.terrainType.isRiver() || sample.terrainType.isLake()) && sample.riverNoise == 0) {
//            return climateType == BiomeType.TUNDRA ? this.frozenRiver : this.river;
            return type.river();
        }

        return input;
    }
    
    public record Climate(float temperature, float moisture, Holder<ClimateType> type) {
    	public static final Codec<BiomeSampler.Climate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.FLOAT.fieldOf("temperature").forGetter(Climate::temperature),
    		Codec.FLOAT.fieldOf("moisture").forGetter(Climate::moisture),
    		ClimateType.CODEC.fieldOf("biome").forGetter(Climate::type)
    	).apply(instance, Climate::new));
    	
    	public float fitness(float temperature, float moisture) {
    		return (this.temperature - temperature) * (this.temperature - temperature) + (this.moisture - moisture) * (this.moisture - moisture);
    	}
    }
}

