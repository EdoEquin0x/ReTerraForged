/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain.region;

import java.util.LinkedList;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.generator.terrain.populator.TerrainPopulator;
import com.terraforged.mod.noise.util.NoiseUtil;

public class RegionSelector implements Populator {
	public static final Codec<RegionSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		TFCodecs.forArray(Populator.CODEC, Populator[]::new).fieldOf("populators").forGetter((p) -> p.populators)
	).apply(instance, RegionSelector::new));
		
	private final Populator[] populators;
    private final int maxIndex;
    private final Populator[] nodes;

    public RegionSelector(Populator[] populators) {
    	this.populators = populators;
    	this.nodes = RegionSelector.getWeightedArray(populators);
        this.maxIndex = this.nodes.length - 1;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        this.get(cell.terrainRegionId).apply(cell, x, y);
    }

    public Populator get(float identity) {
        int index = NoiseUtil.round(identity * (float)this.maxIndex);
        return this.nodes[index];
    }

    private static Populator[] getWeightedArray(Populator[] modules) {
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
            return new Populator[0];
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
            return new Populator[0];
        }
        return result.toArray(new Populator[0]);
    }

	@Override
	public Codec<RegionSelector> codec() {
		return CODEC;
	}
}

