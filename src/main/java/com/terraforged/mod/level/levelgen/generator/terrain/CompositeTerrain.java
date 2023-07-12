/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain;

public class CompositeTerrain extends Terrain {
    private final boolean flat;
    private final float erosion;

    CompositeTerrain(int id, Terrain a, Terrain b) {
        super(id, a.getName() + "-" + b.getName(), CompositeTerrain.getDominant(a, b));
        this.flat = a.isFlat() && b.isFlat();
        this.erosion = Math.min(a.erosionModifier(), b.erosionModifier());
    }

    @Override
    public float erosionModifier() {
        return this.erosion;
    }

    @Override
    public boolean isFlat() {
        return this.flat;
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

