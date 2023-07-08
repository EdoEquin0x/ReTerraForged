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

import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.command.TFCommands;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.ClimateType;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.mod.worldgen.asset.TerrainType;
import com.terraforged.mod.worldgen.asset.VegetationConfig;

import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

@Mod(TerraForged.MODID)
public class TFForge extends TerraForged implements CommonAPI {

	public TFForge() {
        super(TFForge::getRootPath);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onCreateDataPackRegistries);
        modBus.addListener(this::onRegister);
        
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        TFData.STAGE.run();

        if (FMLLoader.getDist().isClient()) {
            TFClient.STAGE.run();
        }
    }

    void onRegisterCommands(RegisterCommandsEvent event) {
        TFCommands.register(event.getDispatcher());
    }
    
    void onCreateDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
    	event.dataPackRegistry(TerraForged.CLIMATES, ClimateType.DIRECT_CODEC);
    	event.dataPackRegistry(TerraForged.CAVES, NoiseCave.DIRECT_CODEC);
    	event.dataPackRegistry(TerraForged.TERRAIN, TerrainNoise.DIRECT_CODEC);
    	event.dataPackRegistry(TerraForged.TERRAIN_TYPES, TerrainType.DIRECT_CODEC);
    	event.dataPackRegistry(TerraForged.VEGETATION, VegetationConfig.DIRECT_CODEC);
    }
    
    @SubscribeEvent
    void onRegister(RegisterEvent event) {
    	event.register(Registries.CHUNK_GENERATOR, (helper) -> {
    		helper.register("terraforged", Generator.CODEC);
    	});
    }
    
    private static Path getRootPath() {
        return ModList.get().getModContainerById(MODID).orElseThrow().getModInfo().getOwningFile().getFile().getFilePath();
    }
}
