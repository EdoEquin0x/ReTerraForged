/*
- * MIT License
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

package com.terraforged.mod.level.levelgen.generator;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.level.levelgen.SampledDensityFunction;
import com.terraforged.mod.level.levelgen.biome.CaveBiomeSampler;
import com.terraforged.mod.level.levelgen.biome.ChunkPopulator;
import com.terraforged.mod.level.levelgen.biome.source.TFBiomeSource;
import com.terraforged.mod.level.levelgen.biome.vegetation.VegetationConfig;
import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.level.levelgen.climate.ClimateSampler;
import com.terraforged.mod.level.levelgen.noise.NoiseGenerator;
import com.terraforged.mod.level.levelgen.noise.erosion.ErosionNoiseGenerator;
import com.terraforged.mod.level.levelgen.noise.erosion.NoiseTileSize;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.terrain.TerrainCache;
import com.terraforged.mod.level.levelgen.terrain.TerrainData;
import com.terraforged.mod.level.levelgen.terrain.TerrainLevels;
import com.terraforged.mod.level.levelgen.util.ChunkUtil;
import com.terraforged.mod.level.levelgen.util.NoopNoise;
import com.terraforged.mod.level.levelgen.util.ThreadPool;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.util.IntLazy;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Aquifer.FluidPicker;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TFChunkGenerator extends NoiseBasedChunkGenerator {
	@SuppressWarnings("unchecked")
	public static final Codec<TFChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Settings.CODEC.fieldOf("settings").forGetter((g) -> g.settings),
    	TerrainLevels.CODEC.fieldOf("levels").forGetter((g) -> g.levels),
    	WeightMap.codec(Module.CODEC, (size) -> (Holder<Module>[]) new Holder[size]).fieldOf("terrain").forGetter((g) -> g.terrain),
    	TFCodecs.forArray(VegetationConfig.CODEC, (size) -> (Holder<VegetationConfig>[]) new Holder[size]).fieldOf("vegetation").forGetter((g) -> g.vegetation),
    	TFCodecs.forArray(NoiseCave.CODEC, (size) -> (Holder<NoiseCave>[]) new Holder[size]).fieldOf("caves").forGetter((g) -> g.caves),
    	BiomeSource.CODEC.fieldOf("biomes").forGetter((g) -> g.biomeSource.getDelegate())
	).apply(instance, instance.stable(TFChunkGenerator::create)));

	protected final Settings settings;
    protected final TerrainLevels levels;
    protected final TFBiomeSource biomeSource;
    protected final ChunkPopulator chunkPopulator;
    protected final IntLazy<NoiseGenerator> noiseGenerator;
    protected final ClimateSampler climateSampler;
    protected final CaveBiomeSampler caveBiomeSampler;
    protected final TerrainCache terrainCache;
    protected final ThreadLocal<GeneratorResource> localResource = ThreadLocal.withInitial(GeneratorResource::new);
    protected final WeightMap<Holder<Module>> terrain;
    protected final Holder<VegetationConfig>[] vegetation;
    protected final Holder<NoiseCave>[] caves;
    protected final Aquifer.FluidPicker globalFluidPicker;

    public TFChunkGenerator(
    	Settings settings,
    	TerrainLevels levels,
    	TFBiomeSource biomeSource,
    	ChunkPopulator chunkPopulator,
    	IntLazy<NoiseGenerator> noiseGenerator,
    	ClimateSampler climateSampler,
    	CaveBiomeSampler caveBiomeSampler,
    	WeightMap<Holder<Module>> terrain,
    	Holder<VegetationConfig>[] vegetation,
    	Holder<NoiseCave>[] caves
    ) {
        super(biomeSource, createGeneratorSettings(levels, climateSampler));
        this.settings = settings;
        this.levels = levels;
        this.biomeSource = biomeSource;
        this.chunkPopulator = chunkPopulator;
        this.noiseGenerator = noiseGenerator;
        this.climateSampler = climateSampler;
        this.caveBiomeSampler = caveBiomeSampler;
        this.terrainCache = new TerrainCache(levels, noiseGenerator);
        this.terrain = terrain;
        this.vegetation = vegetation;
        this.caves = caves;
        this.globalFluidPicker = createGlobalFluidPicker(this.generatorSettings());
    }

    public void init(int seed) {
    	this.noiseGenerator.apply((int) seed);
    }
    
    public NoiseGenerator getNoiseGenerator() {
        return this.noiseGenerator.get();
    }
    
    public ClimateSampler getClimateSampler() {
    	return this.climateSampler;
    }

    public CaveBiomeSampler getCaveBiomeSampler() {
    	return this.caveBiomeSampler;
    }
    
    public TerrainData getChunkData(ChunkPos pos) {
        return this.terrainCache.getNow(pos);
    }

    public CompletableFuture<TerrainData> getChunkDataAsync(ChunkPos pos) {
        return this.terrainCache.getAsync(pos);
    }

    @Override
    public Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void createStructures(RegistryAccess access, ChunkGeneratorStructureState state, StructureManager structures, ChunkAccess chunk, StructureTemplateManager templates) {
    	this.terrainCache.hint(chunk.getPos());
        super.createStructures(access, state, structures, chunk, templates);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureFeatures, ChunkAccess chunk) {
    	this.terrainCache.hint(chunk.getPos());
        super.createReferences(level, structureFeatures, chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Executor executor, RandomState state, Blender blender, StructureManager structures, ChunkAccess chunk) {
    	this.terrainCache.hint(chunk.getPos());
        return CompletableFuture.supplyAsync(() -> {
            ChunkUtil.fillNoiseBiomes(chunk, this.biomeSource, this.localResource.get());
            return chunk;
        }, ThreadPool.EXECUTOR);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState state, StructureManager structureManager, ChunkAccess chunkAccess) {
    	return terrainCache.getAsync(chunkAccess.getPos()).thenApplyAsync((terrainData) -> {
    		int seaLevel = this.getSeaLevel();
    		ChunkUtil.fillChunk(seaLevel, chunkAccess, terrainData, ChunkUtil.FILLER, this.localResource.get());
    		ChunkUtil.primeHeightmaps(seaLevel, chunkAccess, terrainData, ChunkUtil.FILLER);
    		ChunkUtil.buildStructureTerrain(chunkAccess, terrainData, structureManager);
    		return chunkAccess;
    	});
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState state, ChunkAccess chunk) {
    	this.chunkPopulator.surface(chunk, region, state, this);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState state, BiomeManager biomes, StructureManager structures, ChunkAccess chunk, GenerationStep.Carving stage) {
    	this.chunkPopulator.carve((int) seed, chunk, region, biomes, stage, this);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel region, ChunkAccess chunk, StructureManager structures) {
        SectionPos sectionpos = SectionPos.of(chunk.getPos(), region.getMinSection());
        BlockPos blockpos = sectionpos.origin();
        WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        long i = worldgenrandom.setDecorationSeed(region.getSeed(), blockpos.getX(), blockpos.getZ());
        this.chunkPopulator.decorate((int) i, chunk, region, structures, this);
        this.terrainCache.drop(chunk.getPos());
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState state) {
        var sample = this.noiseGenerator.get().getNoiseSample(x, z);

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
        var sample = this.noiseGenerator.get().getNoiseSample(x, z);

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
    	var sample = this.climateSampler.sample(pos.getX(), pos.getZ());
        lines.add("");
        lines.add("[TerraForged] (raw noise)");
        lines.add("Temperature: " + sample.temperature);
        lines.add("Moisture: " + sample.moisture);
        lines.add("Continent noise: " + sample.continentNoise);
        lines.add("River noise: " + sample.riverNoise);
        lines.add("Biome noise: " + sample.biomeNoise);
        lines.add("Base Noise: " + sample.baseNoise);
        lines.add("Height Noise: " + sample.heightNoise);
        lines.add("");
        
        var mcSample = state.sampler().sample(pos.getX(), pos.getY(), pos.getZ());
        lines.add("");
        lines.add("[TerraForged] [mc noise]");
        lines.add("Temperature: " + Climate.unquantizeCoord(mcSample.temperature()));
        lines.add("Humidity: " + Climate.unquantizeCoord(mcSample.humidity()));
        lines.add("Continent: " + Climate.unquantizeCoord(mcSample.continentalness()));
        lines.add("Erosion: " + Climate.unquantizeCoord(mcSample.erosion()));
        lines.add("Depth: " + Climate.unquantizeCoord(mcSample.depth()));
        lines.add("Weirdness: " + Climate.unquantizeCoord(mcSample.weirdness()));
        lines.add("");
    }
    
    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structures, RandomState state, long seed) {
    	this.init((int) seed);
    	return ChunkGeneratorStructureState.createForNormal(state, seed, this.biomeSource, structures);
    }

    public Aquifer.FluidPicker getGlobalFluidPicker() {
    	return this.globalFluidPicker;
    }
    
    public static TFChunkGenerator create(
    	Settings settings,
    	TerrainLevels levels,
    	WeightMap<Holder<Module>> terrain,
    	Holder<VegetationConfig>[] vegetation,
    	Holder<NoiseCave>[] caves,
    	BiomeSource delegate
    ) {
    	var chunkPopulator = new ChunkPopulator(vegetation, caves);
    	IntLazy<NoiseGenerator> noiseGenerator = IntLazy.of(seed -> new ErosionNoiseGenerator(seed, settings, levels, terrain, NoiseTileSize.DEFAULT));
    	var climateSampler = new ClimateSampler(noiseGenerator);
		var caveBiomeSampler = new CaveBiomeSampler(800);
    	return new TFChunkGenerator(settings, levels, new TFBiomeSource(delegate), chunkPopulator, noiseGenerator, climateSampler, caveBiomeSampler, terrain, vegetation, caves);
    }
    
    private static Holder<NoiseGeneratorSettings> createGeneratorSettings(TerrainLevels levels, ClimateSampler sampler) {
    	return Holder.direct(
    		new NoiseGeneratorSettings(
	    		new NoiseSettings(levels.minY, levels.maxY, 1, 1), 
	    		Blocks.STONE.defaultBlockState(), 
	    		Blocks.WATER.defaultBlockState(), 
	    		new NoiseRouter(
	    			NoopNoise.NOOP,
	    			NoopNoise.NOOP,
	    			NoopNoise.NOOP,
	    			NoopNoise.NOOP,
	    			new SampledDensityFunction(sampler, (sample) -> sample.temperature * 2 + 1),
	    			new SampledDensityFunction(sampler, (sample) -> sample.moisture * 2 + 1),
	    			new SampledDensityFunction(sampler, (sample) -> sample.continentNoise * 2 + 1),
	    			new SampledDensityFunction(sampler, (sample) -> sample.riverNoise),
	    			new SampledDensityFunction(sampler, (sample) -> 0),
	    			new SampledDensityFunction(sampler, (sample) -> sample.biomeNoise),
	    			NoopNoise.NOOP,
	    			NoopNoise.NOOP,
	    			NoopNoise.NOOP,
	    			NoopNoise.NOOP,
	    			NoopNoise.NOOP
	    		), 
	    		SurfaceRuleData.overworld(), 
	    		new OverworldBiomeBuilder().spawnTarget(), 
	    		levels.seaLevel, 
	    		false, 
	    		false, 
	    		false, 
	    		false
    		)
    	);
    }
    
    private static FluidPicker createGlobalFluidPicker(Holder<NoiseGeneratorSettings> settings) {
    	Aquifer.FluidStatus fluidStatus1 = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
    	Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(settings.value().seaLevel(), settings.value().defaultFluid());
    	return (x, y, z) -> y < Math.min(-54, settings.value().seaLevel()) ? fluidStatus1 : fluidStatus2;
	}
}
