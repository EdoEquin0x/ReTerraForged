/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.cell;

import com.terraforged.engine.concurrent.Resource;
import com.terraforged.noise.Module;

public interface Populator extends Module {
    public void apply(Cell var1, float var2, float var3);

    @Override
    default public float getValue(float x, float z) {
        try (Resource<Cell> cell = Cell.getResource();){
            this.apply(cell.get(), x, z);
            float f = cell.get().value;
            return f;
        }
    }
}

