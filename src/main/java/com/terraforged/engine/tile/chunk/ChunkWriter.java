/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.chunk;

import com.terraforged.engine.cell.Cell;

public interface ChunkWriter extends ChunkHolder {
    public Cell genCell(int var1, int var2);

    default public void generate(Cell.Visitor visitor) {
        for (int dz = 0; dz < 16; ++dz) {
            for (int dx = 0; dx < 16; ++dx) {
                visitor.visit(this.genCell(dx, dz), dx, dz);
            }
        }
    }

    default public <T> void generate(T ctx, Visitor<T> visitor) {
        int blockX = this.getBlockX();
        int blockZ = this.getBlockZ();
        for (int dz = 0; dz < 16; ++dz) {
            for (int dx = 0; dx < 16; ++dx) {
                visitor.visit(this.genCell(dx, dz), dx, dz, blockX + dx, blockZ + dz, ctx);
            }
        }
    }

    public static interface Visitor<T> {
        public void visit(Cell var1, int var2, int var3, int var4, int var5, T var6);
    }
}

