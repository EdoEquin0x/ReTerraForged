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

package com.terraforged.mod.level.levelgen.terrain;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.terraforged.mod.level.levelgen.noise.TerrainNoise;
import com.terraforged.mod.level.levelgen.util.ThreadPool;
import com.terraforged.mod.util.storage.ObjectPool;

import net.minecraft.world.level.ChunkPos;

public class TerrainCache {
    private final TerrainGenerator generator;
    private final Map<CacheKey, CacheValue> cache = new ConcurrentHashMap<>(512);

    private final ThreadLocal<CacheKey> localKey = ThreadLocal.withInitial(CacheKey::new);
    private final ObjectPool<CacheKey> keyPool = new ObjectPool<>(512, CacheKey::new);
    private final ObjectPool<CacheValue> valuePool = new ObjectPool<>(512, CacheValue::new);

    public TerrainCache(TerrainLevels levels, Supplier<TerrainNoise> terrainNoise) {
        this.generator = new TerrainGenerator(levels, terrainNoise);
    }

    protected CacheKey allocPos(ChunkPos pos) {
        return keyPool.take().set(pos.x, pos.z);
    }

    protected CacheKey lookupPos(ChunkPos pos) {
        return localKey.get().set(pos.x, pos.z);
    }

    public void drop(ChunkPos pos) {
        var key = lookupPos(pos);
        var value = cache.remove(key);

        if (value == null || value.task == null) return;

        generator.restore(value.task.join());

        keyPool.restore(value.key);

        valuePool.restore(value.reset());
    }

    public void hint(ChunkPos pos) {
        getAsync(pos);
    }

    public TerrainData getNow(ChunkPos pos) {
        return getAsync(pos).join();
    }

    @Nullable
    public TerrainData getIfReady(ChunkPos pos) {
        var key = allocPos(pos);
        var value = cache.get(key);

        if (value == null || !value.task.isDone()) return null;

        return value.task.join();
    }

    public CompletableFuture<TerrainData> getAsync(ChunkPos pos) {
        var key = allocPos(pos);
        return cache.computeIfAbsent(key, this::generate).task;
    }

    protected CacheValue generate(CacheKey key) {
        var value = valuePool.take();
        value.key = key;
        value.generator = generator;
        value.task = CompletableFuture.supplyAsync(value, ThreadPool.EXECUTOR);
        return value;
    }

    protected static class CacheKey {
        protected int x, z;

        public CacheKey set(int x, int y) {
            this.x = x;
            this.z = y;
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CacheKey pos && x == pos.x && z == pos.z;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
    }

    protected static class CacheValue implements Supplier<TerrainData> {
        protected CacheKey key;
        protected TerrainGenerator generator;
        protected CompletableFuture<TerrainData> task;

        public CacheValue reset() {
            key = null;
            task = null;
            generator = null;
            return this;
        }

        @Override
        public TerrainData get() {
            return generator.generate(key.x, key.z);
        }
    }
}
