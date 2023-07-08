/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.continent.advanced;

import com.terraforged.engine.Seed;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.settings.WorldSettings;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.continent.SimpleContinent;
import com.terraforged.engine.world.rivermap.Rivermap;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import com.terraforged.noise.source.Line;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.noise.util.Vec2f;

public class AdvancedContinentGenerator extends AbstractContinent implements SimpleContinent {
    protected static final float CENTER_CORRECTION = 0.35f;
    protected final float frequency;
    protected final float variance;
    protected final int varianceSeed;
    protected final Domain warp;
    protected final Module cliffNoise;
    protected final Module bayNoise;

    public AdvancedContinentGenerator(Seed seed, GeneratorContext context) {
        super(seed, context);
        WorldSettings settings = context.settings.world;
        int tectonicScale = settings.continent.continentScale * 4;
        this.frequency = 1.0f / (float)tectonicScale;
        this.varianceSeed = seed.next();
        this.variance = settings.continent.continentSizeVariance;
        this.warp = this.createWarp(seed, tectonicScale, settings.continent);
        this.cliffNoise = Source.build(seed.next(), this.continentScale / 2, 2).build(Source.SIMPLEX2).clamp(0.1, 0.25).map(0.0, 1.0).freq(1.0f / this.frequency, 1.0f / this.frequency);
        this.bayNoise = Source.simplex(seed.next(), 100, 1).scale(0.1).bias(0.9).freq(1.0f / this.frequency, 1.0f / this.frequency);
    }

    @Override
    public void apply(int seed, Cell cell, float x, float y) {
        float wx = this.warp.getX(seed, x, y);
        float wy = this.warp.getY(seed, x, y);
        x = wx * this.frequency;
        y = wy * this.frequency;
        int xi = NoiseUtil.floor(x);
        int yi = NoiseUtil.floor(y);
        int cellX = xi;
        int cellY = yi;
        float cellPointX = x;
        float cellPointY = y;
        float nearest = Float.MAX_VALUE;
        for (int cy = yi - 1; cy <= yi + 1; ++cy) {
            for (int cx = xi - 1; cx <= xi + 1; ++cx) {
                Vec2f vec = NoiseUtil.cell(this.seed, cx, cy);
                float px = (float)cx + vec.x * this.jitter;
                float py = (float)cy + vec.y * this.jitter;
                float dist2 = Line.dist2(x, y, px, py);
                if (!(dist2 < nearest)) continue;
                cellPointX = px;
                cellPointY = py;
                cellX = cx;
                cellY = cy;
                nearest = dist2;
            }
        }
        nearest = Float.MAX_VALUE;
        float sumX = 0.0f;
        float sumY = 0.0f;
        for (int cy = cellY - 1; cy <= cellY + 1; ++cy) {
            for (int cx = cellX - 1; cx <= cellX + 1; ++cx) {
                if (cx == cellX && cy == cellY) continue;
                Vec2f vec = NoiseUtil.cell(this.seed, cx, cy);
                float px = (float)cx + vec.x * this.jitter;
                float py = (float)cy + vec.y * this.jitter;
                float dist2 = AdvancedContinentGenerator.getDistance(x, y, cellPointX, cellPointY, px, py);
                sumX += px;
                sumY += py;
                if (!(dist2 < nearest)) continue;
                nearest = dist2;
            }
        }
        if (this.shouldSkip(cellX, cellY)) {
            return;
        }
        cell.continentId = AdvancedContinentGenerator.getCellValue(this.seed, cellX, cellY);
        cell.continentEdge = this.getDistanceValue(seed, x, y, cellX, cellY, nearest);
        cell.continentX = this.getCorrectedContinentCentre(cellPointX, sumX / 8.0f);
        cell.continentZ = this.getCorrectedContinentCentre(cellPointY, sumY / 8.0f);
    }

    @Override
    public float getEdgeValue(int seed, float x, float z) {
        try (Resource<Cell> resource = Cell.getResource();){
            Cell cell = resource.get();
            this.apply(seed, cell, x, z);
            float f = cell.continentEdge;
            return f;
        }
    }

