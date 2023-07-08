/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.gen;

import com.terraforged.engine.concurrent.Disposable;
import com.terraforged.engine.concurrent.task.LazyCallable;
import com.terraforged.engine.concurrent.thread.ThreadPool;
import com.terraforged.engine.tile.Tile;
import com.terraforged.engine.tile.api.TileFactory;

public class TileGeneratorAsync implements TileFactory {
    protected final TileGenerator generator;
    protected final ThreadPool threadPool;

    public TileGeneratorAsync(TileGenerator generator, ThreadPool threadPool) {
        this.generator = generator;
        this.threadPool = threadPool;
    }

    @Override
    public int chunkToRegion(int i) {
        return this.generator.chunkToRegion(i);
    }

    @Override
    public void setListener(Disposable.Listener<Tile> listener) {
        this.generator.setListener(listener);
    }

    @Override
    public LazyCallable<Tile> getTile(int seed, int regionX, int regionZ) {
        return LazyCallable.callAsync(this.generator.getTile(seed, regionX, regionZ), this.threadPool);
    }

    @Override
    public LazyCallable<Tile> getTile(int seed, float centerX, float centerZ, float zoom, boolean filter) {
        return LazyCallable.callAsync(this.generator.getTile(seed, centerX, centerZ, zoom, filter), this.threadPool);
    }

    @Override
    public TileFactory async() {
        return this;
    }

    @Override
    public TileCache cached() {
        return new TileCache(this.generator, this.threadPool);
    }
}

