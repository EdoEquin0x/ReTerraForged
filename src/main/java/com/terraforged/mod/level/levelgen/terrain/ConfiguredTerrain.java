/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain;

import net.minecraft.resources.ResourceLocation;

public class ConfiguredTerrain extends Terrain {
    private final float erosionModifier;
    private final boolean overridesRiver;

    ConfiguredTerrain(int id, ResourceLocation name, TerrainCategory category, float erosionModifier) {
        this(id, name, category, erosionModifier, category.overridesRiver());
    }

    ConfiguredTerrain(int id, ResourceLocation name, TerrainCategory category, boolean overridesRiver) {
        this(id, name, category, category.erosionModifier(), overridesRiver);
    }

    ConfiguredTerrain(int id, ResourceLocation name, TerrainCategory category, float erosionModifier, boolean overridesRiver) {
        super(id, name, category);
        this.erosionModifier = erosionModifier;
        this.overridesRiver = overridesRiver;
    }

    @Override
    public boolean overridesRiver() {
        return this.overridesRiver;
    }

    @Override
    public float erosionModifier() {
        return this.erosionModifier;
    }
}

