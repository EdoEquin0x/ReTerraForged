/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.terrain.special;

import com.terraforged.engine.Seed;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.heightmap.RegionConfig;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.engine.world.terrain.populator.TerrainPopulator;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.func.EdgeFunc;

public class VolcanoPopulator extends TerrainPopulator {
    private static final float throat_value = 0.925f;
    public static final float RIVER_MASK = 0.85f;
    private final Module cone;
    private final Module height;
    private final Module lowlands;
    private final float inversionPoint;
    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;
    private final float bias;
    private final Terrain inner;
    private final Terrain outer;

    public VolcanoPopulator(Seed seed, RegionConfig region, Levels levels, float weight) {
        super(TerrainType.VOLCANO, Source.ZERO, Source.ZERO, weight);
        float midpoint = 0.3f;
        float range = 0.3f;
        Module heightNoise = Source.perlin(seed.next(), 2, 1).map(0.45, 0.65);
        this.height = Source.cellNoise(region.seed, region.scale, heightNoise).warp(region.warpX, region.warpZ, region.warpStrength);
        this.cone = Source.cellEdge(region.seed, region.scale, EdgeFunc.DISTANCE_2_DIV).invert().warp(region.warpX, region.warpZ, region.warpStrength).powCurve(11.0).clamp(0.475, 1.0).map(0.0, 1.0).grad(0.0, 0.5, 0.5).warp(seed.next(), 15, 2, 10.0).scale(this.height);
        this.lowlands = Source.ridge(seed.next(), 150, 3).warp(seed.next(), 30, 1, 30.0).scale(0.1);
        this.inversionPoint = 0.94f;
        this.blendLower = midpoint - range / 2.0f;
        this.blendUpper = this.blendLower + range;
        this.blendRange = this.blendUpper - this.blendLower;
        this.outer = TerrainType.VOLCANO;
        this.inner = TerrainType.VOLCANO_PIPE;
        this.bias = levels.ground;
    }

    @Override
    public void apply(int seed, Cell cell, float x, float z) {
        float limit;
        float maxHeight;
        float value = this.cone.getValue(seed, x, z);
        if (value > (maxHeight = (limit = this.height.getValue(seed, x, z)) * this.inversionPoint)) {
            float steepnessModifier = 1.0f;
            float delta = (value - maxHeight) * steepnessModifier;
            float range = limit - maxHeight;
            float alpha = delta / range;
            if (alpha > throat_value) {
                cell.terrain = this.inner;
            }
            value = maxHeight - maxHeight / 5.0f * alpha;
        } else if (value < this.blendLower) {
            value += this.lowlands.getValue(seed, x, z);
            cell.terrain = this.outer;
        } else if (value < this.blendUpper) {
            float alpha = 1.0f - (value - this.blendLower) / this.blendRange;
            value += this.lowlands.getValue(seed, x, z) * alpha;
            cell.terrain = this.outer;
        }
        cell.value = this.bias + value;
    }

    public static void modifyVolcanoType(Cell cell, Levels levels) {
        if (cell.terrain == TerrainType.VOLCANO_PIPE && (cell.value < levels.water || cell.riverMask < RIVER_MASK)) {
            cell.terrain = TerrainType.VOLCANO;
        }
    }
}

