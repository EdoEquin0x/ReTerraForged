/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world;

import java.util.function.Function;

import com.terraforged.engine.Seed;
import com.terraforged.engine.concurrent.task.LazySupplier;
import com.terraforged.engine.concurrent.thread.ThreadPools;
import com.terraforged.engine.settings.Settings;
import com.terraforged.engine.tile.api.TileProvider;
import com.terraforged.engine.tile.gen.TileGenerator;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.provider.StandardTerrainProvider;
import com.terraforged.engine.world.terrain.provider.TerrainProviderFactory;

public class GeneratorContext {
    public final Seed seed;
    public final Levels levels;
    public final Settings settings;
    public final LazySupplier<TileProvider> cache;
    public final TerrainProviderFactory terrainFactory;
    public final LazySupplier<WorldGeneratorFactory> worldGenerator;

    public GeneratorContext(Settings settings) {
        this(settings, StandardTerrainProvider::new, GeneratorContext::createCache);
    }

    public <V> LazySupplier<V> then(Function<GeneratorContext, V> function) {
        return LazySupplier.factory(this.copy(), function);
    }

    public <T extends Settings> GeneratorContext(T settings, TerrainProviderFactory terrainFactory, Function<WorldGeneratorFactory, TileProvider> cache) {
        this.settings = settings;
        this.seed = new Seed(settings.world.seed);
        this.levels = new Levels(settings.world);
        this.terrainFactory = terrainFactory;
        this.worldGenerator = this.createFactory(this);
        this.cache = LazySupplier.supplied(this.worldGenerator, cache);
    }

    protected GeneratorContext(GeneratorContext src) {
        this(src, 0);
    }

    protected GeneratorContext(GeneratorContext src, int seedOffset) {
        this.seed = src.seed.offset(seedOffset);
        this.cache = src.cache;
        this.levels = src.levels;
        this.settings = src.settings;
        this.terrainFactory = src.terrainFactory;
        this.worldGenerator = src.worldGenerator;
    }

    public GeneratorContext copy() {
        return new GeneratorContext(this);
    }

    public GeneratorContext split(int offset) {
        return new GeneratorContext(this, offset);
    }

    public Seed seed() {
        return this.seed.split();
    }

    public Seed seed(int offset) {
        return this.seed.offset(offset);
    }

    protected LazySupplier<WorldGeneratorFactory> createFactory(GeneratorContext context) {
        return LazySupplier.factory(context.copy(), WorldGeneratorFactory::new);
    }

    public static GeneratorContext createNoCache(Settings settings) {
        return new GeneratorContext(settings, StandardTerrainProvider::new, s -> null);
    }

    protected static TileProvider createCache(WorldGeneratorFactory factory) {
        return TileGenerator.builder().pool(ThreadPools.createDefault()).factory(factory).size(3, 1).build().cached();
    }
}

