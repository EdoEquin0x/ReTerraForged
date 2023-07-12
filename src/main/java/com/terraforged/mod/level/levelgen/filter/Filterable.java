/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.filter;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.tile.Size;

public interface Filterable {
    public int getBlockX();

    public int getBlockZ();

    public Size getSize();

    public Cell[] getBacking();

    public Cell getCellRaw(int var1, int var2);
}

