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

import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.carver.CarvingContext;

public class VanillaGen {
    protected final NoiseBasedChunkGenerator vanillaGenerator;
    protected final Holder<NoiseGeneratorSettings> settings;

    protected final Supplier<Aquifer.FluidStatus> fluidStatus1;
    protected final Supplier<Aquifer.FluidStatus> fluidStatus2;
    protected final Aquifer.FluidPicker globalFluidPicker;

    public VanillaGen(BiomeSource biomeSource, VanillaGen other) {
        this(biomeSource, other.settings);
    }

    public VanillaGen(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        this.settings = settings;
        this.fluidStatus1 = () -> new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        this.fluidStatus2 = () -> new Aquifer.FluidStatus(settings.value().seaLevel(), settings.value().defaultFluid());
        this.globalFluidPicker = (x, y, z) -> y < Math.min(-54, settings.value().seaLevel()) ? this.fluidStatus1.get() : this.fluidStatus2.get();
        this.vanillaGenerator = new NoiseBasedChunkGenerator(biomeSource, settings);
    }

    public Holder<NoiseGeneratorSettings> getSettings() {
        return this.settings;
    }
    
    public Aquifer.FluidPicker getGlobalFluidPicker() {
        return this.globalFluidPicker;
    }

    public CarvingContext createCarvingContext(WorldGenRegion region, ChunkAccess chunk, NoiseChunk noiseChunk, RandomState state) {
        return new CarvingContext(this.vanillaGenerator, region.registryAccess(), chunk.getHeightAccessorForGeneration(), noiseChunk, state, null);
    }
}
