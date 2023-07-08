/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.filter;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.tile.Size;

public interface Filterable {
    public int getBlockX();

    public int getBlockZ();

    public Size getSize();

    public Cell[] getBacking();

    public Cell getCellRaw(int var1, int var2);
}

