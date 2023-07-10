/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.terrain.provider;

import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.RegionConfig;

public interface TerrainProviderFactory {
    public TerrainProvider create(GeneratorContext var1, RegionConfig var2);
}

