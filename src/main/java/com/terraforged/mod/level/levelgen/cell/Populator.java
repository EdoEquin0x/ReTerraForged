/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.cell;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.concurrent.Resource;
import com.terraforged.mod.noise.Module;

public interface Populator extends Module {
	public static final Codec<Populator> CODEC = TFCodecs.registryCodec(TerraForged.POPULATOR, Populator::codec);
	
    public void apply(Cell var1, float var2, float var3);

    @Override
    default public float getValue(float x, float z) {
        try (Resource<Cell> cell = Cell.getResource();){
            this.apply(cell.get(), x, z);
            float f = cell.get().value;
            return f;
        }
    }
    
    Codec<? extends Populator> codec();
}

