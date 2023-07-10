/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.continent.fancy;

import com.terraforged.engine.Seed;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.settings.WorldSettings;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.continent.Continent;
import com.terraforged.engine.world.rivermap.RiverCache;
import com.terraforged.engine.world.rivermap.Rivermap;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import com.terraforged.noise.util.NoiseUtil;

public class FancyContinentGenerator implements Continent {
    private final float frequency;
    private final Domain warp;
    private final FancyContinent source;
    private final RiverCache riverCache;

    public FancyContinentGenerator(Seed seed, GeneratorContext context) {
        WorldSettings settings = context.settings.world;
        int warpScale = settings.continent.continentScale / 2;
        double warpStrength = (double)warpScale * 0.4;
        this.source = new FancyContinent(seed.next(), 4, 0.2f, context, this);
        this.frequency = 1.0f / (float)settings.continent.continentScale;
        this.riverCache = new RiverCache(this.source);
        this.warp = Domain.warp(Source.SIMPLEX, seed.next(), warpScale, 2, warpStrength).add(Domain.warp(seed.next(), 80, 2, 40.0)).add(Domain.warp(seed.next(), 20, 1, 15.0));
    }

    public FancyContinent getSource() {
        return this.source;
    }

    @Override
    public Rivermap getRivermap(int x, int y) {
        return this.riverCache.getRivers(x, y);
    }

    @Override
    public float getEdgeValue(float x, float y) {
        float px = this.warp.getX(x, y);
        float py = this.warp.getY(x, y);
        return this.source.getValue(px *= this.frequency, py *= this.frequency);
    }

    @Override
    public float getLandValue(float x, float y) {
        float px = this.warp.getX(x, y);
        float py = this.warp.getY(x, y);
        float value = this.source.getLandValue(px *= this.frequency, py *= this.frequency);
        return NoiseUtil.map(value, 0.2f, 0.4f, 0.2f);
    }

    @Override
    public long getNearestCenter(float x, float z) {
        long min = this.source.getMin();
        long max = this.source.getMax();
        float width = PosUtil.unpackLeftf(max) - PosUtil.unpackLeftf(min);
        float height = PosUtil.unpackRightf(max) - PosUtil.unpackRightf(min);
        float cx = width * 0.5f;
        float cz = height * 0.5f;
        int centerX = (int)(cx / this.frequency);
        int centerZ = (int)(cz / this.frequency);
        return PosUtil.pack(centerX, centerZ);
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        cell.continentX = 0;
        cell.continentZ = 0;
        cell.continentId = 0.0f;
        cell.continentEdge = this.getEdgeValue(x, y);
    }
}

