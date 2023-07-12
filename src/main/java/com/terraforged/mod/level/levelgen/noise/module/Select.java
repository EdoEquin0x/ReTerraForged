/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.noise.module;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.noise.Module;

public class Select {
    protected final Module control;

    public Select(Module control) {
        this.control = control;
    }

    public float getSelect(Cell cell, float x, float y) {
        return this.control.getValue(x, y);
    }
}

