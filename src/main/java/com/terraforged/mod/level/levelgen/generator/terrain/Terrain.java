/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain;

import com.terraforged.mod.noise.util.NoiseUtil;

public class Terrain implements ITerrain.Delegate {
    private final int id;
    private final String name;
    private final TerrainCategory type;
    private final ITerrain delegate;

    Terrain(int id, String name, Terrain terrain) {
        this(id, name, terrain.getCategory(), terrain);
    }

    Terrain(int id, String name, TerrainCategory type) {
        this(id, name, type, type);
    }

    Terrain(int id, String name, TerrainCategory type, ITerrain delegate) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.delegate = delegate;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public TerrainCategory getCategory() {
        return this.type;
    }

    public float getRenderHue() {
        return NoiseUtil.valCoord2D(this.name.hashCode(), 0, 0);
    }

    @Override
    public ITerrain getDelegate() {
        return this.delegate;
    }

    public String toString() {
        return this.getName();
    }

    public Terrain withId(int id) {
        ITerrain delegate = this.delegate instanceof Terrain ? this.delegate : this;
        return new Terrain(id, this.name, this.type, delegate);
    }
}

