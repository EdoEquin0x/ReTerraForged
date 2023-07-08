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
import com.terraforged.mod.data.ModBiomes;
import com.terraforged.mod.data.ModCaves;
import com.terraforged.mod.data.ModClimates;
import com.terraforged.mod.data.ModPresets;
import com.terraforged.mod.data.ModTerrainTypes;
import com.terraforged.mod.data.ModTerrain;
import com.terraforged.mod.data.ModVegetation;
import com.terraforged.mod.lifecycle.Stage;

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
		.add(Registries.BIOME, ModBiomes::register)
		.add(TerraForged.CAVES, ModCaves::register)
		.add(TerraForged.CLIMATES, ModClimates::register)
		.add(TerraForged.TERRAIN_TYPES, ModTerrainTypes::register)
		.add(TerraForged.TERRAIN, ModTerrain::register)
		.add(TerraForged.VEGETATION, ModVegetation::register)
		.add(Registries.WORLD_PRESET, ModPresets::register);
	
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
