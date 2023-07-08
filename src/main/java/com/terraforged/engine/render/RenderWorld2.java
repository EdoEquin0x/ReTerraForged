/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.render;

import com.terraforged.engine.concurrent.task.LazyCallable;
import com.terraforged.engine.concurrent.thread.ThreadPool;
import com.terraforged.engine.concurrent.thread.ThreadPools;
import com.terraforged.engine.render.RenderWorld2.RegionHolder;
import com.terraforged.engine.tile.Size;
import com.terraforged.engine.tile.gen.TileGenerator;
import com.terraforged.engine.util.RollingGrid;
import com.terraforged.engine.util.pos.PosIterator;

public class RenderWorld2 implements RollingGrid.Generator<RegionHolder> {
    private final int factor;
    private final Size regionSize;
    private final TileGenerator generator;
    private final RenderAPI context;
    private final RegionRenderer renderer;
    private final RollingGrid<RegionHolder> world;
    private final ThreadPool threadPool = ThreadPools.createDefault();
    private boolean first = true;

    public RenderWorld2(TileGenerator generator, RenderAPI context, RenderSettings settings, int regionCount, int regionSize) {
        this.context = context;
        this.factor = regionSize;
        this.generator = generator;
        this.regionSize = Size.blocks(regionSize, 0);
        this.renderer = new RegionRenderer(context, settings);
        this.world = new RollingGrid<RegionHolder>(regionCount, RegionHolder[]::new, this);
    }

    public boolean isBusy() {
        for (RegionHolder h : this.world.getIterator()) {
            if (h == null || h.region.isDone()) continue;
            return true;
        }
        return false;
    }

    public int getResolution() {
        return this.regionSize.total * this.world.getSize();
    }

    public int blockToRegion(int value) {
        return value >> this.factor;
    }

    public void init(int seed, int centerX, int centerZ) {
        this.renderer.getSettings().zoom = 1.0f;
        this.renderer.getSettings().resolution = this.regionSize.total;
        PosIterator iterator = PosIterator.area(0, 0, this.world.getSize(), this.world.getSize());
        while (iterator.next()) {
            RegionHolder holder = this.generate(seed, iterator.x(), iterator.z());
            this.world.set(iterator.x(), iterator.z(), holder);
        }
    }

    public void move(int seed, int centerX, int centerZ) {
        if (this.first) {
            this.first = false;
            this.init(seed, centerX, centerZ);
        } else {
            this.renderer.getSettings().zoom = 1.0f;
            this.renderer.getSettings().resolution = this.regionSize.total;
            this.world.move(seed, centerX, centerZ);
        }
    }

    public void render() {
        int resolution = this.regionSize.total;
        float w = (float)this.renderer.getSettings().width * 1.0f / (float)(resolution - 1);
        float h = (float)this.renderer.getSettings().width * 1.0f / (float)(resolution - 1);
        float offsetX = (float)(this.world.getSize() * this.regionSize.size) * w / 2.0f;
        float offsetZ = (float)(this.world.getSize() * this.regionSize.size) * h / 2.0f;
        this.context.pushMatrix();
        this.context.translate(-offsetX, -offsetZ, 1000.0f);
        PosIterator iterator = PosIterator.area(0, 0, this.world.getSize(), this.world.getSize());
        while (iterator.next()) {
            RegionHolder holder = this.world.get(iterator.x(), iterator.z());
            if (holder == null || !holder.region.isDone()) continue;
            int relX = iterator.x();
            int relZ = iterator.z();
            float startX = (float)(relX * this.regionSize.size) * w;
            float startZ = (float)(relZ * this.regionSize.size) * h;
            RenderRegion region = (RenderRegion)holder.region.get();
            this.context.pushMatrix();
            this.context.translate(startX, startZ, 0.0f);
            region.getMesh().draw();
            this.context.popMatrix();
        }
        this.context.popMatrix();
    }

    @Override
    public RegionHolder generate(int seed, int x, int z) {
        return new RegionHolder(this.generator.getTile(seed, x, z).then(this.threadPool, this.renderer::render));
    }

    public static class RegionHolder {
        private final LazyCallable<RenderRegion> region;

        private RegionHolder(LazyCallable<RenderRegion> region) {
            this.region = region;
        }
    }
}

