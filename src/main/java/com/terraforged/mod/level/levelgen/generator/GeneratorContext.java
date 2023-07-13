/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.concurrent.task.LazySupplier;
import com.terraforged.mod.concurrent.thread.ThreadPools;
import com.terraforged.mod.level.levelgen.heightmap.Levels;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.terrain.provider.StandardTerrainProvider;
import com.terraforged.mod.level.levelgen.terrain.provider.TerrainProviderFactory;
import com.terraforged.mod.level.levelgen.tile.api.TileProvider;
import com.terraforged.mod.level.levelgen.tile.gen.TileGenerator;

public class GeneratorContext {
	public static final Codec<GeneratorContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Seed.CODEC.fieldOf("seed").forGetter((c) -> c.seed),
		Settings.CODEC.fieldOf("settings").forGetter((c) -> c.settings)
	).apply(instance, GeneratorContext::new));
	
    public final Seed seed;
    public final Levels levels;
    public final Settings settings;
    public final LazySupplier<TileProvider> cache;
    public final TerrainProviderFactory terrainFactory;
    public final LazySupplier<WorldGenerator> worldGenerator;

    public GeneratorContext(Seed seed, Settings settings) {
        this(seed, settings, StandardTerrainProvider::new, GeneratorContext::createCache);
    }

    public <V> LazySupplier<V> then(Function<GeneratorContext, V> function) {
        return LazySupplier.factory(this.copy(), function);
    }

    public GeneratorContext(Seed seed, Settings settings, TerrainProviderFactory terrainFactory, Function<WorldGenerator, TileProvider> cache) {
        this.settings = settings;
        this.seed = seed;
        this.levels = new Levels(settings.world());
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

    protected LazySupplier<WorldGenerator> createFactory(GeneratorContext context) {
        return LazySupplier.factory(context.copy(), WorldGenerator::new);
    }
    
    protected static TileProvider createCache(WorldGenerator factory) {
        return TileGenerator.builder().pool(ThreadPools.createDefault()).generator(factory).size(3, 1).build().cached();
    }
}

