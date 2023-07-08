/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.settings;

import com.terraforged.engine.serialization.annotation.Comment;
import com.terraforged.engine.serialization.annotation.Rand;
import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Serializable;
import com.terraforged.engine.world.terrain.populator.TerrainPopulator;
import com.terraforged.noise.Module;

@Serializable
public class TerrainSettings {
    public General general = new General();
    public Terrain steppe = new Terrain(1.0f, 1.0f, 1.0f);
    public Terrain plains = new Terrain(2.0f, 1.0f, 1.0f);
    public Terrain hills = new Terrain(2.0f, 1.0f, 1.0f);
    public Terrain dales = new Terrain(1.5f, 1.0f, 1.0f);
    public Terrain plateau = new Terrain(1.5f, 1.0f, 1.0f);
    public Terrain badlands = new Terrain(1.0f, 1.0f, 1.0f);
    public Terrain torridonian = new Terrain(2.0f, 1.0f, 1.0f);
    public Terrain mountains = new Terrain(2.5f, 1.0f, 1.0f);
    public Terrain volcano = new Terrain(5.0f, 1.0f, 1.0f);

    @Serializable
    public static class Terrain {
        @Range(min=0.0f, max=10.0f)
        @Comment(value={"Controls how common this terrain type is"})
        public float weight = 1.0f;
        @Range(min=0.0f, max=2.0f)
        @Comment(value={"Controls the base height of this terrain"})
        public float baseScale = 1.0f;
        @Range(min=0.0f, max=10.0f)
        @Comment(value={"Stretches or compresses the terrain vertically"})
        public float verticalScale = 1.0f;
        @Range(min=0.0f, max=10.0f)
        @Comment(value={"Stretches or compresses the terrain horizontally"})
        public float horizontalScale = 1.0f;

        public Terrain() {
        }

        public Terrain(float weight, float vertical, float horizontal) {
            this.weight = weight;
            this.verticalScale = vertical;
            this.horizontalScale = horizontal;
        }

        public Module apply(double bias, double scale, Module module) {
            double moduleBias = bias * (double)this.baseScale;
            double moduleScale = scale * (double)this.verticalScale;
            Module outputModule = module.scale(moduleScale).bias(moduleBias);
            return TerrainPopulator.clamp(outputModule);
        }
    }

    @Serializable
    public static class General {
        @Rand
        @Comment(value={"A seed offset used to randomise terrain distribution"})
        public int terrainSeedOffset = 0;
        @Range(min=125.0f, max=5000.0f)
        @Comment(value={"Controls the size of terrain regions"})
        public int terrainRegionSize = 1200;
        @Range(min=0.01f, max=1.0f)
        @Comment(value={"Globally controls the vertical scaling of terrain"})
        public float globalVerticalScale = 0.98f;
        @Range(min=0.01f, max=5.0f)
        @Comment(value={"Globally controls the horizontal scaling of terrain"})
        public float globalHorizontalScale = 1.0f;
        @Comment(value={"Carries out extra processing on mountains to make them look even nicer.", "Can be disabled to improve performance slightly."})
        public boolean fancyMountains = true;
    }
}

