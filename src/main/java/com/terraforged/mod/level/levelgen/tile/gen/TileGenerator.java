/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.tile.gen;

import com.terraforged.mod.concurrent.Disposable;
import com.terraforged.mod.concurrent.task.LazyCallable;
import com.terraforged.mod.concurrent.thread.ThreadPool;
import com.terraforged.mod.concurrent.thread.ThreadPools;
import com.terraforged.mod.level.levelgen.generator.WorldGenerator;
import com.terraforged.mod.level.levelgen.tile.Tile;
import com.terraforged.mod.level.levelgen.tile.api.TileFactory;
import com.terraforged.mod.level.levelgen.tile.api.TileProvider;

public class TileGenerator implements TileFactory {
    protected final int factor;
    protected final int border;
    protected final int batchSize;
    protected final ThreadPool threadPool;
    protected final WorldGenerator generator;
    private final TileResources resources = new TileResources();
    private Disposable.Listener<Tile> listener = r -> {};

    protected TileGenerator(Builder builder) {
        this.factor = builder.factor;
        this.border = builder.border;
        this.batchSize = builder.batchSize;
        this.generator = builder.generator;
        this.threadPool = Builder.getOrDefaultPool(builder);
    }

    public WorldGenerator getGenerator() {
        return this.generator;
    }

    @Override
    public void setListener(Disposable.Listener<Tile> listener) {
        this.listener = listener;
    }

    @Override
    public int chunkToRegion(int i) {
        return i >> this.factor;
    }

    @Override
    public LazyCallable<Tile> getTile(int regionX, int regionZ) {
        return new CallableTile(regionX, regionZ, this);
    }

    @Override
    public LazyCallable<Tile> getTile(float centerX, float centerZ, float zoom, boolean filter) {
        return new CallableZoomTile(centerX, centerZ, zoom, filter, this);
    }

    @Override
    public TileFactory async() {
        return new TileGeneratorAsync(this, this.threadPool);
    }

    @Override
    public TileProvider cached() {
        return new TileCache(this, this.threadPool);
    }

    public Tile generateRegion(int regionX, int regionZ) {
        Tile tile = this.createEmptyRegion(regionX, regionZ);
        tile.generate(this.generator.getHeightmap());
        this.postProcess(tile);
        return tile;
    }

    public Tile generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        Tile tile = this.createEmptyRegion(0, 0);
        tile.generate(this.generator.getHeightmap(), centerX, centerZ, zoom);
        this.postProcess(tile, filter);
        return tile;
    }

    public Tile createEmptyRegion(int regionX, int regionZ) {
        return new Tile(regionX, regionZ, this.factor, this.border, this.resources, this.listener);
    }

    protected void postProcess(Tile tile) {
        this.generator.getFilters().apply(tile, true);
    }

    protected void postProcess(Tile tile, boolean filter) {
        this.generator.getFilters().apply(tile, filter);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected int factor = 0;
        protected int border = 0;
        protected int batchSize = 0;
        protected boolean striped = false;
        protected WorldGenerator generator;
        private ThreadPool threadPool;

        public Builder size(int factor, int border) {
            return this.factor(factor).border(border);
        }

        public Builder factor(int factor) {
            this.factor = factor;
            return this;
        }

        public Builder border(int border) {
            this.border = border;
            return this;
        }

        public Builder pool(ThreadPool threadPool) {
            this.threadPool = threadPool;
            return this;
        }

        public Builder generator(WorldGenerator generator) {
            this.generator = generator;
            return this;
        }

        public Builder batch(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder striped() {
            this.striped = true;
            return this;
        }

        public TileGenerator build() {
            if (this.batchSize > 0) {
                if (this.striped) {
                    return new TileGeneratorStriped(this);
                }
                return new TileGeneratorBatched(this);
            }
            return new TileGenerator(this);
        }

        protected static ThreadPool getOrDefaultPool(Builder builder) {
            return builder.threadPool == null ? ThreadPools.NONE : builder.threadPool;
        }
    }
}

