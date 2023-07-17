/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent.simple;

import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.settings.Levels;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.util.Seed;
import com.terraforged.mod.noise.util.Vec2i;
import com.terraforged.mod.util.pos.PosUtil;

public class SingleContinentGenerator extends ContinentGenerator {
    private final Vec2i center;

    public SingleContinentGenerator(Seed seed, Levels levels, Settings settings) {
        super(seed, levels, settings);
        long center = this.getNearestCenter(0.0f, 0.0f);
        int cx = PosUtil.unpackLeft(center);
        int cz = PosUtil.unpackRight(center);
        this.center = new Vec2i(cx, cz);
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        super.apply(cell, x, y);
        if (cell.continentX != this.center.x || cell.continentZ != this.center.y) {
            cell.continentId = 0.0f;
            cell.continentEdge = 0.0f;
            cell.continentX = 0;
            cell.continentZ = 0;
        }
    }
}

