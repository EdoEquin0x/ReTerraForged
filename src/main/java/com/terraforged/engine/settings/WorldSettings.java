/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.settings;

import com.terraforged.engine.serialization.annotation.Comment;
import com.terraforged.engine.serialization.annotation.Limit;
import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Restricted;
import com.terraforged.engine.serialization.annotation.Serializable;
import com.terraforged.engine.serialization.annotation.legacy.LegacyFloat;
import com.terraforged.engine.world.continent.ContinentType;
import com.terraforged.engine.world.continent.SpawnType;
import com.terraforged.noise.func.DistanceFunc;

@Serializable
public class WorldSettings {
    public static final int DEFAULT_CONTINENT_SCALE = 3000;
    public transient long seed = 0L;
    public Continent continent = new Continent();
    public ControlPoints controlPoints = new ControlPoints();
    public Properties properties = new Properties();

    @Serializable
    public static class Properties {
        @Comment(value={"Set whether spawn should be close to x=0,z=0 or the centre of the nearest continent"})
        public SpawnType spawnType = SpawnType.CONTINENT_CENTER;
        @Range(min=0.0f, max=256.0f)
        @Comment(value={"Controls the world height"})
        public int worldHeight = 256;
        @Range(min=0.0f, max=255.0f)
        @Comment(value={"Controls the sea level"})
        public int seaLevel = 63;
    }

    @Serializable
    public static class ControlPoints {
        @Range(min=0.0f, max=1.0f)
        @Limit(upper="shallowOcean")
        @Comment(value={"Controls the point above which deep oceans transition into shallow oceans.", "The greater the gap to the shallow ocean slider, the more gradual the transition."})
        public float deepOcean = 0.1f;
        @Range(min=0.0f, max=1.0f)
        @Limit(lower="deepOcean", upper="beach")
        @Comment(value={"Controls the point above which shallow oceans transition into coastal terrain.", "The greater the gap to the coast slider, the more gradual the transition."})
        public float shallowOcean = 0.25f;
        @Range(min=0.0f, max=1.0f)
        @Limit(lower="shallowOcean", upper="coast")
        @Comment(value={"Controls how much of the coastal terrain is assigned to beach biomes."})
        public float beach = 0.327f;
        @Range(min=0.0f, max=1.0f)
        @Limit(lower="beach", upper="inland")
        @Comment(value={"Controls the size of coastal regions and is also the point below", "which inland terrain transitions into oceans. Certain biomes such", "as Mushroom Fields only generate in coastal areas."})
        public float coast = 0.448f;
        @Range(min=0.0f, max=1.0f)
        @Limit(lower="coast")
        @Comment(value={"Controls the overall transition from ocean to inland terrain."})
        public float inland = 0.502f;
    }

    @Serializable
    public static class Continent {
        @Comment(value={"Controls the continent generator type"})
        public ContinentType continentType = ContinentType.MULTI_IMPROVED;
        @Restricted(name="continentType", value={"MULTI", "SINGLE"})
        @Comment(value={"Controls how continent shapes are calculated.", "You may also need to adjust the transition points to ensure beaches etc still form."})
        public DistanceFunc continentShape = DistanceFunc.EUCLIDEAN;
        @Range(min=100.0f, max=10000.0f)
        @Comment(value={"Controls the size of continents.", "You may also need to adjust the transition points to ensure beaches etc still form."})
        public int continentScale = 3000;
        @LegacyFloat(value=0.7f)
        @Range(min=0.5f, max=1.0f)
        @Comment(value={"Controls how much continent centers are offset from the underlying noise grid."})
        public float continentJitter = 0.7f;
        @Range(min=0.0f, max=1.0f)
        @Restricted(name="continentType", value={"MULTI_IMPROVED"})
        @Comment(value={"Reduces the number of continents to create more vast oceans."})
        public float continentSkipping = 0.25f;
        @Range(min=0.0f, max=0.75f)
        @Restricted(name="continentType", value={"MULTI_IMPROVED"})
        @Comment(value={"Increases the variance of continent sizes."})
        public float continentSizeVariance = 0.25f;
        @Range(min=1.0f, max=5.0f)
        @Restricted(name="continentType", value={"MULTI_IMPROVED"})
        @Comment(value={"The number of octaves of noise used to distort the continent."})
        public int continentNoiseOctaves = 5;
        @Range(min=0.0f, max=0.5f)
        @Restricted(name="continentType", value={"MULTI_IMPROVED"})
        @Comment(value={"The contribution strength of each noise octave."})
        public float continentNoiseGain = 0.26f;
        @Range(min=1.0f, max=10.0f)
        @Restricted(name="continentType", value={"MULTI_IMPROVED"})
        @Comment(value={"The frequency multiplier for each noise octave."})
        public float continentNoiseLacunarity = 4.33f;
    }
}

