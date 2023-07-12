/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.continent;

public interface SimpleContinent extends Continent {
    float getEdgeValue(float x, float y);

    default float getDistanceToEdge(int cx, int cz, float dx, float dy) {
        return 1.0f;
    }

    default float getDistanceToOcean(int cx, int cz, float dx, float dy) {
        return 1.0f;
    }
}

