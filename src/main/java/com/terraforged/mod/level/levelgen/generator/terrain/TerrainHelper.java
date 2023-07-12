/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain;

public class TerrainHelper {
    public static Terrain getOrCreate(String name, Terrain parent) {
        if (parent == null || parent == TerrainType.NONE) {
            return TerrainType.NONE;
        }
        Terrain current = TerrainType.get(name);
        if (current != null) {
            return current;
        }
        return TerrainType.register(new Terrain(0, name, parent));
    }
}

