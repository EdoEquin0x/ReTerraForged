/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain.provider;

import java.util.function.Consumer;

import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.settings.TerrainSettings;
import com.terraforged.mod.level.levelgen.terrain.LandForms;
import com.terraforged.mod.level.levelgen.terrain.Terrain;
import com.terraforged.mod.level.levelgen.terrain.populator.TerrainPopulator;
import com.terraforged.mod.noise.Module;

import net.minecraft.resources.ResourceLocation;

public interface TerrainProvider {
    public LandForms getLandforms();

    public Populator[] getPopulators();

    public int getVariantCount(Terrain var1);

    default public void forEach(Consumer<TerrainPopulator> consumer) {
    }

    default public Terrain getTerrain(ResourceLocation name) {
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

