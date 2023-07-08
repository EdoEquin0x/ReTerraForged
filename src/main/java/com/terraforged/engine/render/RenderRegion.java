/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.render;

import com.terraforged.engine.tile.Tile;

public class RenderRegion {
    private final Tile tile;
    private final Object lock = new Object();
    private RenderBuffer mesh;

    public RenderRegion(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return this.tile;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public RenderBuffer getMesh() {
        Object object = this.lock;
        synchronized (object) {
            return this.mesh;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setMesh(RenderBuffer mesh) {
        Object object = this.lock;
        synchronized (object) {
            this.mesh = mesh;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clear() {
        Object object = this.lock;
        synchronized (object) {
            if (this.mesh != null) {
                this.mesh.dispose();
                this.mesh = null;
            }
        }
    }
}

