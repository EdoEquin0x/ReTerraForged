/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain;

import com.terraforged.mod.level.levelgen.heightmap.Levels;
import com.terraforged.mod.level.levelgen.noise.module.Valley;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.level.levelgen.settings.TerrainSettings;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.domain.Domain;
import com.terraforged.mod.noise.func.CurveFunc;
import com.terraforged.mod.noise.func.EdgeFunc;
import com.terraforged.mod.noise.func.Interpolation;

public class LandForms {
    private static final int PLAINS_H = 250;
    private static final int MOUNTAINS_H = 410;
    private static final double MOUNTAINS_V = 0.7;
    private static final int MOUNTAINS2_H = 400;
    private static final double MOUNTAINS2_V = 0.645;
    private final TerrainSettings settings;
    private final float terrainVerticalScale;
    private final float seaLevel;
    private final Module ground;

    public LandForms(TerrainSettings settings, Levels levels, Module ground) {
        this.settings = settings;
        this.ground = ground;
        this.terrainVerticalScale = settings.general().verticalScale();
        this.seaLevel = levels.water;
    }

    public Module getOceanBase() {
        return Source.ZERO;
    }

    public Module getLandBase() {
        return this.ground;
    }

    public Module deepOcean(int seed) {
        Module hills = Source.perlin(++seed, 150, 3).scale(this.seaLevel * 0.7).bias(Source.perlin(++seed, 200, 1).scale(this.seaLevel * 0.2f));
        Module canyons = Source.perlin(++seed, 150, 4).powCurve(0.2).invert().scale(this.seaLevel * MOUNTAINS_V).bias(Source.perlin(++seed, 170, 1).scale(this.seaLevel * 0.15f));
        return Source.perlin(++seed, 500, 1).blend(hills, canyons, 0.6, 0.65).warp(++seed, 50, 2, 50.0);
    }

