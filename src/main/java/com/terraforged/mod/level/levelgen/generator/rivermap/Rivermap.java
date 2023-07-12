/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.rivermap;

import com.terraforged.mod.concurrent.cache.ExpiringEntry;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.generator.rivermap.gen.GenWarp;
import com.terraforged.mod.level.levelgen.generator.rivermap.river.Network;
import com.terraforged.mod.level.levelgen.heightmap.Heightmap;
import com.terraforged.mod.noise.domain.Domain;

public class Rivermap implements ExpiringEntry {
    private final int x;
    private final int z;
    private final Domain lakeWarp;
    private final Domain riverWarp;
    private final Network[] networks;
    private final long timestamp = System.currentTimeMillis();

    public Rivermap(int x, int z, Network[] networks, GenWarp warp) {
        this.x = x;
        this.z = z;
        this.networks = networks;
        this.lakeWarp = warp.lake;
        this.riverWarp = warp.river;
    }

    public void apply(Cell cell, float x, float z) {
        float rx = this.riverWarp.getX(x, z);
        float rz = this.riverWarp.getY(x, z);
        float lx = this.lakeWarp.getOffsetX(rx, rz);
        float lz = this.lakeWarp.getOffsetY(rx, rz);
        for (Network network : this.networks) {
            if (!network.contains(rx, rz)) continue;
            network.carve(cell, rx, rz, lx, lz);
        }
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public static Rivermap get(Cell cell, Rivermap instance, Heightmap heightmap) {
        return Rivermap.get(cell.continentX, cell.continentZ, instance, heightmap);
    }

    public static Rivermap get(int x, int z, Rivermap instance, Heightmap heightmap) {
        if (instance != null && x == instance.getX() && z == instance.getZ()) {
            return instance;
        }
        return heightmap.getContinent().getRivermap(x, z);
    }
}

