/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.worldgen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.data.codec.Codecs;
import com.terraforged.mod.util.storage.WeightMap;
import com.terraforged.mod.worldgen.asset.ClimateType;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainCache;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.mod.worldgen.util.ChunkUtil;
import com.terraforged.mod.worldgen.util.ThreadPool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class Generator extends ChunkGenerator implements IGenerator {
	// should these take HolderSets instead?
	public static final Codec<Generator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    	TerrainLevels.CODEC.optionalFieldOf("levels", TerrainLevels.DEFAULT).forGetter((g) -> g.levels),
    	Codec.unboundedMap(Codecs.forEnum(BiomeType::valueOf, BiomeType::name), ClimateType.CODEC).fieldOf("climates").forGetter((g) -> g.climates),
    	WeightMap.codec(TerrainNoise.CODEC, Holder[]::new).fieldOf("terrain").forGetter((g) -> g.terrain),
    	Codecs.forArray(VegetationConfig.CODEC, Holder[]::new).fieldOf("vegetation").forGetter((g) -> g.vegetation),
    	Codecs.forArray(Structure.CODEC, Holder[]::new).fieldOf("structures").forGetter((g) -> g.structures),
    	Codecs.forArray(NoiseCave.CODEC, Holder[]::new).fieldOf("caves").forGetter((g) -> g.caves),
    	RegistryOps.retrieveGetter(Registries.BIOME),
    	RegistryOps.retrieveGetter(Registries.NOISE_SETTINGS)
	).apply(instance, instance.stable(GeneratorPreset::build)));

    protected final TerrainLevels levels;
    protected final VanillaGen vanillaGen;
    protected final Source biomeSource;
    protected final BiomeGenerator biomeGenerator;
    protected final INoiseGenerator noiseGenerator;
    protected final TerrainCache terrainCache;
    protected final ThreadLocal<GeneratorResource> localResource = ThreadLocal.withInitial(GeneratorResource::new);
    private OptionalLong seed = OptionalLong.empty(); //TODO this is a hack, remove this
    protected final WeightMap<Holder<TerrainNoise>> terrain;
    protected final Map<BiomeType, Holder<ClimateType>> climates;
    protected final Holder<VegetationConfig>[] vegetation;
    protected final Holder<Structure>[] structures;
    protected final Holder<NoiseCave>[] caves;
    
    public Generator(
    	TerrainLevels levels,
    	VanillaGen vanillaGen,
    	Source biomeSource,
    	BiomeGenerator biomeGenerator,
    	INoiseGenerator noiseGenerator,
    	WeightMap<Holder<TerrainNoise>> terrain,
    	Map<BiomeType, Holder<ClimateType>> climates,
    	Holder<VegetationConfig>[] vegetation,
    	Holder<Structure>[] structures,
    	Holder<NoiseCave>[] caves
    ) {
        super(biomeSource);
        this.levels = levels;
        this.vanillaGen = vanillaGen;
        this.biomeSource = biomeSource;
        this.biomeGenerator = biomeGenerator;
        this.noiseGenerator = noiseGenerator;
        this.terrainCache = new TerrainCache(levels, noiseGenerator);
        this.terrain = terrain;
        this.climates = climates;
        this.vegetation = vegetation;
        this.structures = structures;
        this.caves = caves;
    }

    public VanillaGen getVanillaGen() {
        return this.vanillaGen;
    }

    public INoiseGenerator getNoiseGenerator() {
        return this.noiseGenerator;
    }

    public TerrainData getChunkData(int seed, ChunkPos pos) {
        return this.terrainCache.getNow(seed, pos);
    }

    public CompletableFuture<TerrainData> getChunkDataAsync(int seed, ChunkPos pos) {
        return this.terrainCache.getAsync(seed, pos);
    }

    @Override
    public Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getMinY() {
        return this.levels.minY;
    }

    @Override
    public int getSeaLevel() {
        return this.levels.seaLevel;
    }

    @Override
    public int getGenDepth() {
        return this.levels.maxY;
    }

    @Override
    public Source getBiomeSource() {
        return this.biomeSource;
    }
    
    private long setSeed(long seed) {
		if(this.seed.isEmpty()) { 
			this.biomeSource.withSeed(seed);
			this.seed = OptionalLong.of(seed);
		}
		return seed;
    }

    @Override
    public void createStructures(RegistryAccess access, ChunkGeneratorStructureState state, StructureManager structures, ChunkAccess chunk, StructureTemplateManager templates) {
    	this.terrainCache.hint(Seeds.get(this.setSeed(state.getLevelSeed())), chunk.getPos());
        super.createStructures(access, state, structures, chunk, templates);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureFeatures, ChunkAccess chunk) {
    	if(this.seed.isEmpty()) {
    		this.seed = OptionalLong.of(level.getSeed());
    	}
    	
    	this.terrainCache.hint(Seeds.get(this.seed.getAsLong()), chunk.getPos());
        super.createReferences(level, structureFeatures, chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Executor executor, RandomState state, Blender blender, StructureManager structures, ChunkAccess chunk) {
    	this.terrainCache.hint(Seeds.get(this.seed.getAsLong()), chunk.getPos());
        return CompletableFuture.supplyAsync(() -> {
            ChunkUtil.fillNoiseBiomes(chunk, this.biomeSource, this.localResource.get());
            return chunk;
        }, ThreadPool.EXECUTOR);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState state, StructureManager structureManager, ChunkAccess chunkAccess) {
        return terrainCache.combineAsync(executor, Seeds.get(this.seed.getAsLong()), chunkAccess, (chunk, terrainData) -> {
            ChunkUtil.fillChunk(getSeaLevel(), chunk, terrainData, ChunkUtil.FILLER, this.localResource.get());
            ChunkUtil.primeHeightmaps(getSeaLevel(), chunk, terrainData, ChunkUtil.FILLER);
            ChunkUtil.buildStructureTerrain(chunk, terrainData, structureManager);
            return chunk;
        });
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState state, ChunkAccess chunk) {
    	this.biomeGenerator.surface(chunk, region, state, this);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState state, BiomeManager biomes, StructureManager structures, ChunkAccess chunk, GenerationStep.Carving stage) {
    	this.biomeGenerator.carve((int) seed, chunk, region, biomes, stage, this);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel region, ChunkAccess chunk, StructureManager structures) {
        int seed = Seeds.get(region.getSeed());
        this.biomeGenerator.decorate(chunk, region, structures, this);
        this.terrainCache.drop(seed, chunk.getPos());
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {    	
        var settings = this.vanillaGen.getSettings().value();
        if (settings.disableMobGeneration()) return;

        var chunkPos = region.getCenter();
        var position = chunkPos.getWorldPosition().atY(region.getMaxBuildHeight() - 1);

        var holder = region.getBiome(position);
        var random = new WorldgenRandom(new LegacyRandomSource(region.getSeed()));
        random.setDecorationSeed(region.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());

        NaturalSpawner.spawnMobsForChunkGeneration(region, holder, chunkPos, random);
    }

    @Override
    public int getBaseHeight(int x, int z, net.minecraft.world.level.levelgen.Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState state) {
        var sample = this.terrainCache.getSample(Seeds.get(this.seed.getAsLong()), x, z);

        float scaledBase = this.levels.getScaledBaseLevel(sample.baseNoise);
        float scaledHeight = this.levels.getScaledHeight(sample.heightNoise);

        int base = this.levels.getHeight(scaledBase);
        int height = this.levels.getHeight(scaledHeight);

        return switch (types) {
            case WORLD_SURFACE, WORLD_SURFACE_WG, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES -> Math.max(base, height) + 1;
            case OCEAN_FLOOR, OCEAN_FLOOR_WG -> height + 1;
        };
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor, RandomState state) {
        var sample = this.terrainCache.getSample(Seeds.get(this.seed.getAsLong()), x, z);

        float scaledBase = this.levels.getScaledBaseLevel(sample.baseNoise);
        float scaledHeight = this.levels.getScaledHeight(sample.heightNoise);

        int base = this.levels.getHeight(scaledBase);
        int height = this.levels.getHeight(scaledHeight);
        int surface = Math.max(base, height);

        var states = new BlockState[surface];
        Arrays.fill(states, 0, height, Blocks.STONE.defaultBlockState());
        if (surface > height) {
            Arrays.fill(states, height, surface, Blocks.WATER.defaultBlockState());
        }

        return new NoiseColumn(height, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState state, BlockPos pos) {
        int seed = Seeds.get(this.seed.orElse(Long.MIN_VALUE));

        var sample = this.biomeSource.getBiomeSampler().getSample();
        this.terrainCache.sample(seed, pos.getX(), pos.getZ(), sample);
        this.biomeSource.getBiomeSampler().sample(seed, pos.getX(), pos.getZ(), sample);

        lines.add("");
        lines.add("[TerraForged]");
        lines.add("Terrain Type: " + sample.terrainType.getName());
        lines.add("Climate Type: " + sample.climateType.name());
        lines.add("Base Noise: " + sample.baseNoise);
        lines.add("Height Noise: " + sample.heightNoise);
        lines.add("Ocean Proximity: " + (1 - sample.continentNoise));
        lines.add("River Proximity: " + (1 - sample.riverNoise));
        lines.add("");
    }
}
