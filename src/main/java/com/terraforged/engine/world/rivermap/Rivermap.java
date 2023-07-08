/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.rivermap;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.cache.ExpiringEntry;
import com.terraforged.engine.world.heightmap.Heightmap;
import com.terraforged.engine.world.rivermap.gen.GenWarp;
import com.terraforged.engine.world.rivermap.river.Network;
import com.terraforged.noise.domain.Domain;

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

    public void apply(int seed, Cell cell, float x, float z) {
        float rx = this.riverWarp.getX(seed, x, z);
        float rz = this.riverWarp.getY(seed, x, z);
        float lx = this.lakeWarp.getOffsetX(seed, rx, rz);
        float lz = this.lakeWarp.getOffsetY(seed, rx, rz);
        for (Network network : this.networks) {
            if (!network.contains(rx, rz)) continue;
            network.carve(seed, cell, rx, rz, lx, lz);
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

    public static Rivermap get(int seed, Cell cell, Rivermap instance, Heightmap heightmap) {
        return Rivermap.get(seed, cell.continentX, cell.continentZ, instance, heightmap);
    }

    public static Rivermap get(int seed, int x, int z, Rivermap instance, Heightmap heightmap) {
        if (instance != null && x == instance.getX() && z == instance.getZ()) {
            return instance;
        }
        return heightmap.getContinent().getRivermap(seed, x, z);
    }
}

