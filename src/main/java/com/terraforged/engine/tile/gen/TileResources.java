/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.gen;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.pool.ArrayPool;
import com.terraforged.engine.tile.Tile;

public class TileResources {
    public final ArrayPool<Cell> blocks = ArrayPool.of(100, Cell[]::new);
    public final ArrayPool<Tile.GenChunk> chunks = ArrayPool.of(100, Tile.GenChunk[]::new);
}

