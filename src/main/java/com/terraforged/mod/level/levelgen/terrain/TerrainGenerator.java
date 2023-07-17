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

import java.util.function.Supplier;

import com.terraforged.mod.level.levelgen.noise.NoiseGenerator;
import com.terraforged.mod.util.storage.ObjectPool;

public class TerrainGenerator {
    protected final TerrainLevels levels;
    protected final Supplier<NoiseGenerator> noiseGenerator;
    protected final ObjectPool<TerrainData> terrainDataPool;

    public TerrainGenerator(TerrainLevels levels, Supplier<NoiseGenerator> noiseGenerator) {
        this.levels = levels;
        this.noiseGenerator = noiseGenerator;
        this.terrainDataPool = new ObjectPool<>(() -> new TerrainData(this.levels));
    }

    public NoiseGenerator getNoiseGenerator() {
        return noiseGenerator.get();
    }

    public void restore(TerrainData terrainData) {
        terrainDataPool.restore(terrainData);
    }

    public TerrainData generate(int chunkX, int chunkZ) {
        var terrainData = terrainDataPool.take();
        noiseGenerator.get().generate(chunkX, chunkZ, terrainData);
        return terrainData;
    }
}
