/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.continent;

public interface SimpleContinent extends Continent {
    @Override
    public float getEdgeValue(float var1, float var2);

    default public float getDistanceToEdge(int cx, int cz, float dx, float dy) {
        return 1.0f;
    }

    default public float getDistanceToOcean(int cx, int cz, float dx, float dy) {
        return 1.0f;
    }
}

