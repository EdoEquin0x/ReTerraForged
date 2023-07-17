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

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableSet;
import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.command.TFCommands;
import com.terraforged.mod.level.levelgen.biome.vegetation.VegetationConfig;
import com.terraforged.mod.level.levelgen.cave.NoiseCave;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.registry.TFChunkGenerators;
import com.terraforged.mod.registry.TFCurves;
import com.terraforged.mod.registry.TFDomains;
import com.terraforged.mod.registry.TFModules;
import com.terraforged.mod.registry.TFRegistries;
import com.terraforged.mod.registry.TFViabilities;
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
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.client.event.RegisterPresetEditorsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

@Mod(TerraForged.MODID)
public class TFForge extends TerraForged implements CommonAPI {

	public TFForge() {
        super(TFForge::getRootPath);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onCreateRegistries);
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onCreateDataPackRegistries);
        modBus.addListener(this::onGenerateData);
        modBus.addListener(this::onPresetEditorRegister);
        
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    void onRegisterCommands(RegisterCommandsEvent event) {
        TFCommands.register(event.getDispatcher());
    }
    
    void onCreateRegistries(NewRegistryEvent event) {
    	TFRegistries.register(event::create);
    }
    
    void onRegister(RegisterEvent event) {
    	event.register(Registries.CHUNK_GENERATOR, (helper) -> {
    		TFChunkGenerators.register(helper::register);
    		logRegister(Registries.CHUNK_GENERATOR);
    	});
    	event.register(TFRegistries.MODULE_TYPE, (helper) -> {
    		TFModules.register(helper::register);
    		logRegister(TFRegistries.MODULE_TYPE);
    	});
    	event.register(TFRegistries.DOMAIN_TYPE, (helper) -> {
    		TFDomains.register(helper::register);
    		logRegister(TFRegistries.DOMAIN_TYPE);
    	});
    	event.register(TFRegistries.CURVE_TYPE, (helper) -> {
    		TFCurves.register(helper::register);
    		logRegister(TFRegistries.CURVE_TYPE);
    	});
    	event.register(TFRegistries.VIABILITY_TYPE, (helper) -> {
    		TFViabilities.register(helper::register);
    		logRegister(TFRegistries.VIABILITY_TYPE);
    	});
    }
    
    void onCreateDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
    	event.dataPackRegistry(TFDataRegistries.NOISE, Module.DIRECT_CODEC);
    	event.dataPackRegistry(TFDataRegistries.CAVE, NoiseCave.DIRECT_CODEC);
    	event.dataPackRegistry(TFDataRegistries.VEGETATION, VegetationConfig.DIRECT_CODEC);
    }

    void onGenerateData(GatherDataEvent event) {
    	RegistrySetBuilder builder = new RegistrySetBuilder(); {
        	builder.add(Registries.BIOME, TFBiomes::register);
        	builder.add(TFDataRegistries.NOISE, TFNoise::register);
        	builder.add(TFDataRegistries.CAVE, TFCaves::register);
        	builder.add(TFDataRegistries.VEGETATION, TFVegetation::register);
        	builder.add(Registries.WORLD_PRESET, TFPresets::register);
    	}
    	boolean includeServer = event.includeServer();
    	CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
    	ExistingFileHelper fileHelper = event.getExistingFileHelper();
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput("resources");
    	
        generator.addProvider(includeServer, new DatapackBuiltinEntriesProvider(output, lookupProvider, builder, ImmutableSet.of(TerraForged.MODID)));
        generator.addProvider(includeServer, TFTags.biomes(output, lookupProvider, fileHelper));
        generator.addProvider(includeServer, TFTags.blocks(output, lookupProvider, fileHelper));
    }
    
    //TODO
    void onPresetEditorRegister(RegisterPresetEditorsEvent event) {
//    	event.register(TFPresets.TERRAFORGED, (parent, ctx) -> {
//    		LevelStem overworld = ctx.selectedDimensions().dimensions().getOrThrow(LevelStem.OVERWORLD);
//    		if(overworld.generator() instanceof TFChunkGenerator tfGen) {
//        		tfGen.init((int) ctx.options().seed());
//        		return new WorldPreviewScreen(parent, tfGen);
//    		} else {
//    			return parent;
//    		}
//    	});
    }
    
    private static void logRegister(ResourceKey<?> registry) {
    	LOG.info("Registered entries for {}", registry.location());
    }
    
    private static Path getRootPath() {
        return ModList.get().getModContainerById(MODID).orElseThrow().getModInfo().getOwningFile().getFile().getFilePath();
    }
}
