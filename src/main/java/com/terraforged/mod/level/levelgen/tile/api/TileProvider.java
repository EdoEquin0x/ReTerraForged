/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.tile.api;

import com.terraforged.mod.concurrent.Disposable;
import com.terraforged.mod.concurrent.task.LazyCallable;
import com.terraforged.mod.level.levelgen.tile.Tile;
import com.terraforged.mod.level.levelgen.tile.chunk.ChunkReader;

public interface TileProvider extends Disposable.Listener<Tile> {
    public int chunkToRegion(int var1);

    public void queueChunk(int var1, int var2);

    public void queueRegion(int var1, int var2);

    public LazyCallable<Tile> get(long var1);

    public LazyCallable<Tile> getOrCompute(long var1);

    default public Tile getTile(long id) {
        return this.getOrCompute(id).get();
    }

    default public Tile getTile(int regionX, int regionZ) {
        return this.getTile(Tile.getRegionId(regionX, regionZ));
    }

    default public Tile getTileIfPresent(long id) {
        LazyCallable<Tile> entry = this.get(id);
        if (entry == null || !entry.isDone()) {
            return null;
        }
        return entry.get();
    }

    default public Tile getTileIfPresent(int regionX, int regionZ) {
        return this.getTileIfPresent(Tile.getRegionId(regionX, regionZ));
    }

    default public ChunkReader getChunk(int chunkX, int chunkZ) {
        int regionX = this.chunkToRegion(chunkX);
        int regionZ = this.chunkToRegion(chunkZ);
        return this.getTile(regionX, regionZ).getChunk(chunkX, chunkZ);
    }
}

