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

import java.util.Map;

import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.worldgen.asset.ClimateType;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.noise.climate.ClimateSample;
import com.terraforged.mod.worldgen.noise.continent.ContinentPoints;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

//TODO move all these fields to a config or something
public class BiomeSampler extends IBiomeSampler.Sampler implements IBiomeSampler {
    protected final Map<BiomeType, Holder<ClimateType>> climates;
    private final Holder<Biome> plains;
    private final Holder<Biome> deepColdOcean;
    private final Holder<Biome> deepFrozenOcean;
    private final Holder<Biome> deepLukewarmOcean;
    private final Holder<Biome> deepOcean;
    private final Holder<Biome> coldOcean;
    private final Holder<Biome> frozenOcean;
    private final Holder<Biome> warmOcean;
    private final Holder<Biome> ocean;
    private final Holder<Biome> snowyBeach;
    private final Holder<Biome> stonyBeach;
    private final Holder<Biome> beach;
    private final Holder<Biome> frozenRiver;
    private final Holder<Biome> river;
    protected final float beachSize = 0.005f;

    public BiomeSampler(
    	INoiseGenerator noiseGenerator, 
    	Map<BiomeType, Holder<ClimateType>> climates,
    	Holder<Biome> plains,
    	Holder<Biome> deepColdOcean,
    	Holder<Biome> deepFrozenOcean,
    	Holder<Biome> deepLukewarmOcean,
    	Holder<Biome> deepOcean,
    	Holder<Biome> coldOcean,
    	Holder<Biome> frozenOcean,
    	Holder<Biome> warmOcean,
    	Holder<Biome> ocean,
    	Holder<Biome> snowyBeach,
    	Holder<Biome> stonyBeach,
    	Holder<Biome> beach,
    	Holder<Biome> frozenRiver,
    	Holder<Biome> river
    ) {
        super(noiseGenerator);
        this.climates = climates;
        this.plains = plains;
        this.deepColdOcean = deepColdOcean;
        this.deepFrozenOcean = deepFrozenOcean;
        this.deepLukewarmOcean = deepLukewarmOcean;
        this.deepOcean = deepOcean;
        this.coldOcean = coldOcean;
        this.frozenOcean = frozenOcean;
        this.warmOcean = warmOcean;
        this.ocean = ocean;
        this.snowyBeach = snowyBeach;
        this.stonyBeach = stonyBeach;
        this.beach = beach;
        this.frozenRiver = frozenRiver;
        this.river = river;
    }

    public Holder<Biome> sampleBiome(int seed, int x, int z) {
        var sample = this.getSample(seed, x, z);
        var climateType = BiomeType.get(sample.temperature, sample.moisture);
        var biome = this.getInitialBiome(sample.biomeNoise, climateType);
        return this.getBiomeOverride(biome, sample);
    }

    private Holder<Biome> getInitialBiome(float noise, BiomeType climateType) {
        var climate = this.climates.get(climateType).value();
        if (climate == null || climate.isEmpty()) {
            return this.plains;
        }
        return climate.getValue(noise);
    }

    protected Holder<Biome> getBiomeOverride(Holder<Biome> input, ClimateSample sample) {
        var climateType = BiomeType.get(sample.temperature, sample.moisture);

        if (sample.continentNoise <= ContinentPoints.SHALLOW_OCEAN) {
            return switch (climateType) {
                case TAIGA, COLD_STEPPE -> this.deepColdOcean;
                case TUNDRA -> this.deepFrozenOcean;
                case DESERT, SAVANNA, TROPICAL_RAINFOREST -> this.deepLukewarmOcean;
                default -> this.deepOcean;
            };
        }

        if (sample.continentNoise <= ContinentPoints.BEACH) {
            return switch (climateType) {
                case TAIGA, COLD_STEPPE -> this.coldOcean;
                case TUNDRA -> this.frozenOcean;
                case DESERT, SAVANNA, TROPICAL_RAINFOREST -> this.warmOcean;
                default -> this.ocean;
            };
        }

        if (sample.continentNoise <= ContinentPoints.BEACH + this.beachSize) {
            return switch (climateType) {
                case TUNDRA -> this.snowyBeach;
                case COLD_STEPPE -> this.stonyBeach;
                default -> this.beach;
            };
        }

        if ((sample.terrainType.isRiver() || sample.terrainType.isLake()) && sample.riverNoise == 0) {
            return climateType == BiomeType.TUNDRA ? this.frozenRiver : this.river;
        }

        return input;
    }
}
