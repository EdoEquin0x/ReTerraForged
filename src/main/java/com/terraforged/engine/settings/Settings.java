/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.settings;

import com.terraforged.engine.serialization.annotation.Serializable;

@Serializable
public class Settings {
    public WorldSettings world = new WorldSettings();
    public ClimateSettings climate = new ClimateSettings();
    public TerrainSettings terrain = new TerrainSettings();
    public RiverSettings rivers = new RiverSettings();
    public FilterSettings filters = new FilterSettings();
}

