/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.rivermap;

import java.util.concurrent.TimeUnit;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.cache.Cache;
import com.terraforged.engine.concurrent.cache.map.StampedLongMap;
import com.terraforged.engine.util.pos.PosUtil;

public class RiverCache {
    protected final RiverGenerator generator;
    protected final Cache<Rivermap> cache = new Cache<>("RiverCache", 32, 5L, 1L, TimeUnit.MINUTES, StampedLongMap::new);

    public RiverCache(RiverGenerator generator) {
        this.generator = generator;
    }

    public Rivermap getRivers(Cell cell) {
        return this.getRivers(cell.continentX, cell.continentZ);
    }

    public Rivermap getRivers(int x, int z) {
        return this.cache.computeIfAbsent(PosUtil.pack(x, z), id -> this.generator.generateRivers(PosUtil.unpackLeft(id), PosUtil.unpackRight(id), id));
    }
}

