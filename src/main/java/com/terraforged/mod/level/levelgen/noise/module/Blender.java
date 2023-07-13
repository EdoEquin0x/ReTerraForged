/*

 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.noise.module;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
//import com.terraforged.mod.level.levelgen.terrain.Terrain;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.func.Interpolation;
import com.terraforged.mod.noise.util.NoiseUtil;

public class Blender extends Select implements Populator {
    private final Populator lower;
    private final Populator upper;
    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;

    public Blender(Module control, Populator lower, Populator upper, float min, float max) {
        super(control);
        this.lower = lower;
        this.upper = upper;
        this.blendLower = min;
        this.blendUpper = max;
        this.blendRange = this.blendUpper - this.blendLower;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        float select = this.getSelect(cell, x, y);
        if (select < this.blendLower) {
            this.lower.apply(cell, x, y);
            return;
        }
        if (select > this.blendUpper) {
            this.upper.apply(cell, x, y);
            return;
        }
        float alpha = Interpolation.LINEAR.apply((select - this.blendLower) / this.blendRange);
        this.lower.apply(cell, x, y);
        float lowerVal = cell.value;
        this.upper.apply(cell, x, y);
        float upperVal = cell.value;
        cell.value = NoiseUtil.lerp(lowerVal, upperVal, alpha);
    }
}

