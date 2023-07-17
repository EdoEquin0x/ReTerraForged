/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent.advanced;

import com.terraforged.mod.level.levelgen.continent.SimpleContinent;
import com.terraforged.mod.level.levelgen.continent.simple.SimpleRiverGenerator;
import com.terraforged.mod.level.levelgen.rivermap.RiverCache;
import com.terraforged.mod.level.levelgen.settings.ControlPoints;
import com.terraforged.mod.level.levelgen.settings.Levels;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.settings.WorldSettings;
import com.terraforged.mod.level.levelgen.util.Seed;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.util.pos.PosUtil;

public abstract class AbstractContinent implements SimpleContinent {
    protected final int seed;
    protected final int skippingSeed;
    protected final int continentScale;
    protected final float jitter;
    protected final boolean hasSkipping;
    protected final float skipThreshold;
    protected final RiverCache riverCache;
    protected final ControlPoints controlPoints;

    public AbstractContinent(Seed seed, Levels levels, Settings settings) {
    	WorldSettings worldSettingssettings = settings.world();
        this.seed = seed.next();
        this.skippingSeed = seed.next();
        this.continentScale = worldSettingssettings.continent().scale();
        this.jitter = worldSettingssettings.continent().jitter();
        this.skipThreshold = worldSettingssettings.continent().skipping();
        this.hasSkipping = this.skipThreshold > 0.0f;
        this.controlPoints = new ControlPoints(worldSettingssettings.controlPoints());
        this.riverCache = new RiverCache(new SimpleRiverGenerator(this, seed, levels, settings));
    }

    @Override
    public float getDistanceToOcean(int cx, int cz, float dx, float dz) {
        float high = this.getDistanceToEdge(cx, cz, dx, dz);
        float low = 0.0f;
        for (int i = 0; i < 50; ++i) {
            float mid = (low + high) / 2.0f;
            float x = (float)cx + dx * mid;
            float z = (float)cz + dz * mid;
            float edge = this.getEdgeValue(x, z);
            if (edge > this.controlPoints.shallowOcean) {
                low = mid;
            } else {
                high = mid;
            }
            if (high - low < 10.0f) break;
        }
        return high;
    }

    @Override
    public float getDistanceToEdge(int cx, int cz, float dx, float dz) {
        float distance = this.continentScale * 4;
        for (int i = 0; i < 10; ++i) {
            float x = (float)cx + dx * distance;
            float z = (float)cz + dz * distance;
            long centerPos = this.getNearestCenter(x, z);
            int conX = PosUtil.unpackLeft(centerPos);
            int conZ = PosUtil.unpackRight(centerPos);
            distance += distance;
            if (conX == cx && conZ == cz) continue;
            float low = 0.0f;
            float high = distance;
            for (int j = 0; j < 50; ++j) {
                float mid = (low + high) / 2.0f;
                float px = (float)cx + dx * mid;
                float pz = (float)cz + dz * mid;
                centerPos = this.getNearestCenter(px, pz);
                conX = PosUtil.unpackLeft(centerPos);
                conZ = PosUtil.unpackRight(centerPos);
                if (conX == cx && conZ == cz) {
                    low = mid;
                } else {
                    high = mid;
                }
                if (high - low < 50.0f) break;
            }
            return high;
        }
        return distance;
    }

    protected boolean isDefaultContinent(int cellX, int cellY) {
        return cellX == 0 && cellY == 0;
    }

    protected boolean shouldSkip(int cellX, int cellY) {
        if (this.hasSkipping && !this.isDefaultContinent(cellX, cellY)) {
            float skipValue = AbstractContinent.getCellValue(this.skippingSeed, cellX, cellY);
            return skipValue < this.skipThreshold;
        }
        return false;
    }

    protected static float getCellValue(int seed, int cellX, int cellY) {
        return 0.5f + NoiseUtil.valCoord2D(seed, cellX, cellY) * 0.5f;
    }
}

