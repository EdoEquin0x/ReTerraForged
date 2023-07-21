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

import java.util.Optional;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.level.levelgen.cave.UniqueCaveDistributor;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.level.levelgen.util.Seed;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.util.NoiseUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public interface TFCaves {
	ResourceKey<Registry<NoiseCave>> REGISTRY = registryKey("worldgen/cave");
	
	ResourceKey<NoiseCave> SYNAPSE_HIGH = resolve("synapse_high");
	ResourceKey<NoiseCave> SYNAPSE_MID = resolve("synapse_mid");
	ResourceKey<NoiseCave> SYNAPSE_LOW = resolve("synapse_low");
	ResourceKey<NoiseCave> MEGA = resolve("mega");
	ResourceKey<NoiseCave> MEGA_DEEP = resolve("mega_deep");
	ResourceKey<NoiseCave> IRON_VEIN = resolve("iron_vein");
	ResourceKey<NoiseCave> COPPER_VEIN = resolve("copper_vein");
	ResourceKey<NoiseCave> DEEP_LAVA_GEN = resolve("deep_lava_gen");
	
    static void register(BootstapContext<NoiseCave> ctx) {
        var seed = new Seed(0);
        
        HolderGetter<Climate> climates = ctx.lookup(TFClimates.REGISTRY);
        
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState copper = Blocks.IRON_ORE.defaultBlockState();
        BlockState iron = Blocks.COPPER_ORE.defaultBlockState();
        RuleTest carverTest = new TagMatchTest(BlockTags.OVERWORLD_CARVER_REPLACEABLES);
        //TODO these should still throw errors if the climate is missing
        ctx.register(SYNAPSE_HIGH, Factory.synapse(air, carverTest, climates.get(TFClimates.CAVE), seed.next(), 0.75F, 96, 384));
        ctx.register(SYNAPSE_MID, Factory.synapse(air, carverTest, climates.get(TFClimates.CAVE), seed.next(), 1.0F, 0, 256));
        ctx.register(SYNAPSE_LOW, Factory.synapse(air, carverTest, climates.get(TFClimates.CAVE), seed.next(), 1.2F, -32, 128));
        ctx.register(MEGA, Factory.mega(air, carverTest, climates.get(TFClimates.CAVE), seed.next(), 1.2F, -16, 64));
        ctx.register(MEGA_DEEP, Factory.mega(air, carverTest, climates.get(TFClimates.CAVE_DEEP), seed.next(), 1.4F, -64, 48));
        ctx.register(COPPER_VEIN, Factory.ore(copper, carverTest, seed.next(), 0.4F, -64, 200));
        ctx.register(IRON_VEIN, Factory.ore(iron, carverTest, seed.next(), 0.375F, -64, 150));
        ctx.register(DEEP_LAVA_GEN, Factory.deepLavaGen());
    }

    private static ResourceKey<NoiseCave> resolve(String path) {
		return TerraForged.resolve(REGISTRY, path);
	}
    
    class Factory {
        static NoiseCave mega(BlockState state, RuleTest test, Optional<? extends Holder<Climate>> climate, int seed, float scale, int minY, int maxY) {
            int elevationScale = NoiseUtil.floor(200 * scale);
            int networkScale = NoiseUtil.floor(250 * scale);
            int floorScale = NoiseUtil.floor(50 * scale);
            int size = NoiseUtil.floor(30 * scale);

            var elevation = Source.simplex(++seed, elevationScale, 2).map(0.0, 0.7);
            var shape = Source.simplex(++seed, networkScale, 3)
                    .bias(-0.5).abs().scale(2).invert()
                    .clamp(0.75, 1.0).map(0, 1);

            var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.3).map(0, 1);
            return new NoiseCave(state, test, climate, Holder.direct(elevation), Holder.direct(shape), Holder.direct(floor), Holder.direct(createUniqueCaveNoise(500, 0.7F)), size, minY, maxY);
        }

        static NoiseCave synapse(BlockState state, RuleTest test, Optional<? extends Holder<Climate>> climate, int seed, float scale, int minY, int maxY) {
            int elevationScale = NoiseUtil.floor(350 * scale);
            int networkScale = NoiseUtil.floor(180 * scale);
            int networkWarpScale = NoiseUtil.floor(20 * scale);
            int networkWarpStrength = networkWarpScale / 2;
            int floorScale = NoiseUtil.floor(30 * scale);
            int size = NoiseUtil.floor(15 *  scale);

            var elevation = Source.simplex(++seed, elevationScale, 3).map(0.0, 0.9);
            var shape = Source.simplexRidge(++seed, networkScale, 3)
                    .warp(++seed, networkWarpScale, 1, networkWarpStrength)
                    .clamp(0.35, 0.75).map(0, 1);
            var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.15).map(0, 1);
            return new NoiseCave(state, test, climate, Holder.direct(elevation), Holder.direct(shape), Holder.direct(floor), Holder.direct(Source.ONE), size, minY, maxY);
        }
        
        static NoiseCave ore(BlockState state, RuleTest test, int seed, float scale, int minY, int maxY) {
            int elevationScale = NoiseUtil.floor(350 * scale);
            int networkScale = NoiseUtil.floor(480 * scale);
            int networkWarpScale = NoiseUtil.floor(20 * scale);
            int networkWarpStrength = networkWarpScale / 2;
            int floorScale = NoiseUtil.floor(30 * scale);
            int size = NoiseUtil.floor(15 *  scale);

            var elevation = Source.simplex(++seed, elevationScale, 3).map(0.0, 0.9);
            var shape = Source.simplexRidge(++seed, networkScale, 3)
                    .warp(++seed, networkWarpScale, 1, networkWarpStrength)
                    .clamp(0.35, 0.75).map(0, 1);
            var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.15).map(0, 1);
            return new NoiseCave(state, test, Optional.empty(), Holder.direct(elevation), Holder.direct(shape), Holder.direct(floor), Holder.direct(createOreNoise(250, 0.55F)), size, minY, maxY);
        }
        
        static NoiseCave deepLavaGen() {
            return new NoiseCave(Blocks.LAVA.defaultBlockState(), new BlockMatchTest(Blocks.AIR), Optional.empty(), Holder.direct(Source.ZERO), Holder.direct(Source.ONE), Holder.direct(Source.ZERO), Holder.direct(Source.ONE), Math.abs(-64 - -55), -64, -55);
        }

        private static Module createUniqueCaveNoise(int scale, float density) {
            return new UniqueCaveDistributor(1286745, 1F / scale, 0.75F, density)
                    .clamp(0.2, 1.0).map(0, 1)
                    .warp(781624, 30, 1, 20);
        }
        
        private static Module createOreNoise(int scale, float density) {
            return new UniqueCaveDistributor(1286745, 1F / scale, 0.75F, density)
                    .clamp(0.2, 1.0).map(0, 1)
                    .warp(781624, 30, 1, 20);
        }
    }
}