    public Module steppe(Seed seed) {
        int scaleH = Math.round(PLAINS_H * this.settings.steppe().horizontalScale());
        double erosionAmount = 0.45;
        Module erosion = Source.build(seed.next(), scaleH * 2, 3).lacunarity(3.75).perlin().alpha(erosionAmount);
        Module warpX = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.0).perlin();
        Module warpY = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.0).perlin();
        Module module = Source.perlin(seed.next(), scaleH, 1).mul(erosion).warp(warpX, warpY, Source.constant((float)scaleH / 4.0f)).warp(seed.next(), 256, 1, 200.0);
        return module.scale(0.08).bias(-0.02);
    }

    public Module plains(Seed seed) {
        int scaleH = Math.round(250.0f * this.settings.plains().horizontalScale());
        double erosionAmount = 0.45;
        Module erosion = Source.build(seed.next(), scaleH * 2, 3).lacunarity(3.75).perlin().alpha(erosionAmount);
        Module warpX = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.5).perlin();
        Module warpY = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.5).perlin();
        Module module = Source.perlin(seed.next(), scaleH, 1).mul(erosion).warp(warpX, warpY, Source.constant((float)scaleH / 4.0f)).warp(seed.next(), 256, 1, 256.0);
        return module.scale(0.15f * this.terrainVerticalScale).bias(-0.02);
    }

    public Module plateau(Seed seed) {
        Module valley = Source.ridge(seed.next(), 500, 1).invert().warp(seed.next(), 100, 1, 150.0).warp(seed.next(), 20, 1, 15.0);
        Module top = Source.build(seed.next(), 150, 3).lacunarity(2.45).ridge().warp(seed.next(), 300, 1, 150.0).warp(seed.next(), 40, 2, 20.0).scale(0.15).mul(valley.clamp(0.02, 0.1).map(0.0, 1.0));
        Module surface = Source.perlin(seed.next(), 20, 3).scale(0.05).warp(seed.next(), 40, 2, 20.0);
        Module module = valley.mul(Source.cubic(seed.next(), 500, 1).scale(0.6).bias(0.3)).add(top).terrace(Source.constant(0.9), Source.constant(0.15), Source.constant(0.35), 4, 0.4).add(surface);
        return module.scale(0.475 * this.terrainVerticalScale);
    }

    public Module hills1(Seed seed) {
        return Source.perlin(seed.next(), 200, 3).mul(Source.billow(seed.next(), MOUNTAINS2_H, 3).alpha(0.5)).warp(seed.next(), 30, 3, 20.0).warp(seed.next(), MOUNTAINS2_H, 3, 200.0).scale(0.6f * this.terrainVerticalScale);
    }

    public Module hills2(Seed seed) {
        return Source.cubic(seed.next(), 128, 2).mul(Source.perlin(seed.next(), 32, 4).alpha(0.075)).warp(seed.next(), 30, 3, 20.0).warp(seed.next(), MOUNTAINS2_H, 3, 200.0).mul(Source.ridge(seed.next(), 512, 2).alpha(0.8)).scale(0.55f * this.terrainVerticalScale);
    }

    public Module dales(Seed seed) {
        Module hills1 = Source.build(seed.next(), 300, 4).gain(0.8).lacunarity(4.0).billow().powCurve(0.5).scale(0.75);
        Module hills2 = Source.build(seed.next(), 350, 3).gain(0.8).lacunarity(4.0).billow().pow(1.25);
        Module combined = Source.perlin(seed.next(), MOUNTAINS2_H, 1).clamp(0.3, 0.6).map(0.0, 1.0).blend(hills1, hills2, 0.4, 0.75);
        Module hills = combined.pow(1.125).warp(seed.next(), 300, 1, 100.0);
        return hills.scale(0.4);
    }

    public Module mountains(Seed seed) {
        int scaleH = Math.round(MOUNTAINS_H * this.settings.mountains().horizontalScale());
        Module module = Source.build(seed.next(), scaleH, 4).gain(1.15).lacunarity(2.35).ridge().mul(Source.perlin(seed.next(), 24, 4).alpha(0.075)).warp(seed.next(), 350, 1, 150.0);
        return this.makeFancy(seed, module).scale(MOUNTAINS_V * this.terrainVerticalScale);
    }

    public Module mountains2(Seed seed) {
        Module cell = Source.cellEdge(seed.next(), 360, EdgeFunc.DISTANCE_2).scale(1.2).clamp(0.0, 1.0).warp(seed.next(), 200, 2, 100.0);
        Module blur = Source.perlin(seed.next(), 10, 1).alpha(0.025);
        Module surface = Source.ridge(seed.next(), 125, 4).alpha(0.37);
        Module mountains = cell.clamp(0.0, 1.0).mul(blur).mul(surface).pow(1.1);
        return this.makeFancy(seed, mountains).scale(MOUNTAINS2_V * this.terrainVerticalScale);
    }

    public Module mountains3(Seed seed) {
        Module cell = Source.cellEdge(seed.next(), MOUNTAINS2_H, EdgeFunc.DISTANCE_2).scale(1.2).clamp(0.0, 1.0).warp(seed.next(), 200, 2, 100.0);
        Module blur = Source.perlin(seed.next(), 10, 1).alpha(0.025);
        Module surface = Source.ridge(seed.next(), 125, 4).alpha(0.37);
        Module mountains = cell.clamp(0.0, 1.0).mul(blur).mul(surface).pow(1.1);
        Module terraced = mountains.terrace(Source.perlin(seed.next(), 50, 1).scale(0.5), Source.perlin(seed.next(), 100, 1).clamp(0.5, 0.95).map(0.0, 1.0), Source.constant(0.45), 0.2f, 0.45f, 24, 1);
        return this.makeFancy(seed, terraced).scale(MOUNTAINS2_V * this.terrainVerticalScale);
    }

    public Module badlands(Seed seed) {
        Module mask = Source.build(seed.next(), 270, 3).perlin().clamp(0.35, 0.65).map(0.0, 1.0);
        Module hills = Source.ridge(seed.next(), 275, 4).warp(seed.next(), MOUNTAINS2_H, 2, 100.0).warp(seed.next(), 18, 1, 20.0).mul(mask);
        double modulation = 0.4;
        double alpha = 1.0 - modulation;
        Module mod1 = hills.warp(seed.next(), 100, 1, 50.0).scale(modulation);
        Module lowFreq = hills.steps(4, 0.6, MOUNTAINS_V).scale(alpha).add(mod1);
        Module highFreq = hills.steps(10, 0.6, MOUNTAINS_V).scale(alpha).add(mod1);
        Module detail = lowFreq.add(highFreq);
        Module mod2 = hills.mul(Source.perlin(seed.next(), 200, 3).scale(modulation));
        Module shape = hills.steps(4, 0.65, 0.75, (CurveFunc)Interpolation.CURVE3).scale(alpha).add(mod2).scale(alpha);
        Module badlands = shape.mul(detail.alpha(0.5));
        return badlands.scale(0.55).bias(0.025);
    }

    public Module torridonian(Seed seed) {
        Module plains = Source.perlin(seed.next(), 100, 3).warp(seed.next(), 300, 1, 150.0).warp(seed.next(), 20, 1, 40.0).scale(0.15);
        Module hills = Source.perlin(seed.next(), 150, 4).warp(seed.next(), 300, 1, 200.0).warp(seed.next(), 20, 2, 20.0).boost();
        Module module = Source.perlin(seed.next(), 200, 3).blend(plains, hills, 0.6, 0.6).terrace(Source.perlin(seed.next(), 120, 1).scale(0.25), Source.perlin(seed.next(), 200, 1).scale(0.5).bias(0.5), Source.constant(0.5), 0.0, 0.3, 6, 1).boost();
        return module.scale(0.5);
    }

    private Module makeFancy(Seed seed, Module module) {
        if (this.settings.general().fancyMountains()) {
            Domain warp = Domain.direction(Source.perlin(seed.next(), 10, 1), Source.constant(2.0));
            Valley erosion = new Valley(seed.next(), 2, 0.65f, 128.0f, 0.15f, 3.1f, 0.8f, Valley.Mode.CONSTANT);
            return erosion.wrap(module).warp(warp);
        }
        return module;
    }
}

