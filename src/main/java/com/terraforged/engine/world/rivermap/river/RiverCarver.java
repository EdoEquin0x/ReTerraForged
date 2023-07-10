/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.rivermap.river;

import java.util.Random;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.engine.world.terrain.populator.TerrainPopulator;
import com.terraforged.noise.Source;
import com.terraforged.noise.func.CurveFunc;
import com.terraforged.noise.func.SCurve;
import com.terraforged.noise.source.Line;
import com.terraforged.noise.util.NoiseUtil;

public class RiverCarver extends TerrainPopulator implements Comparable<RiverCarver> {
    public final boolean main;
    private final boolean connecting;
    private final float fade;
    private final float fadeInv;
    private final Range bedWidth;
    private final Range banksWidth;
    private final Range valleyWidth;
    private final Range bedDepth;
    private final Range banksDepth;
    private final float waterLine;
    public final River river;
    public final RiverWarp warp;
    public final RiverConfig config;
    public final CurveFunc valleyCurve;

    public RiverCarver(River river, RiverWarp warp, RiverConfig config, Settings settings, Levels levels) {
        super(TerrainType.RIVER, Source.ZERO, Source.ZERO, 1.0f);
        this.fade = settings.fadeIn;
        this.fadeInv = 1.0f / settings.fadeIn;
        this.bedWidth = new Range(0.25f, config.bedWidth * config.bedWidth);
        this.banksWidth = new Range(1.5625f, config.bankWidth * config.bankWidth);
        this.valleyWidth = new Range(settings.valleySize * settings.valleySize, settings.valleySize * settings.valleySize);
        this.river = river;
        this.warp = warp;
        this.config = config;
        this.main = config.main;
        this.connecting = settings.connecting;
        this.waterLine = levels.water;
        this.bedDepth = new Range(levels.water, config.bedHeight);
        this.banksDepth = new Range(config.minBankHeight, config.maxBankHeight);
        this.valleyCurve = settings.valleyCurve;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
    }

    @Override
    public int compareTo(RiverCarver o) {
        return Integer.compare(this.config.order, o.config.order);
    }

    public void carve(Cell cell, float px, float pz, float pt, float x, float z, float t) {
        float bedAlpha;
        float d2 = this.getDistance2(x, z, t);
        float pd2 = this.getDistance2(px, pz, pt);
        float valleyAlpha = this.getDistanceAlpha(pt, Math.min(d2, pd2), this.valleyWidth);
        if (valleyAlpha == 0.0f) {
            return;
        }
        float bankHeight = this.getScaledSize(t, this.banksDepth);
        valleyAlpha = this.valleyCurve.apply(valleyAlpha);
        cell.riverMask = Math.min(cell.riverMask, 1.0f - valleyAlpha);
        cell.value = Math.min(NoiseUtil.lerp(cell.value, bankHeight, valleyAlpha), cell.value);
        if (!this.connecting || t > 1.0f) {
            // empty if block
        }
        float mouthModifier = RiverCarver.getMouthModifier(cell);
        float bedHeight = this.getScaledSize(t, this.bedDepth);
        float banksAlpha = this.getDistanceAlpha(t, d2 * mouthModifier, this.banksWidth);
        if (banksAlpha == 0.0f) {
            return;
        }
        if (cell.value > bedHeight) {
            cell.value = Math.min(NoiseUtil.lerp(cell.value, bedHeight, banksAlpha), cell.value);
            this.tag(cell, bedHeight);
        }
        if ((bedAlpha = this.getDistanceAlpha(t, d2, this.bedWidth)) != 0.0f && cell.value > bedHeight) {
            cell.value = NoiseUtil.lerp(cell.value, bedHeight, bedAlpha);
            this.tag(cell, bedHeight);
        }
    }

    public RiverConfig createForkConfig(float t, Levels levels) {
        int bedHeight = levels.scale(this.getScaledSize(t, this.bedDepth));
        int bedWidth = (int)Math.round(Math.sqrt(this.getScaledSize(t, this.bedWidth)) * 0.75);
        int bankWidth = (int)Math.round(Math.sqrt(this.getScaledSize(t, this.banksWidth)) * 0.75);
        bedWidth = Math.max(1, bedWidth);
        bankWidth = Math.max(bedWidth + 1, bankWidth);
        return this.config.createFork(bedHeight, bedWidth, bankWidth, levels);
    }

    private float getDistance2(float x, float y, float t) {
        if (t <= 0.0f) {
            return Line.dist2(x, y, this.river.x1, this.river.z1);
        }
        if (t >= 1.0f) {
            return Line.dist2(x, y, this.river.x2, this.river.z2);
        }
        float px = this.river.x1 + t * this.river.dx;
        float py = this.river.z1 + t * this.river.dz;
        return Line.dist2(x, y, px, py);
    }

    private float getDistanceAlpha(float t, float dist2, Range range) {
        float size2 = this.getScaledSize(t, range);
        if (dist2 >= size2) {
            return 0.0f;
        }
        return 1.0f - dist2 / size2;
    }

    private float getScaledSize(float t, Range range) {
        if (t < 0.0f) {
            return range.min;
        }
        if (t > 1.0f) {
            return range.max;
        }
        if (range.min == range.max) {
            return range.min;
        }
        if (t >= this.fade) {
            return range.max;
        }
        return NoiseUtil.lerp(range.min, range.max, t * this.fadeInv);
    }

    private void tag(Cell cell, float bedHeight) {
        if (cell.terrain.overridesRiver() && (cell.value < bedHeight || cell.value > this.waterLine)) {
            return;
        }
        cell.erosionMask = true;
        if (cell.value <= this.waterLine) {
            cell.terrain = TerrainType.RIVER;
        }
    }

    private static float getMouthModifier(Cell cell) {
        float modifier = NoiseUtil.map(cell.continentEdge, 0.0f, 0.5f, 0.5f);
        modifier *= modifier;
        return modifier;
    }

    public static CurveFunc getValleyType(Random random) {
        int value = random.nextInt(100);
        if (value < 5) {
            return new SCurve(0.4f, 1.0f);
        }
        if (value < 30) {
            return new SCurve(4.0f, 5.0f);
        }
        if (value < 50) {
            return new SCurve(3.0f, 0.25f);
        }
        return new SCurve(2.0f, -0.5f);
    }

    public static RiverCarver create(float x1, float z1, float x2, float z2, RiverConfig config, Levels levels, Random random) {
        River river = new River(x1, z1, x2, z2);
        RiverWarp warp = RiverWarp.create(0.35f, random);
        float valleyWidth = 275.0f * River.MAIN_VALLEY.next(random);
        Settings settings = RiverCarver.creatSettings(random);
        settings.connecting = false;
        settings.fadeIn = config.fade;
        settings.valleySize = valleyWidth;
        return new RiverCarver(river, warp, config, settings, levels);
    }

    private static Settings creatSettings(Random random) {
        Settings settings = new Settings();
        settings.valleyCurve = RiverCarver.getValleyType(random);
        return settings;
    }

    public static class Settings {
        public float valleySize = 275.0f;
        public float fadeIn = 0.7f;
        public boolean connecting = false;
        public CurveFunc valleyCurve = new SCurve(2.0f, -0.5f);
    }
}

