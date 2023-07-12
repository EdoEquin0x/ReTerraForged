/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.tile.chunk;

import com.terraforged.mod.concurrent.Disposable;
import com.terraforged.mod.concurrent.cache.SafeCloseable;
import com.terraforged.mod.level.levelgen.cell.Cell;

public interface ChunkReader extends ChunkHolder, SafeCloseable, Disposable {
    public Cell getCell(int var1, int var2);

    default public void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor visitor) {
        int regionMinX = this.getBlockX();
        int regionMinZ = this.getBlockZ();
        if (maxX < regionMinX || maxZ < regionMinZ) {
            return;
        }
        int regionMaxX = this.getBlockX() + 15;
        int regionMaxZ = this.getBlockZ() + 15;
        if (minX > regionMaxX || maxZ > regionMaxZ) {
            return;
        }
        minX = Math.max(minX, regionMinX);
        minZ = Math.max(minZ, regionMinZ);
        maxX = Math.min(maxX, regionMaxX);
        maxZ = Math.min(maxZ, regionMaxZ);
        for (int z = minZ; z <= maxX; ++z) {
            for (int x = minX; x <= maxZ; ++x) {
                visitor.visit(this.getCell(x, z), x, z);
            }
        }
    }

    default public void iterate(Cell.Visitor visitor) {
        for (int dz = 0; dz < 16; ++dz) {
            for (int dx = 0; dx < 16; ++dx) {
                visitor.visit(this.getCell(dx, dz), dx, dz);
            }
        }
    }

    default public <C> void iterate(C context, Cell.ContextVisitor<C> visitor) {
        for (int dz = 0; dz < 16; ++dz) {
            for (int dx = 0; dx < 16; ++dx) {
                visitor.visit(this.getCell(dx, dz), dx, dz, context);
            }
        }
    }

    @Override
    default public void close() {
    }
}

