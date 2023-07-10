/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.gen;

import com.terraforged.engine.concurrent.task.LazyCallable;
import com.terraforged.engine.tile.Tile;

public class CallableTile extends LazyCallable<Tile> {
    private final int regionX;
    private final int regionZ;
    private final TileGenerator generator;

    public CallableTile(int regionX, int regionZ, TileGenerator generator) {
    	this.regionX = regionX;
        this.regionZ = regionZ;
        this.generator = generator;
    }

    @Override
    protected Tile create() {
        return this.generator.generateRegion(this.regionX, this.regionZ);
    }
}

