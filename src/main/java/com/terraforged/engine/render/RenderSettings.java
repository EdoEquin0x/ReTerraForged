/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.render;

import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.Levels;

public class RenderSettings {
    public int width;
    public int height;
    public int resolution;
    public float zoom = 1.0f;
    public final Levels levels;
    public RenderMode renderMode = RenderMode.BIOME_TYPE;

    public RenderSettings(GeneratorContext context) {
        this.levels = context.levels;
    }
}

