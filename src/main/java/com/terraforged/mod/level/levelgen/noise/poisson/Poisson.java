/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.noise.poisson;

import java.util.Arrays;

import com.terraforged.mod.concurrent.Resource;
import com.terraforged.mod.concurrent.pool.ObjectPool;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.noise.util.Vec2f;

public class Poisson {
    private static final int SAMPLES = 50;
    private final int radius;
    private final int radius2;
    private final float halfRadius;
    private final int maxDistance;
    private final int regionSize;
    private final int gridSize;
    private final float cellSize;
    private final ObjectPool<Vec2f[][]> pool;

    public Poisson(int radius) {
        int size = 48;
        this.radius = radius;
        this.radius2 = radius * radius;
        this.halfRadius = (float)radius / 2.0f;
        this.maxDistance = radius * 2;
        this.regionSize = size - radius;
        this.cellSize = (float)radius / NoiseUtil.SQRT2;
        this.gridSize = (int)Math.ceil((float)this.regionSize / this.cellSize);
        this.pool = new ObjectPool<Vec2f[][]>(3, () -> new Vec2f[this.gridSize][this.gridSize]);
    }

    public int getRadius() {
        return this.radius;
    }

    public Visitor visit(int chunkX, int chunkZ, PoissonContext context, Visitor visitor) {
        try (Resource<Vec2f[][]> grid = this.pool.get();){
            Poisson.clear(grid.get());
            context.startX = chunkX << 4;
            context.startZ = chunkZ << 4;
            context.endX = context.startX + 16;
            context.endZ = context.startZ + 16;
            int regionX = context.startX >> 5;
            int regionZ = context.startZ >> 5;
            context.offsetX = regionX << 5;
            context.offsetZ = regionZ << 5;
            context.random.setSeed(NoiseUtil.hash2D(context.seed, regionX, regionZ));
            int x = context.random.nextInt(this.regionSize);
            int z = context.random.nextInt(this.regionSize);
            this.visit(x, z, grid.get(), SAMPLES, context, visitor);
            Visitor visitor2 = visitor;
            return visitor2;
        }
    }

    private void visit(float px, float pz, Vec2f[][] grid, int samples, PoissonContext context, Visitor visitor) {
        for (int i = 0; i < samples; ++i) {
            float angle = context.random.nextFloat() * ((float)Math.PI * 2);
            float distance = (float)this.radius + context.random.nextFloat() * (float)this.maxDistance;
            float x = this.halfRadius + px + NoiseUtil.sin(angle) * distance;
            float z = this.halfRadius + pz + NoiseUtil.cos(angle) * distance;
            if (!this.valid(x, z, grid, context)) continue;
            Vec2f vec = new Vec2f(x, z);
            this.visit(vec, context, visitor);
            int cellX = (int)(x / this.cellSize);
            int cellZ = (int)(z / this.cellSize);
            grid[cellZ][cellX] = vec;
            this.visit(vec.x, vec.y, grid, samples, context, visitor);
        }
    }

    private void visit(Vec2f pos, PoissonContext context, Visitor visitor) {
        int x = context.offsetX + (int)pos.x;
        int z = context.offsetZ + (int)pos.y;
        if (x >= context.startX && x < context.endX && z >= context.startZ && z < context.endZ) {
            visitor.visit(x, z);
        }
    }

    private boolean valid(float x, float z, Vec2f[][] grid, PoissonContext context) {
        if (x < 0.0f || x >= (float)this.regionSize || z < 0.0f || z >= (float)this.regionSize) {
            return false;
        }
        int cellZ = (int)(z / this.cellSize);
        int cellX = (int)(x / this.cellSize);
        if (grid[cellZ][cellX] != null) {
            return false;
        }
        float noise = context.density.getValue((float)context.offsetX + x, (float)context.offsetZ + z);
        float radius2 = noise * (float)this.radius2;
        int searchRadius = 2;
        int minX = Math.max(0, cellX - searchRadius);
        int maxX = Math.min(grid[0].length - 1, cellX + searchRadius);
        int minZ = Math.max(0, cellZ - searchRadius);
        int maxZ = Math.min(grid.length - 1, cellZ + searchRadius);
        for (int dz = minZ; dz <= maxZ; ++dz) {
            for (int dx = minX; dx <= maxX; ++dx) {
                Vec2f vec = grid[dz][dx];
                if (vec == null || !((vec.dist2(x, z)) < radius2)) continue;
                return false;
            }
        }
        return true;
    }

    private static void clear(Vec2f[][] grid) {
        for (Object[] arrobject : grid) {
            Arrays.fill(arrobject, null);
        }
    }

    public static interface Visitor {
        public void visit(int var1, int var2);
    }
}

