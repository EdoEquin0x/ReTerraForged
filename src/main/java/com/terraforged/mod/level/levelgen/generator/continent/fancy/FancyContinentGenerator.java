/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.continent.fancy;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.generator.continent.Continent;
import com.terraforged.mod.level.levelgen.generator.rivermap.RiverCache;
import com.terraforged.mod.level.levelgen.generator.rivermap.Rivermap;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.level.levelgen.settings.WorldSettings;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.domain.Domain;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.util.pos.PosUtil;

public class FancyContinentGenerator implements Continent {
    private final float frequency;
    private final Domain warp;
    private final FancyContinent source;
    private final RiverCache riverCache;

    public FancyContinentGenerator(Seed seed, GeneratorContext context) {
    	WorldSettings settings = context.settings.world();
        int warpScale = settings.continent().scale() / 2;
        double warpStrength = (double)warpScale * 0.4;
        this.source = new FancyContinent(seed.next(), 4, 0.2f, context, this);
        this.frequency = 1.0f / (float)settings.continent().scale();
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

