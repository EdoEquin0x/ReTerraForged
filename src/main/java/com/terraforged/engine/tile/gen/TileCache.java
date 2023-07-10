/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.gen;

import com.terraforged.engine.concurrent.cache.Cache;
import com.terraforged.engine.concurrent.cache.CacheEntry;
import com.terraforged.engine.concurrent.task.LazyCallable;
import com.terraforged.engine.concurrent.thread.ThreadPool;
import com.terraforged.engine.tile.Tile;
import com.terraforged.engine.tile.api.TileFactory;
import com.terraforged.engine.tile.api.TileProvider;
import java.util.concurrent.TimeUnit;

public class TileCache implements TileProvider {
    public static final int QUEUING_MIN_POOL_SIZE = 4;
    private final boolean canQueue;
    private final TileFactory generator;
    private final ThreadPool threadPool;
    private final Cache<CacheEntry<Tile>> cache;

    public TileCache(TileFactory generator, ThreadPool threadPool) {
        this.canQueue = threadPool.size() > 4;
        this.generator = generator;
        this.threadPool = threadPool;
        this.cache = new Cache<>("TileCache", 256, 60L, 20L, TimeUnit.SECONDS);
        generator.setListener(this);
    }

    @Override
    public void onDispose(Tile tile) {
        this.cache.remove(tile.getRegionId());
    }

    @Override
    public int chunkToRegion(int coord) {
        return this.generator.chunkToRegion(coord);
    }

    public CacheEntry<Tile> get(long id) {
        return this.cache.get(id);
    }

    public CacheEntry<Tile> getOrCompute(long id) {
        return this.cache.computeIfAbsent(id, this::computeCacheEntry);
    }

    @Override
    public void queueChunk(int chunkX, int chunkZ) {
        if (!this.canQueue) {
            return;
        }
        this.queueRegion(this.chunkToRegion(chunkX), this.chunkToRegion(chunkZ));
    }

    @Override
    public void queueRegion(int regionX, int regionZ) {
        if (!this.canQueue) {
            return;
        }
        this.getOrCompute(Tile.getRegionId(regionX, regionZ));
    }

    protected CacheEntry<Tile> computeCacheEntry(long id) {
        int regionX = Tile.getRegionX(id);
        int regionZ = Tile.getRegionZ(id);
        LazyCallable<Tile> tile = this.generator.getTile(regionX, regionZ);
        return CacheEntry.computeAsync(tile, this.threadPool);
    }
}

