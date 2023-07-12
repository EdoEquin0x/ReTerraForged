/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain.region;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.heightmap.RegionConfig;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.domain.Domain;
import com.terraforged.mod.noise.func.DistanceFunc;
import com.terraforged.mod.noise.func.EdgeFunc;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.noise.util.Vec2f;
import com.terraforged.mod.util.pos.PosUtil;

public class RegionModule implements Populator {
	public static final Codec<RegionModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("seed").forGetter((p) -> p.seed),
		Codec.INT.fieldOf("scale").forGetter((p) -> p.scale),
		Module.CODEC.fieldOf("warp_x").forGetter((p) -> p.warpX),
		Module.CODEC.fieldOf("warp_z").forGetter((p) -> p.warpZ),
		Module.CODEC.fieldOf("warp_strength").forGetter((p) -> p.warpStrength)	
	).apply(instance, RegionModule::new));
	
    private static final float JITTER = 0.7f;
    private final int seed;
    private final int scale;
    private final Module warpX;
    private final Module warpZ;
    private final Module warpStrength;
    private final int offsetSeed;
    private final float frequency;
    private final float edgeMin;
    private final float edgeMax;
    private final float edgeRange;
    private final Domain warp;

    public RegionModule(RegionConfig config) {
    	this(config.seed, config.scale, config.warpX, config.warpZ, Source.constant(config.warpStrength));
    }
    
    public RegionModule(int seed, int scale, Module warpX, Module warpZ, Module warpStrength) {
    	this.seed = seed;
    	this.scale = scale;
    	this.warpX = warpX;
    	this.warpZ = warpZ;
    	this.warpStrength = warpStrength;
    	this.offsetSeed = seed + 7;
        this.edgeMin = 0.0f;
        this.edgeMax = 0.5f;
        this.edgeRange = this.edgeMax - this.edgeMin;
        this.frequency = 1.0f / (float) scale;
        this.warp = Domain.warp(warpX, warpZ, warpStrength);
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        float ox = this.warp.getOffsetX(x, y);
        float oz = this.warp.getOffsetY(x, y);
        float px = x + ox;
        float py = y + oz;
        int cellX = 0;
        int cellY = 0;
        float centerX = 0.0f;
        float centerY = 0.0f;
        int xi = NoiseUtil.floor(px *= this.frequency);
        int yi = NoiseUtil.floor(py *= this.frequency);
        float edgeDistance = Float.MAX_VALUE;
        float edgeDistance2 = Float.MAX_VALUE;
        DistanceFunc dist = DistanceFunc.NATURAL;
        for (int dy = -1; dy <= 1; ++dy) {
            for (int dx = -1; dx <= 1; ++dx) {
                int cx = xi + dx;
                int cy = yi + dy;
                Vec2f vec = NoiseUtil.cell(this.offsetSeed, cx, cy);
                float vecX = (float)cx + vec.x * JITTER;
                float vecY = (float)cy + vec.y * JITTER;
                float distance = dist.apply(vecX - px, vecY - py);
                if (distance < edgeDistance) {
                    edgeDistance2 = edgeDistance;
                    edgeDistance = distance;
                    centerX = vecX;
                    centerY = vecY;
                    cellX = cx;
                    cellY = cy;
                    continue;
                }
                if (!(distance < edgeDistance2)) continue;
                edgeDistance2 = distance;
            }
        }
        cell.terrainRegionId = this.cellValue(this.offsetSeed, cellX, cellY);
        cell.terrainRegionEdge = this.edgeValue(edgeDistance, edgeDistance2);
        cell.terrainRegionCenter = PosUtil.pack(centerX / this.frequency, centerY / this.frequency);
    }
    
	@Override
	public Codec<RegionModule> codec() {
		return CODEC;
	}

    private float cellValue(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1.0f, 1.0f, 2.0f);
    }

    private float edgeValue(float distance, float distance2) {
        EdgeFunc edge = EdgeFunc.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        float edgeValue = 1.0f - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        if ((edgeValue = NoiseUtil.pow(edgeValue, 1.5f)) < this.edgeMin) {
            return 0.0f;
        }
        if (edgeValue > this.edgeMax) {
            return 1.0f;
        }
        return (edgeValue - this.edgeMin) / this.edgeRange;
    }
}

