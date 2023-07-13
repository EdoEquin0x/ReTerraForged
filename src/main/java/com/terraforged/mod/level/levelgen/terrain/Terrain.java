/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain;

import net.minecraft.resources.ResourceLocation;

public class Terrain implements ITerrain.Delegate {
    private final int id;
    private final ResourceLocation name;
    private final TerrainCategory type;
    private final ITerrain delegate;

    Terrain(int id, ResourceLocation name, Terrain terrain) {
        this(id, name, terrain.getCategory(), terrain);
    }

    Terrain(int id, ResourceLocation name, TerrainCategory type) {
        this(id, name, type, type);
    }

    Terrain(int id, ResourceLocation name, TerrainCategory type, ITerrain delegate) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.delegate = delegate;
    }

    public int getId() {
        return this.id;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public TerrainCategory getCategory() {
        return this.type;
    }

    @Override
    public ITerrain getDelegate() {
        return this.delegate;
    }

    @Override
    public String toString() {
        return this.getName().toString();
    }

    public Terrain withId(int id) {
        ITerrain delegate = this.delegate instanceof Terrain ? this.delegate : this;
        return new Terrain(id, this.name, this.type, delegate);
    }
}

