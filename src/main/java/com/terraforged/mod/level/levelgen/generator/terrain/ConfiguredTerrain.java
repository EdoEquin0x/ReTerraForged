/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain;

public class ConfiguredTerrain extends Terrain {
    private final float erosionModifier;
    private final boolean isMountain;
    private final boolean overridesRiver;

    ConfiguredTerrain(int id, String name, TerrainCategory category, float erosionModifier) {
        this(id, name, category, erosionModifier, category.isMountain(), category.overridesRiver());
    }

    ConfiguredTerrain(int id, String name, TerrainCategory category, boolean overridesRiver) {
        this(id, name, category, category.erosionModifier(), category.isMountain(), overridesRiver);
    }

    ConfiguredTerrain(int id, String name, TerrainCategory category, boolean isMountain, boolean overridesRiver) {
        this(id, name, category, category.erosionModifier(), isMountain, overridesRiver);
    }

    ConfiguredTerrain(int id, String name, TerrainCategory category, float erosionModifier, boolean isMountain, boolean overridesRiver) {
        super(id, name, category);
        this.erosionModifier = erosionModifier;
        this.isMountain = isMountain;
        this.overridesRiver = overridesRiver;
    }

    @Override
    public boolean overridesRiver() {
        return this.overridesRiver;
    }

    @Override
    public boolean isMountain() {
        return this.isMountain;
    }

    @Override
    public float erosionModifier() {
        return this.erosionModifier;
    }
}

