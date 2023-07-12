/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.filter;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.generator.terrain.TerrainType;
import com.terraforged.mod.level.levelgen.heightmap.ControlPoints;

public class BeachDetect implements Filter, Filter.Visitor {
    private final ControlPoints transition;
    private final int radius = 8;
    private final int diameter = 17;

    public BeachDetect(GeneratorContext context) {
        this.transition = new ControlPoints(context.settings.world().controlPoints());
    }

    @Override
    public void apply(Filterable map, int seedX, int seedZ, int iterations) {
        this.iterate(map, this);
    }

    @Override
    public void visit(Filterable cellMap, Cell cell, int dx, int dz) {
        if (cell.terrain.isCoast() && cell.continentEdge < this.transition.beach) {
            Cell n = cellMap.getCellRaw(dx, dz - this.radius);
            Cell s = cellMap.getCellRaw(dx, dz + this.radius);
            Cell e = cellMap.getCellRaw(dx + this.radius, dz);
            Cell w = cellMap.getCellRaw(dx - this.radius, dz);
            float gx = this.grad(e, w, cell);
            float gz = this.grad(n, s, cell);
            float d2 = gx * gx + gz * gz;
            if (d2 < 0.275f) {
                cell.terrain = TerrainType.BEACH;
            }
        }
    }

    private float grad(Cell a, Cell b, Cell def) {
        int distance = this.diameter;
        if (a.isAbsent()) {
            a = def;
            distance -= this.radius;
        }
        if (b.isAbsent()) {
            b = def;
            distance -= this.radius;
        }
        return (a.value - b.value) / (float)distance;
    }
}

