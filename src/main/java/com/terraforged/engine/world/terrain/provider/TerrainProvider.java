/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.terrain.provider;

import java.util.List;
import java.util.function.Consumer;

import com.terraforged.engine.cell.Populator;
import com.terraforged.engine.settings.TerrainSettings;
import com.terraforged.engine.world.terrain.LandForms;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.populator.TerrainPopulator;
import com.terraforged.noise.Module;

public interface TerrainProvider {
    public LandForms getLandforms();

    public List<Populator> getPopulators();

    public int getVariantCount(Terrain var1);

    default public void forEach(Consumer<TerrainPopulator> consumer) {
    }

    default public Terrain getTerrain(String name) {
        return null;
    }

    public void registerMixable(TerrainPopulator var1);

    public void registerUnMixable(TerrainPopulator var1);

    default public void registerMixable(Terrain type, Module base, Module variance, TerrainSettings.Terrain settings) {
        this.registerMixable(TerrainPopulator.of(type, base, variance, settings));
    }

    default public void registerUnMixable(Terrain type, Module base, Module variance, TerrainSettings.Terrain settings) {
        this.registerUnMixable(TerrainPopulator.of(type, base, variance, settings));
    }
}

