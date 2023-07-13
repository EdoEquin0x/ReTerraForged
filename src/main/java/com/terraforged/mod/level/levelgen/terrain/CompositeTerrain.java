/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain;

import com.terraforged.mod.TerraForged;

import net.minecraft.resources.ResourceLocation;

public class CompositeTerrain extends Terrain {
    private final float erosion;

    CompositeTerrain(int id, Terrain a, Terrain b) {
        super(id, new ResourceLocation(TerraForged.MODID, a.toString() + "-" + b.toString()), CompositeTerrain.getDominant(a, b));
        this.erosion = Math.min(a.erosionModifier(), b.erosionModifier());
    }

    @Override
    public float erosionModifier() {
        return this.erosion;
    }

    private static Terrain getDominant(Terrain a, Terrain b) {
        TerrainCategory typeA = a.getCategory();
        TerrainCategory typeB = a.getCategory();
        TerrainCategory dom = typeA.getDominant(typeB);
        if (dom == typeA) {
            return a;
        }
        return b;
    }
}

