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

package com.terraforged.mod.platform.forge;

import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableSet;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.lifecycle.Stage;
import com.terraforged.mod.registry.data.TFBiomes;
import com.terraforged.mod.registry.data.TFCaves;
import com.terraforged.mod.registry.data.TFDataRegistries;
import com.terraforged.mod.registry.data.TFNoise;
import com.terraforged.mod.registry.data.TFPresets;
import com.terraforged.mod.registry.data.TFTags;
import com.terraforged.mod.registry.data.TFVegetation;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class TFData extends Stage {
	public static final TFData STAGE = new TFData();
	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
		.add(Registries.BIOME, TFBiomes::register)
		.add(TFDataRegistries.NOISE, TFNoise::register)
		.add(TFDataRegistries.CAVE, TFCaves::register)
		.add(TFDataRegistries.VEGETATION, TFVegetation::register)
		.add(Registries.WORLD_PRESET, TFPresets::register);
	
    @Override
    protected void doInit() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::onGenerateData);
    }

    void onGenerateData(GatherDataEvent event) {
    	boolean includeServer = event.includeServer();
    	CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
    	ExistingFileHelper fileHelper = event.getExistingFileHelper();
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput("resources");
    	
        generator.addProvider(includeServer, new DatapackBuiltinEntriesProvider(output, lookupProvider, BUILDER, ImmutableSet.of(TerraForged.MODID)));
        generator.addProvider(includeServer, TFTags.biomes(output, lookupProvider, fileHelper));
        generator.addProvider(includeServer, TFTags.blocks(output, lookupProvider, fileHelper));
    }
}
