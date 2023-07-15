/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain.populator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
//import com.terraforged.mod.level.levelgen.terrain.Terrain;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;

public class TerrainPopulator implements Populator {
	public static final Codec<TerrainPopulator> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Module.CODEC.fieldOf("base").forGetter(TerrainPopulator::getBase),
		Module.CODEC.fieldOf("variance").forGetter(TerrainPopulator::getVariance),
		Codec.FLOAT.fieldOf("weight").forGetter(TerrainPopulator::getWeight)
	).apply(instance, TerrainPopulator::new));
	
//    protected final Terrain type;
    protected final Module base;
    protected final Module variance;
	protected final float weight;

    public TerrainPopulator(/*Terrain type, */Module base, Module variance, float weight) {
//        this.type = type;
        this.base = base;
        this.variance = variance;
        this.weight = weight;
    }
    
    public Module getBase() {
    	return this.base;
    }
    
    public Module getVariance() {
        return this.variance;
    }

    public float getWeight() {
        return this.weight;
    }

//    public Terrain getType() {
//        return this.type;
//    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float base = this.base.getValue(x, z);
        float variance = this.variance.getValue(x, z);
        cell.value = base + variance;
        if (cell.value < 0.0f) {
            cell.value = 0.0f;
        } else if (cell.value > 1.0f) {
            cell.value = 1.0f;
        }
//        cell.terrain = this.type;
    }

    public static Module clamp(Module module) {
        if (module.minValue() < 0.0f || module.maxValue() > 1.0f) {
            return module.clamp(0.0, 1.0);
        }
        return module;
    }

    public static TerrainPopulator of(/*Terrain type, */Module variance) {
        return new TerrainPopulator(/*type, */Source.ZERO, variance, 1.0f);
    }

    public static TerrainPopulator of(/*Terrain type, */Module base, Module variance, float weight, float baseScale, float verticalScale) {
        if (verticalScale == 1.0f &&  baseScale == 1.0f) {
            return new TerrainPopulator(/*type, */base, variance, weight);
        }
        return new ScaledPopulator(/*type, */base, variance,  baseScale, verticalScale, weight);
    }
}

