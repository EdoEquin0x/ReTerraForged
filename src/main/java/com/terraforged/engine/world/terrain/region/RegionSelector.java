/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.terrain.region;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.cell.Populator;
import com.terraforged.engine.world.terrain.populator.TerrainPopulator;
import com.terraforged.noise.util.NoiseUtil;
import java.util.LinkedList;
import java.util.List;

public class RegionSelector implements Populator {
    private final int maxIndex;
    private final Populator[] nodes;

    public RegionSelector(List<Populator> populators) {
        this.nodes = RegionSelector.getWeightedArray(populators);
        this.maxIndex = this.nodes.length - 1;
    }

    @Override
    public void apply(int seed, Cell cell, float x, float y) {
        this.get(cell.terrainRegionId).apply(seed, cell, x, y);
    }

    public Populator get(float identity) {
        int index = NoiseUtil.round(identity * (float)this.maxIndex);
        return this.nodes[index];
    }

    private static Populator[] getWeightedArray(List<Populator> modules) {
        float smallest = Float.MAX_VALUE;
        for (Populator p : modules) {
            if (p instanceof TerrainPopulator) {
                TerrainPopulator tp = (TerrainPopulator)p;
                if (tp.getWeight() == 0.0f) continue;
                smallest = Math.min(smallest, tp.getWeight());
                continue;
            }
            smallest = Math.min(smallest, 1.0f);
        }
        if (smallest == Float.MAX_VALUE) {
            return modules.toArray(new Populator[0]);
        }
        LinkedList<Populator> result = new LinkedList<Populator>();
        for (Populator p : modules) {
            int count;
            if (p instanceof TerrainPopulator) {
                TerrainPopulator tp = (TerrainPopulator)p;
                if (tp.getWeight() == 0.0f) continue;
                count = Math.round(tp.getWeight() / smallest);
            } else {
                count = Math.round(1.0f / smallest);
            }
            while (count-- > 0) {
                result.add(p);
            }
        }
        if (result.isEmpty()) {
            return modules.toArray(new Populator[0]);
        }
        return result.toArray(new Populator[0]);
    }
}

