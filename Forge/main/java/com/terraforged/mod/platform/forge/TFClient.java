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

import com.terraforged.mod.lifecycle.Stage;

import net.minecraftforge.client.event.RegisterPresetEditorsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class TFClient extends Stage {
    public static final TFClient STAGE = new TFClient();

    @Override
    protected void doInit() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    	modBus.addListener(this::onPresetEditorRegister);
    }
    
    //TODO
    void onPresetEditorRegister(RegisterPresetEditorsEvent event) {
//    	event.register(TFPresets.TERRAFORGED, (parent, ctx) -> {
//    		Registry<Biome> biomes = ctx.worldgenLoadContext().registryOrThrow(Registries.BIOME);
//    		LevelStem overworld = ctx.selectedDimensions().dimensions().getOrThrow(LevelStem.OVERWORLD);
//    		TFChunkGenerator generator = (TFChunkGenerator) overworld.generator(); //FIXME unsafe cast
//    		return new WorldPreviewScreen(parent, biomes, generator);
//    	});
    }
}
