/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.cell;

import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.SimpleResource;
import com.terraforged.engine.concurrent.pool.ThreadLocalPool;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;

public class Cell {
    private static final Cell defaults = new Cell();
    private static final Cell EMPTY = new Cell(){

        @Override
        public boolean isAbsent() {
            return true;
        }
    };
    private static final ThreadLocalPool<Cell> POOL = new ThreadLocalPool<Cell>(32, Cell::new, Cell::reset);
    private static final ThreadLocal<SimpleResource<Cell>> LOCAL = ThreadLocal.withInitial(() -> new SimpleResource<Cell>(new Cell(), Cell::reset));
    public float value;
    public float erosion;
    public float sediment;
    public float gradient;
    public float moisture = 0.5f;
    public float temperature = 0.5f;
    public float continentId;
    public float continentEdge;
    public float terrainRegionId;
    public float terrainRegionEdge;
    public long terrainRegionCenter;
    public float biomeRegionId;
    public float biomeRegionEdge = 1.0f;
    public float macroBiomeId;
    public float riverMask = 1.0f;
    public int continentX;
    public int continentZ;
    public boolean erosionMask = false;
    public Terrain terrain = TerrainType.NONE;
    public BiomeType biome = BiomeType.GRASSLAND;

    public void copyFrom(Cell other) {
        this.value = other.value;
        this.continentX = other.continentX;
        this.continentZ = other.continentZ;
        this.continentId = other.continentId;
        this.continentEdge = other.continentEdge;
        this.terrainRegionId = other.terrainRegionId;
        this.terrainRegionEdge = other.terrainRegionEdge;
        this.biomeRegionId = other.biomeRegionId;
        this.biomeRegionEdge = other.biomeRegionEdge;
        this.riverMask = other.riverMask;
        this.erosionMask = other.erosionMask;
        this.moisture = other.moisture;
        this.temperature = other.temperature;
        this.macroBiomeId = other.macroBiomeId;
        this.gradient = other.gradient;
        this.erosion = other.erosion;
        this.sediment = other.sediment;
        this.biome = other.biome;
        this.terrain = other.terrain;
    }

    public Cell reset() {
        this.copyFrom(defaults);
        return this;
    }

    public boolean isAbsent() {
        return false;
    }

    public static Cell empty() {
        return EMPTY;
    }

    public static Resource<Cell> getResource() {
        SimpleResource<Cell> resource = LOCAL.get();
        if (resource.isOpen()) {
            return POOL.get();
        }
        return resource;
    }

    public static interface ContextVisitor<C> {
        public void visit(Cell var1, int var2, int var3, C var4);
    }

    public static interface Visitor {
        public void visit(Cell var1, int var2, int var3);
    }
}

