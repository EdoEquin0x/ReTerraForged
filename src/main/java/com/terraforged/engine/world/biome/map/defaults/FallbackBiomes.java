/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.map.defaults;

public class FallbackBiomes<T> {
    public final T river;
    public final T lake;
    public final T beach;
    public final T ocean;
    public final T deepOcean;
    public final T wetland;
    public final T land;

    public FallbackBiomes(T river, T lake, T beach, T ocean, T deepOcean, T wetland, T land) {
        this.river = river;
        this.lake = lake;
        this.beach = beach;
        this.ocean = ocean;
        this.deepOcean = deepOcean;
        this.wetland = wetland;
        this.land = land;
    }
}

