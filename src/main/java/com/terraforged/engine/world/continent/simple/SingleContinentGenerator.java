/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.continent.simple;

import com.terraforged.engine.Seed;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.noise.util.Vec2i;

public class SingleContinentGenerator extends ContinentGenerator {
    private final Vec2i center;

    public SingleContinentGenerator(Seed seed, GeneratorContext context) {
        super(seed, context);
        long center = this.getNearestCenter(seed.get(), 0.0f, 0.0f);
        int cx = PosUtil.unpackLeft(center);
        int cz = PosUtil.unpackRight(center);
        this.center = new Vec2i(cx, cz);
    }

    @Override
    public void apply(int seed, Cell cell, float x, float y) {
        super.apply(seed, cell, x, y);
        if (cell.continentX != this.center.x || cell.continentZ != this.center.y) {
            cell.continentId = 0.0f;
            cell.continentEdge = 0.0f;
            cell.continentX = 0;
            cell.continentZ = 0;
        }
    }
}

