/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.rivermap.wetland;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.util.Boundsf;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.engine.world.terrain.populator.TerrainPopulator;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.source.Line;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.noise.util.Vec2f;

public class Wetland extends TerrainPopulator {
    private static final float VALLEY = 0.65f;
    private static final float POOLS = 0.7f;
    private static final float BANKS = 0.050000012f;
    private final Vec2f a;
    private final Vec2f b;
    private final float radius;
    private final float radius2;
    private final float bed;
    private final float banks;
    private final float moundMin;
    private final float moundMax;
    private final float moundVariance;
    private final Module moundShape;
    private final Module moundHeight;
    private final Module terrainEdge;

    public Wetland(int seed, Vec2f a, Vec2f b, float radius, Levels levels) {
        super(TerrainType.WETLAND, Source.ZERO, Source.ZERO, 1.0f);
        this.a = a;
        this.b = b;
        this.radius = radius;
        this.radius2 = radius * radius;
        this.bed = levels.water(-1) - 0.5f / (float)levels.worldHeight;
        this.banks = levels.ground(3);
        this.moundMin = levels.water(1);
        this.moundMax = levels.water(2);
        this.moundVariance = this.moundMax - this.moundMin;
        this.moundShape = Source.perlin(++seed, 10, 1).clamp(0.3, 0.6).map(0.0, 1.0);
        this.moundHeight = Source.simplex(++seed, 20, 1).clamp(0.0, 0.3).map(0.0, 1.0);
        this.terrainEdge = Source.perlin(++seed, 8, 1).clamp(0.2, 0.8).map(0.0, 0.9);
    }

    @Override
    public void apply(int seed, Cell cell, float x, float z) {
        this.apply(seed, cell, x, z, x, z);
    }

    public void apply(int seed, Cell cell, float rx, float rz, float x, float z) {
        if (cell.value < this.bed) {
            return;
        }
        float t = Line.distanceOnLine(rx, rz, this.a.x, this.a.y, this.b.x, this.b.y);
        float d2 = Wetland.getDistance2(rx, rz, this.a.x, this.a.y, this.b.x, this.b.y, t);
        if (d2 > this.radius2) {
            return;
        }
        float dist = 1.0f - d2 / this.radius2;
        if (dist <= 0.0f) {
            return;
        }
        float valleyAlpha = NoiseUtil.map(dist, 0.0f, VALLEY, VALLEY);
        if (cell.value > this.banks) {
            cell.value = NoiseUtil.lerp(cell.value, this.banks, valleyAlpha);
        }
        float poolsAlpha = NoiseUtil.map(dist, VALLEY, POOLS, BANKS);
        if (cell.value > this.bed && cell.value <= this.banks) {
            cell.value = NoiseUtil.lerp(cell.value, this.bed, poolsAlpha);
        }
        if (poolsAlpha >= 1.0f) {
            cell.erosionMask = true;
        }
        if (dist > 0.65f && poolsAlpha > this.terrainEdge.getValue(seed, x, z)) {
            cell.terrain = this.getType();
        }
        if (cell.value >= this.bed && cell.value < this.moundMax) {
            float shapeAlpha = this.moundShape.getValue(seed, x, z) * poolsAlpha;
            float mounds = this.moundMin + this.moundHeight.getValue(seed, x, z) * this.moundVariance;
            cell.value = NoiseUtil.lerp(cell.value, mounds, shapeAlpha);
        }
        cell.riverMask = Math.min(cell.riverMask, 1.0f - valleyAlpha);
    }

    public void recordBounds(Boundsf.Builder builder) {
        builder.record(Math.min(this.a.x, this.b.x) - this.radius, Math.min(this.a.y, this.b.y) - this.radius);
        builder.record(Math.max(this.a.x, this.b.x) + this.radius, Math.max(this.a.y, this.b.y) + this.radius);
    }

    private static float getDistance2(float x, float y, float ax, float ay, float bx, float by, float t) {
        if (t <= 0.0f) {
            return Line.dist2(x, y, ax, ay);
        }
        if (t >= 1.0f) {
            return Line.dist2(x, y, bx, by);
        }
        float px = ax + t * (bx - ax);
        float py = ay + t * (by - ay);
        return Line.dist2(x, y, px, py);
    }
}

