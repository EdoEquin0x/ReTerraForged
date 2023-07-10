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

import com.google.common.collect.ImmutableSet;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.lifecycle.Stage;
import com.terraforged.mod.registry.data.TFBiomes;
import com.terraforged.mod.registry.data.TFCaves;
import com.terraforged.mod.registry.data.TFPresets;
import com.terraforged.mod.registry.data.TFTerrain;
import com.terraforged.mod.registry.data.TFTerrainTypes;
import com.terraforged.mod.registry.data.TFVegetation;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class TFData extends Stage {
	public static final TFData STAGE = new TFData();
	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
		.add(Registries.BIOME, TFBiomes::register)
		.add(TerraForged.CAVE, TFCaves::register)
		.add(TerraForged.TERRAIN_TYPE, TFTerrainTypes::register)
		.add(TerraForged.TERRAIN, TFTerrain::register)
		.add(TerraForged.VEGETATION, TFVegetation::register)
		.add(Registries.WORLD_PRESET, TFPresets::register);
	
    @Override
    protected void doInit() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::onGenerateData);
    }

    void onGenerateData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput("resources");
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), BUILDER, ImmutableSet.of(TerraForged.MODID)));
    }
}
