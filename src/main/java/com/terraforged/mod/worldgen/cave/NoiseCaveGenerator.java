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

package com.terraforged.mod.worldgen.cave;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.terraforged.mod.util.storage.ObjectPool;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

public class NoiseCaveGenerator {
    protected static final int POOL_SIZE = 32;
    protected static final float DENSITY = 0.05F;
    protected static final float BREACH_THRESHOLD = 0.7F;
    protected static final int GLOBAL_CAVE_REPS = 2;

    //TODO this should be final
    protected final Holder<NoiseCave>[] caves;
    //TODO remove
    protected final Module uniqueCaveNoise;
    protected final Module caveBreachNoise;
    protected final ObjectPool<CarverChunk> pool;
    protected final Map<ChunkPos, CarverChunk> cache = new ConcurrentHashMap<>();

    public NoiseCaveGenerator(Holder<NoiseCave>[] caves) {
    	this.caves = createArray(caves);
        this.uniqueCaveNoise = createUniqueNoise(500, DENSITY);
        this.caveBreachNoise = createBreachNoise(300, BREACH_THRESHOLD);
        this.pool = new ObjectPool<>(POOL_SIZE, () -> this.createCarverChunk(caves.length));
    }
    
    public void carve(int seed, ChunkAccess chunk, Generator generator) {
        var carver = getPreCarveChunk(chunk);
        carver.terrainData = generator.getChunkData(seed, chunk.getPos());
        carver.mask = this.caveBreachNoise;

        for (var config : this.caves) {
            carver.modifier = getModifier(config.value());

            NoiseCaveCarver.carve(seed, chunk, carver, generator, config.value(), true);
        }
    }

    public void decorate(int seed, ChunkAccess chunk, WorldGenLevel region, Generator generator) {
    	var carver = getPostCarveChunk(seed, chunk, generator);

        for (var config : this.caves) {
            NoiseCaveDecorator.decorate(chunk, carver, region, generator, config.value());
        }

        this.pool.restore(carver);
    }

    private CarverChunk getPreCarveChunk(ChunkAccess chunk) {
        return this.cache.computeIfAbsent(chunk.getPos(), p -> this.pool.take().reset());
    }

    private CarverChunk getPostCarveChunk(int seed, ChunkAccess chunk, Generator generator) {
        var carver = this.cache.remove(chunk.getPos());
        if (carver != null) return carver;

        // Chunk may have been saved in an incomplete state so need run the carve step
        // again to populate the CarverChunk (flag set false to skip setting blocks).

        carver = this.pool.take().reset();

        carver.mask = this.caveBreachNoise;
        carver.terrainData = generator.getChunkData(seed, chunk.getPos());

        for (int i = 0; i < this.caves.length; i++) {
        	var config = this.caves[i];
            carver.modifier = getModifier(config.value());

            NoiseCaveCarver.carve((int) (i * 0xFA90C2L) + seed, chunk, carver, generator, config.value(), false);
        }

        return carver;
    }

    private Module getModifier(NoiseCave cave) {
        return switch (cave.getType()) {
            case GLOBAL -> Source.ONE;
            case UNIQUE -> this.uniqueCaveNoise;
        };
    }

    private CarverChunk createCarverChunk(int length) {
        return new CarverChunk(length);
    }

    private static Module createUniqueNoise(int scale, float density) {
        return new UniqueCaveDistributor(1286745, 1F / scale, 0.75F, density)
                .clamp(0.2, 1.0).map(0, 1)
                .warp(781624, 30, 1, 20);
    }

    private static Module createBreachNoise(int scale, float threshold) {
        return Source.simplexRidge(1567328, scale, 2).clamp(threshold * 0.8F, threshold).map(0, 1);
    }

    private static Holder<NoiseCave>[] createArray(Holder<NoiseCave>[] source) {
        int length = 0;
        for (var cave : source) {
            length += getCount(cave);
        }

        @SuppressWarnings("unchecked")
		Holder<NoiseCave>[] array = new Holder[length];

        int i = 0;
        for (Holder<NoiseCave> cave : source) {
            int count = getCount(cave);
            for (int j = 0; j < count; j++) {
                array[i++] = cave;
            }
        }

        return array;
    }

    private static int getCount(Holder<NoiseCave> cave) {
//        return cave.getType() == CaveType.GLOBAL ? GLOBAL_CAVE_REPS : 1;
    	return GLOBAL_CAVE_REPS;
    }
}
