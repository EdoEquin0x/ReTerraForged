/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.gen;

import com.terraforged.engine.concurrent.task.LazyCallable;
import com.terraforged.engine.tile.Tile;

public class CallableTile extends LazyCallable<Tile> {
	private final int seed;
    private final int regionX;
    private final int regionZ;
    private final TileGenerator generator;

    public CallableTile(int seed, int regionX, int regionZ, TileGenerator generator) {
    	this.seed = seed;
    	this.regionX = regionX;
        this.regionZ = regionZ;
        this.generator = generator;
    }

    @Override
    protected Tile create() {
        return this.generator.generateRegion(this.seed, this.regionX, this.regionZ);
    }
}

