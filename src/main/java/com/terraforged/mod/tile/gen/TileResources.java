/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.tile.gen;

import com.terraforged.mod.concurrent.pool.ArrayPool;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.tile.Tile;

public class TileResources {
    public final ArrayPool<Cell> blocks = ArrayPool.of(100, Cell[]::new);
    public final ArrayPool<Tile.GenChunk> chunks = ArrayPool.of(100, Tile.GenChunk[]::new);
}

