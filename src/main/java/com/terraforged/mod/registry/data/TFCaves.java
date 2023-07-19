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

package com.terraforged.mod.registry.data;

import static com.terraforged.mod.TerraForged.registryKey;

import java.util.Random;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.level.levelgen.cave.UniqueCaveDistributor;
import com.terraforged.mod.level.levelgen.util.Seed;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.util.storage.WeightMap;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public interface TFCaves {
	ResourceKey<Registry<NoiseCave>> REGISTRY = registryKey("worldgen/cave");
	
	ResourceKey<NoiseCave> SYNAPSE_HIGH = resolve("synapse_high");
	ResourceKey<NoiseCave> SYNAPSE_MID = resolve("synapse_mid");
	ResourceKey<NoiseCave> SYNAPSE_LOW = resolve("synapse_low");
	ResourceKey<NoiseCave> MEGA = resolve("mega");
	ResourceKey<NoiseCave> MEGA_DEEP = resolve("mega_deep");
	
    static void register(BootstapContext<NoiseCave> ctx) {
        var seed = new Seed(new Random().nextInt());
        
        HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
        ctx.register(SYNAPSE_HIGH, Factory.synapse(new WeightMap.Builder<>().entry(1.0F, biomes.getOrThrow(Biomes.DRIPSTONE_CAVES)).build(), seed.next(), 0.75F, 96, 384));
        ctx.register(SYNAPSE_MID, Factory.synapse(new WeightMap.Builder<>().entry(1.0F, biomes.getOrThrow(Biomes.DRIPSTONE_CAVES)).build(), seed.next(), 1.0F, 0, 256));
        ctx.register(SYNAPSE_LOW, Factory.synapse(new WeightMap.Builder<>().entry(1.0F, biomes.getOrThrow(Biomes.DRIPSTONE_CAVES)).build(), seed.next(), 1.2F, -32, 128));
        ctx.register(MEGA, Factory.mega(new WeightMap.Builder<>().entry(1.0F, biomes.getOrThrow(Biomes.DRIPSTONE_CAVES)).build(), seed.next(), 1.0F, -16, 64));
        ctx.register(MEGA_DEEP, Factory.mega(new WeightMap.Builder<>().entry(1.0F, biomes.getOrThrow(Biomes.DRIPSTONE_CAVES)).build(), seed.next(), 1.2F, -32, 48));
    }

    private static ResourceKey<NoiseCave> resolve(String path) {
		return TerraForged.resolve(REGISTRY, path);
	}
    
    class Factory {
        static NoiseCave mega(WeightMap<Holder<Biome>> biomes, int seed, float scale, int minY, int maxY) {
            int elevationScale = NoiseUtil.floor(200 * scale);
            int networkScale = NoiseUtil.floor(250 * scale);
            int floorScale = NoiseUtil.floor(50 * scale);
            int size = NoiseUtil.floor(30 * scale);

            var elevation = Source.simplex(++seed, elevationScale, 2).map(0.3, 0.7);
            var shape = Source.simplex(++seed, networkScale, 3)
                    .bias(-0.5).abs().scale(2).invert()
                    .clamp(0.75, 1.0).map(0, 1);

            var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.3).map(0, 1);
            return new NoiseCave(biomes, Holder.direct(elevation), Holder.direct(shape), Holder.direct(floor), Holder.direct(createUniqueNoise(500, 0.05F)), size, minY, maxY);
        }

        static NoiseCave synapse(WeightMap<Holder<Biome>> biomes, int seed, float scale, int minY, int maxY) {
            int elevationScale = NoiseUtil.floor(350 * scale);
            int networkScale = NoiseUtil.floor(180 * scale);
            int networkWarpScale = NoiseUtil.floor(20 * scale);
            int networkWarpStrength = networkWarpScale / 2;
            int floorScale = NoiseUtil.floor(30 * scale);
            int size = NoiseUtil.floor(15 *  scale);

            var elevation = Source.simplex(++seed, elevationScale, 3).map(0.1, 0.9);
            var shape = Source.simplexRidge(++seed, networkScale, 3)
                    .warp(++seed, networkWarpScale, 1, networkWarpStrength)
                    .clamp(0.35, 0.75).map(0, 1);
            var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.15).map(0, 1);
            return new NoiseCave(biomes, Holder.direct(elevation), Holder.direct(shape), Holder.direct(floor), Holder.direct(Source.ONE), size, minY, maxY);
        }

        private static Module createUniqueNoise(int scale, float density) {
            return new UniqueCaveDistributor(1286745, 1F / scale, 0.75F, density)
                    .clamp(0.2, 1.0).map(0, 1)
                    .warp(781624, 30, 1, 20);
        }
    }
}
