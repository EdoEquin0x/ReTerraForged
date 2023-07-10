/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.gen;

import com.terraforged.engine.concurrent.task.LazyCallable;
import com.terraforged.engine.tile.Tile;

public class CallableZoomTile extends LazyCallable<Tile> {
    private final float centerX;
    private final float centerY;
    private final float zoom;
    private final boolean filters;
    private final TileGenerator generator;

    public CallableZoomTile(float centerX, float centerY, float zoom, boolean filters, TileGenerator generator) {
    	this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
        this.filters = filters;
        this.generator = generator;
    }

    @Override
    protected Tile create() {
        return this.generator.generateRegion(this.centerX, this.centerY, this.zoom, this.filters);
    }
}

