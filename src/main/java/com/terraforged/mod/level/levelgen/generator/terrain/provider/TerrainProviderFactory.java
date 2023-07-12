/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain.provider;

import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.heightmap.RegionConfig;

public interface TerrainProviderFactory {
    public TerrainProvider create(GeneratorContext var1, RegionConfig var2);
}

