/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.heightmap;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.tile.Tile;
import com.terraforged.engine.tile.api.TileProvider;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.WorldGeneratorFactory;
import com.terraforged.engine.world.terrain.TerrainType;

public class WorldLookup {
    private final float waterLevel;
    private final float beachLevel;
    private final TileProvider cache;
    private final Heightmap heightmap;

    public WorldLookup(GeneratorContext context) {
        this.cache = (TileProvider)context.cache.get();
        this.heightmap = ((WorldGeneratorFactory)context.worldGenerator.get()).getHeightmap();
        this.waterLevel = context.levels.water;
        this.beachLevel = context.levels.water(5);
    }

    public Resource<Cell> get(int x, int z) {
        ChunkReader chunk = this.cache.getChunk(x >> 4, z >> 4);
        Resource<Cell> cell = Cell.getResource();
        cell.get().copyFrom(chunk.getCell(x & 0xF, z & 0xF));
        return cell;
    }

    public Resource<Cell> getCell(int seed, int x, int z) {
        return this.getCell(seed, x, z, false);
    }

    public Resource<Cell> getCell(int seed, int x, int z, boolean load) {
        Resource<Cell> resource = Cell.getResource();
        this.applyCell(seed, resource.get(), x, z, load);
        return resource;
    }

    public void applyCell(int seed, Cell cell, int x, int z) {
        this.applyCell(seed, cell, x, z, false);
    }

    public void applyCell(int seed, Cell cell, int x, int z, boolean load) {
        if (load && this.computeAccurate(cell, x, z)) {
            return;
        }
        if (this.computeCached(cell, x, z)) {
            return;
        }
        this.compute(seed, cell, x, z);
    }

    private boolean computeAccurate(Cell cell, int x, int z) {
        int rx = this.cache.chunkToRegion(x >> 4);
        int rz = this.cache.chunkToRegion(z >> 4);
        Tile tile = this.cache.getTile(rx, rz);
        Cell c = tile.getCell(x, z);
        if (c != null) {
            cell.copyFrom(c);
        }
        return cell.terrain != null;
    }

    private boolean computeCached(Cell cell, int x, int z) {
        int rx = this.cache.chunkToRegion(x >> 4);
        int rz = this.cache.chunkToRegion(z >> 4);
        Tile tile = this.cache.getTileIfPresent(rx, rz);
        if (tile != null) {
            Cell c = tile.getCell(x, z);
            if (c != null) {
                cell.copyFrom(c);
            }
            return cell.terrain != null;
        }
        return false;
    }

    private void compute(int seed, Cell cell, int x, int z) {
        this.heightmap.apply(seed, cell, x, z);
        if (cell.terrain == TerrainType.COAST && cell.value > this.waterLevel && cell.value <= this.beachLevel) {
            cell.terrain = TerrainType.BEACH;
        }
    }
}

