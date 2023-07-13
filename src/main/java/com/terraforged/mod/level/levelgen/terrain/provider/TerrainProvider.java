/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain.provider;

import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.terrain.LandForms;
import com.terraforged.mod.level.levelgen.terrain.populator.TerrainPopulator;

public interface TerrainProvider {
    public LandForms getLandforms();

    public Populator[] getPopulators();

    public void registerMixable(TerrainPopulator var1);

    public void registerUnMixable(TerrainPopulator var1);
}

