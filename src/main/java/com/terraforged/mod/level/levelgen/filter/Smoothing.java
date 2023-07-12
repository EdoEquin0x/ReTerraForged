/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.filter;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.heightmap.Levels;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.noise.util.NoiseUtil;

public class Smoothing implements Filter {
    private final int radius;
    private final float rad2;
    private final float strength;
    private final Modifier modifier;

    public Smoothing(Settings settings, Levels levels) {
        this.radius = NoiseUtil.round(settings.filters().smoothing().smoothingRadius() + 0.5f);
        this.rad2 = settings.filters().smoothing().smoothingRadius() * settings.filters().smoothing().smoothingRadius();
        this.strength = settings.filters().smoothing().smoothingRate();
        this.modifier = Modifier.range(levels.ground(1), levels.ground(120)).invert();
    }

    @Override
    public void apply(Filterable map, int seedX, int seedZ, int iterations) {
        while (iterations-- > 0) {
            this.apply(map);
        }
    }

    private void apply(Filterable cellMap) {
        int maxZ = cellMap.getSize().total - this.radius;
        int maxX = cellMap.getSize().total - this.radius;
        for (int z = this.radius; z < maxZ; ++z) {
            for (int x = this.radius; x < maxX; ++x) {
                Cell cell = cellMap.getCellRaw(x, z);
                if (cell.erosionMask) continue;
                float total = 0.0f;
                float weights = 0.0f;
                for (int dz = -this.radius; dz <= this.radius; ++dz) {
                    for (int dx = -this.radius; dx <= this.radius; ++dx) {
                        Cell neighbour;
                        float dist2 = dx * dx + dz * dz;
                        if (dist2 > this.rad2 || (neighbour = cellMap.getCellRaw(x + dx, z + dz)).isAbsent()) continue;
                        float value = neighbour.value;
                        float weight = 1.0f - dist2 / this.rad2;
                        total += value * weight;
                        weights += weight;
                    }
                }
                if (!(weights > 0.0f)) continue;
                float dif = cell.value - total / weights;
                cell.value -= this.modifier.modify(cell, dif * this.strength);
            }
        }
    }
}

