/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.heightmap;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.level.levelgen.continent.Continent;
import com.terraforged.mod.level.levelgen.continent.ContinentLerper2;
import com.terraforged.mod.level.levelgen.continent.ContinentLerper3;
import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.noise.module.Blender;
import com.terraforged.mod.level.levelgen.rivermap.Rivermap;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.settings.TerrainSettings;
import com.terraforged.mod.level.levelgen.settings.WorldSettings;
import com.terraforged.mod.level.levelgen.terrain.Terrain;
import com.terraforged.mod.level.levelgen.terrain.TerrainType;
import com.terraforged.mod.level.levelgen.terrain.populator.TerrainPopulator;
import com.terraforged.mod.level.levelgen.terrain.provider.TerrainProvider;
import com.terraforged.mod.level.levelgen.terrain.region.RegionLerper;
import com.terraforged.mod.level.levelgen.terrain.region.RegionModule;
import com.terraforged.mod.level.levelgen.terrain.region.RegionSelector;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.func.EdgeFunc;
import com.terraforged.mod.noise.func.Interpolation;

public class Heightmap implements Populator {
    public static final int MOUNTAIN_SCALE = 1000;
    private static final int REGION_SEED_OFFSET = 789124;
    private static final int WARP_SEED_OFFSET = 8934;
    protected final Continent continentGenerator;
    protected final Populator regionModule;
    private final Levels levels;
    private final Climate climate;
    private final Populator root;
    private final TerrainProvider terrainProvider;
    private final float terrainFrequency;

    public Heightmap(GeneratorContext context) {
    	Settings settings = context.settings;
        WorldSettings world = context.settings.world();
        ControlPoints controlPoints = new ControlPoints(world.controlPoints());
        Seed regionSeed = context.seed.offset(REGION_SEED_OFFSET);
        Seed regionWarp = context.seed.offset(WARP_SEED_OFFSET);
        int regionWarpScale = 400;
        int regionWarpStrength = 200;
        RegionConfig regionConfig = new RegionConfig(regionSeed.get(), context.settings.terrain().general().regionSize(), Source.simplex(regionWarp.next(), regionWarpScale, 1), Source.simplex(regionWarp.next(), regionWarpScale, 1), regionWarpStrength);
        this.levels = context.levels;
        this.terrainFrequency = 1.0f / settings.terrain().general().horizontalScale();
        this.regionModule = new RegionModule(regionConfig);
        Seed mountainSeed = context.seed.offset(context.settings.terrain().general().seedOffset());
        Module mountainShapeBase = Source.cellEdge(mountainSeed.next(), MOUNTAIN_SCALE, EdgeFunc.DISTANCE_2_ADD).warp(mountainSeed.next(), 333, 2, 250.0);
        Module mountainShape = mountainShapeBase.curve(Interpolation.CURVE3).clamp(0.0, 0.9).map(0.0, 1.0);
        this.terrainProvider = context.terrainFactory.create(context, regionConfig);
        RegionSelector terrainRegions = new RegionSelector(this.terrainProvider.getPopulators());
        TerrainPopulator terrainRegionBorders = TerrainPopulator.of(TerrainType.FLATS, this.terrainProvider.getLandforms().getLandBase(), this.terrainProvider.getLandforms().plains(context.seed), settings.terrain().steppe());
        RegionLerper terrain = new RegionLerper(terrainRegionBorders, terrainRegions);
        TerrainPopulator mountains = this.register(TerrainType.MOUNTAIN_CHAIN, this.terrainProvider.getLandforms().getLandBase(), this.terrainProvider.getLandforms().mountains(mountainSeed), settings.terrain().mountains());
        this.continentGenerator = world.continent().type().create(context.seed, context);
        this.climate = new Climate(this.continentGenerator, context);
        Blender land = new Blender(mountainShape, terrain, mountains, 0.3f, 0.8f, 0.575f);
        ContinentLerper3 oceans = new ContinentLerper3(this.register(TerrainType.DEEP_OCEAN, this.terrainProvider.getLandforms().deepOcean(context.seed.next())), this.register(TerrainType.SHALLOW_OCEAN, Source.constant(context.levels.water(-7))), this.register(TerrainType.COAST, Source.constant(context.levels.water)), controlPoints.deepOcean, controlPoints.shallowOcean, controlPoints.coast);
        this.root = new ContinentLerper2(oceans, land, controlPoints.shallowOcean, controlPoints.inland);
    }

    public TerrainProvider getTerrainProvider() {
        return this.terrainProvider;
    }

    public Populator getRegionModule() {
        return this.regionModule;
    }

    public Levels getLevels() {
        return this.levels;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        this.applyBase(cell, x, z);
        this.applyRivers(cell, x, z);
        this.applyClimate(cell, x, z);
    }

    public void applyBase(Cell cell, float x, float z) {
        cell.terrain = TerrainType.FLATS;
        this.continentGenerator.apply(cell, x, z);
        this.regionModule.apply(cell, x, z);
        this.root.apply(cell, x *= this.terrainFrequency, z *= this.terrainFrequency);
    }

    public void applyRivers(Cell cell, float x, float z, Rivermap rivermap) {
        rivermap.apply(cell, x, z);
    }

    public void applyClimate(Cell cell, float x, float z) {
        this.climate.apply(cell, x, z);
    }

    public Climate getClimate() {
        return this.climate;
    }

    public Continent getContinent() {
        return this.continentGenerator;
    }

    public Rivermap getRivermap(int x, int z) {
        return this.continentGenerator.getRivermap(x, z);
    }

    private void applyRivers(Cell cell, float x, float z) {
        this.applyRivers(cell, x, z, this.continentGenerator.getRivermap(cell));
    }

    private TerrainPopulator register(Terrain terrain, Module variance) {
        TerrainPopulator populator = TerrainPopulator.of(terrain, variance);
        this.terrainProvider.registerMixable(populator);
        return populator;
    }

    private TerrainPopulator register(Terrain terrain, Module base, Module variance, TerrainSettings.Terrain settings) {
        TerrainPopulator populator = TerrainPopulator.of(terrain, base, variance, settings);
        this.terrainProvider.registerMixable(populator);
        return populator;
    }
}

