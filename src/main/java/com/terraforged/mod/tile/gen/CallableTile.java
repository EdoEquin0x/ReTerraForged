/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.tile.gen;

import com.terraforged.mod.concurrent.task.LazyCallable;
import com.terraforged.mod.tile.Tile;

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

