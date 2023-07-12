/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.tile.api;

import com.terraforged.mod.concurrent.Disposable;
import com.terraforged.mod.concurrent.task.LazyCallable;
import com.terraforged.mod.tile.Tile;

public interface TileFactory {
    public int chunkToRegion(int var1);

    public void setListener(Disposable.Listener<Tile> var1);

    public LazyCallable<Tile> getTile(int var1, int var2);

    public LazyCallable<Tile> getTile(float var1, float var2, float var3, boolean var4);

    public TileFactory async();

    public TileProvider cached();
}