    @Override
    public long getNearestCenter(int seed, float x, float z) {
        try (Resource<Cell> resource = Cell.getResource();){
            Cell cell = resource.get();
            this.apply(seed, cell, x, z);
            long l = PosUtil.pack(cell.continentX, cell.continentZ);
            return l;
        }
    }

    @Override
    public Rivermap getRivermap(int seed, int x, int z) {
        return this.riverCache.getRivers(seed, x, z);
    }

    protected Domain createWarp(Seed seed, int tectonicScale, WorldSettings.Continent continent) {
        int warpScale = NoiseUtil.round((float)tectonicScale * 0.225f);
        double strength = NoiseUtil.round((float)tectonicScale * 0.33f);
        return Domain.warp(Source.build(seed.next(), warpScale, continent.continentNoiseOctaves).gain(continent.continentNoiseGain).lacunarity(continent.continentNoiseLacunarity).build(Source.PERLIN2), Source.build(seed.next(), warpScale, continent.continentNoiseOctaves).gain(continent.continentNoiseGain).lacunarity(continent.continentNoiseLacunarity).build(Source.PERLIN2), Source.constant(strength));
    }

    protected float getDistanceValue(int seed, float x, float y, int cellX, int cellY, float distance) {
        distance = this.getVariedDistanceValue(cellX, cellY, distance);
        distance = NoiseUtil.sqrt(distance);
        distance = NoiseUtil.map(distance, 0.05f, 0.25f, 0.2f);
        if ((distance = this.getCoastalDistanceValue(seed, x, y, distance)) < this.controlPoints.inland && distance >= this.controlPoints.shallowOcean) {
            distance = this.getCoastalDistanceValue(seed, x, y, distance);
        }
        return distance;
    }

    protected float getVariedDistanceValue(int cellX, int cellY, float distance) {
        if (this.variance > 0.0f && !this.isDefaultContinent(cellX, cellY)) {
            float sizeValue = AdvancedContinentGenerator.getCellValue(this.varianceSeed, cellX, cellY);
            float sizeModifier = NoiseUtil.map(sizeValue, 0.0f, this.variance, this.variance);
            distance *= sizeModifier;
        }
        return distance;
    }

    protected float getCoastalDistanceValue(int seed, float x, float y, float distance) {
        if (distance > this.controlPoints.shallowOcean && distance < this.controlPoints.inland) {
            float alpha = distance / this.controlPoints.inland;
            float cliff = this.cliffNoise.getValue(seed, x, y);
            if ((distance = NoiseUtil.lerp(distance * cliff, distance, alpha)) < this.controlPoints.shallowOcean) {
                distance = this.controlPoints.shallowOcean * this.bayNoise.getValue(seed, x, y);
            }
        }
        return distance;
    }

    protected int getCorrectedContinentCentre(float point, float average) {
        point = NoiseUtil.lerp(point, average, 0.35f) / this.frequency;
        return (int)point;
    }

    protected static float midPoint(float a, float b) {
        return (a + b) * 0.5f;
    }

    protected static float getDistance(float x, float y, float ax, float ay, float bx, float by) {
        float mx = AdvancedContinentGenerator.midPoint(ax, bx);
        float my = AdvancedContinentGenerator.midPoint(ay, by);
        float dx = bx - ax;
        float dy = by - ay;
        float nx = -dy;
        float ny = dx;
        return AdvancedContinentGenerator.getDistance2Line(x, y, mx, my, mx + nx, my + ny);
    }

    protected static float getDistance2Line(float x, float y, float ax, float ay, float bx, float by) {
        float dx = bx - ax;
        float dy = by - ay;
        float v = (x - ax) * dx + (y - ay) * dy;
        float ox = ax + dx * (v /= dx * dx + dy * dy);
        float oy = ay + dy * v;
        return Line.dist2(x, y, ox, oy);
    }
}

