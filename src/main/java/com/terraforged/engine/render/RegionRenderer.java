/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.render;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.tile.Tile;

public class RegionRenderer {
    public static final float RENDER_SCALE = 1.0f;
    private final RenderSettings settings;
    private final RenderAPI context;

    public RegionRenderer(RenderAPI context, RenderSettings settings) {
        this.context = context;
        this.settings = settings;
    }

    public RenderSettings getSettings() {
        return this.settings;
    }

    public RenderRegion render(Tile tile) {
        RenderRegion renderRegion = new RenderRegion(tile);
        this.render(renderRegion);
        return renderRegion;
    }

    public void render(RenderRegion region) {
        region.clear();
        int resolution = this.settings.resolution;
        float w = (float)this.settings.width / ((float)resolution - 1.0f);
        float h = (float)this.settings.width / ((float)resolution - 1.0f);
        float unit = w / this.settings.zoom;
        RenderBuffer shape = this.context.createBuffer();
        shape.beginQuads();
        shape.noFill();
        for (int dy = 0; dy < resolution; ++dy) {
            for (int dx = 0; dx < resolution; ++dx) {
                this.draw(shape, region.getTile(), dx, dy, resolution, w, h, unit);
            }
        }
        shape.endQuads();
        region.setMesh(shape);
    }

    private void draw(RenderBuffer shape, Tile tile, int dx, int dz, int resolution, float w, float h, float unit) {
        Cell cell = tile.getCell(dx, dz);
        if (cell == null) {
            return;
        }
        float height = cell.value * (float)this.settings.levels.worldHeight;
        float x = (float)dx * w;
        float z = (float)dz * h;
        int y = this.getY(height, unit);
        this.settings.renderMode.fillColor(cell, height, shape, this.settings);
        shape.vertex(x, z, y);
        shape.vertex(x + w, z, y);
        shape.vertex(x + w, z + w, y);
        shape.vertex(x, z + w, y);
        if (dx <= 0 && dz <= 0) {
            this.drawEdge(shape, dx, y, dz, w, h, true);
            this.drawEdge(shape, dx, y, dz, w, h, false);
            return;
        }
        if (dx >= resolution - 1 && dz >= resolution - 1) {
            this.drawEdge(shape, dx + 1, y, dz, w, h, true);
            this.drawEdge(shape, dx, y, dz + 1, w, h, false);
            return;
        }
        if (dx <= 0 && dz >= resolution - 1) {
            this.drawEdge(shape, dx, y, dz, w, h, true);
            this.drawEdge(shape, dx, y, dz + 1, w, h, false);
            return;
        }
        if (dz <= 0 && dx >= resolution - 1) {
            this.drawEdge(shape, dx, y, dz, w, h, false);
            this.drawEdge(shape, dx + 1, y, dz, w, h, true);
            this.drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
            return;
        }
        if (dx <= 0) {
            this.drawEdge(shape, dx, y, dz, w, h, true);
            this.drawFace(shape, tile, dx, y, dz, dx, dz - 1, w, h, unit);
            return;
        }
        if (dz <= 0) {
            this.drawEdge(shape, dx, y, dz, w, h, false);
            this.drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
            return;
        }
        if (dx >= resolution - 1) {
            this.drawEdge(shape, dx + 1, y, dz, w, h, true);
            this.drawFace(shape, tile, dx, y, dz, dx, dz - 1, w, h, unit);
            this.drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
            return;
        }
        if (dz >= resolution - 1) {
            this.drawEdge(shape, dx, y, dz + 1, w, h, false);
            this.drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
            this.drawFace(shape, tile, dx, y, dz, dx, dz - 1, w, h, unit);
            return;
        }
        this.drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
        this.drawFace(shape, tile, dx, y, dz, dx, dz - 1, w, h, unit);
    }

    private void drawFace(RenderBuffer shape, Tile tile, int px, int py, int pz, int dx, int dz, float w, float h, float unit) {
        Cell cell = tile.getCell(dx, dz);
        if (cell == null) {
            return;
        }
        float height = cell.value * (float)this.settings.levels.worldHeight;
        int y = this.getY(height, unit);
        if (y == py) {
            return;
        }
        if (dx != px) {
            shape.vertex((float)px * w, (float)pz * h, py);
            shape.vertex((float)px * w, (float)(pz + 1) * h, py);
            shape.vertex((float)px * w, (float)(pz + 1) * h, y);
            shape.vertex((float)px * w, (float)pz * h, y);
        } else {
            shape.vertex((float)px * w, (float)pz * h, py);
            shape.vertex((float)(px + 1) * w, (float)pz * h, py);
            shape.vertex((float)(px + 1) * w, (float)pz * h, y);
            shape.vertex((float)px * w, (float)pz * h, y);
        }
    }

    private void drawEdge(RenderBuffer shape, int px, int py, int pz, float w, float h, boolean x) {
        if (x) {
            shape.vertex((float)px * w, (float)pz * h, py);
            shape.vertex((float)px * w, (float)(pz + 1) * h, py);
            shape.vertex((float)px * w, (float)(pz + 1) * h, py);
            shape.vertex((float)px * w, (float)pz * h, py);
        } else {
            shape.vertex((float)px * w, (float)pz * h, py);
            shape.vertex((float)(px + 1) * w, (float)pz * h, py);
            shape.vertex((float)(px + 1) * w, (float)pz * h, py);
            shape.vertex((float)px * w, (float)pz * h, py);
        }
    }

    private int getY(float height, float unit) {
        if (height <= (float)(-this.settings.levels.waterLevel)) {
            return (int)((float)this.settings.levels.waterLevel * unit);
        }
        return (int)((float)((int)height) * unit);
    }
}

