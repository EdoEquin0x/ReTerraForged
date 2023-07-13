/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.heightmap;

import com.terraforged.mod.concurrent.cache.map.LoadBalanceLongMap;
import com.terraforged.mod.concurrent.cache.map.LongMap;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.rivermap.Rivermap;
import com.terraforged.mod.util.pos.PosUtil;

public class HeightmapCache {
    public static final int CACHE_SIZE = 4096;
    private final float waterLevel;
    private final float beachLevel;
    private final Heightmap heightmap;
    private final LongMap<Cell> cache;
    private final ThreadLocal<CachedContext> contextLocal = ThreadLocal.withInitial(() -> new CachedContext());

    public HeightmapCache(Heightmap heightmap) {
        this(heightmap, 4096);
    }

    public HeightmapCache(Heightmap heightmap, int size) {
        this.heightmap = heightmap;
        this.waterLevel = heightmap.getLevels().water;
        this.beachLevel = heightmap.getLevels().water(5);
        this.cache = new LoadBalanceLongMap<Cell>(Runtime.getRuntime().availableProcessors(), size);
    }

    public Cell get(int x, int z) {
        long index = PosUtil.pack(x, z);
        return this.cache.computeIfAbsent(index, this::compute);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Rivermap generate(Cell cell, int x, int z, Rivermap rivermap) {
        CachedContext context = this.contextLocal.get();
        try {
            context.cell = cell;
            context.rivermap = rivermap;
            long index = PosUtil.pack(x, z);
            Cell value = this.cache.computeIfAbsent(index, (i) -> this.contextCompute(i));
            if (value != cell) {
                cell.copyFrom(value);
            }
            Rivermap rivermap2 = context.rivermap;
            return rivermap2;
        }
        finally {
            context.rivermap = null;
        }
    }

    private Cell compute(long index) {
        int x = PosUtil.unpackLeft(index);
        int z = PosUtil.unpackRight(index);
        Cell cell = new Cell();
        this.heightmap.apply(cell, x, z);
//        if (cell.terrain == TerrainType.COAST && cell.value > this.waterLevel && cell.value <= this.beachLevel) {
//            cell.terrain = TerrainType.BEACH;
//        }
        return cell;
    }

    private Cell contextCompute(long index) {
        CachedContext context = this.contextLocal.get();
        int x = PosUtil.unpackLeft(index);
        int z = PosUtil.unpackRight(index);
        this.heightmap.applyBase(context.cell, x, z);
        context.rivermap = Rivermap.get(context.cell, context.rivermap, this.heightmap);
        this.heightmap.applyRivers(context.cell, x, z, context.rivermap);
        this.heightmap.applyClimate(context.cell, x, z);
        return context.cell;
    }

    private static class CachedContext {
        public Cell cell;
        public Rivermap rivermap;
    }
}

