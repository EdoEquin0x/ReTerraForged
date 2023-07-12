/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.filter;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.noise.util.NoiseUtil;

public interface Modifier {
    public float getValueModifier(float var1);

    default public float modify(Cell cell, float value) {
        float strengthModifier = 1.0f;
        float erosionModifier = cell.terrain.erosionModifier();
        if (erosionModifier != 1.0f) {
            float alpha = NoiseUtil.map(cell.terrainRegionEdge, 0.0f, 0.15f, 0.15f);
            strengthModifier = NoiseUtil.lerp(1.0f, erosionModifier, alpha);
        }
        if (cell.riverMask < 0.1f) {
            strengthModifier *= NoiseUtil.map(cell.riverMask, 0.002f, 0.1f, 0.098f);
        }
        return this.getValueModifier(cell.value) * strengthModifier * value;
    }

    default public Modifier invert() {
        return v -> 1.0f - this.getValueModifier(v);
    }

    public static Modifier range(final float minValue, final float maxValue) {
        return new Modifier(){
            private final float min;
            private final float max;
            private final float range;
            {
                this.min = minValue;
                this.max = maxValue;
                this.range = maxValue - minValue;
            }

            @Override
            public float getValueModifier(float value) {
                if (value > this.max) {
                    return 1.0f;
                }
                if (value < this.min) {
                    return 0.0f;
                }
                return (value - this.min) / this.range;
            }
        };
    }
}

