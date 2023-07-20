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

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.terraforged.mod.TerraForged;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public interface TFTags {
    TagKey<Biome> COPSES = resolve(Registries.BIOME, "trees/copses");
    TagKey<Biome> HARDY = resolve(Registries.BIOME, "trees/hardy");
    TagKey<Biome> HARDY_SLOPES = resolve(Registries.BIOME, "trees/hardy_slopes");
    TagKey<Biome> PATCHY = resolve(Registries.BIOME, "trees/patchy");
    TagKey<Biome> RAINFOREST = resolve(Registries.BIOME, "trees/rainforest");
    TagKey<Biome> SPARSE = resolve(Registries.BIOME, "trees/sparse");
    TagKey<Biome> SPARSE_RAINFOREST = resolve(Registries.BIOME, "trees/sparse_rainforest");
    TagKey<Biome> TEMPERATE = resolve(Registries.BIOME, "trees/temperate");
    
    TagKey<Block> ERODIBLE = resolve(Registries.BLOCK, "erodible");
    	
    static DataProvider biomes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    	return new BiomeTagsProvider(output, lookupProvider, TerraForged.MODID, existingFileHelper) {
			
			@Override
			protected void addTags(Provider provider) {
				this.tag(COPSES).add(Biomes.PLAINS);
				this.tag(HARDY).add(Biomes.FOREST);
				this.tag(HARDY_SLOPES).add(Biomes.FOREST);
				this.tag(PATCHY).add(Biomes.PLAINS);
				this.tag(RAINFOREST).add(Biomes.JUNGLE);
				this.tag(SPARSE_RAINFOREST).add(Biomes.SPARSE_JUNGLE);
				this.tag(TEMPERATE).add(Biomes.FOREST);
			}
		};
    }
    
    static DataProvider blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    	return new BlockTagsProvider(output, lookupProvider, TerraForged.MODID, existingFileHelper) {
			
			@Override
			protected void addTags(Provider provider) {
				this.tag(ERODIBLE).addTag(BlockTags.DIRT);
			}
		};
    }

    static DataProvider presets(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    	return new WorldPresetTagsProvider(output, lookupProvider, TerraForged.MODID, existingFileHelper) {
			
			@Override
			protected void addTags(Provider provider) {
				this.tag(WorldPresetTags.NORMAL).add(TFWorldPresets.TERRAFORGED);
			}
		};
    }
    
    static <T> TagKey<T> resolve(ResourceKey<Registry<T>> registry, String path) {
    	return TagKey.create(registry, TerraForged.location(path));
    }
}
