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

package com.terraforged.mod.level.levelgen.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.generator.terrain.Terrain;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

public record TerrainType(String name, Terrain parentType, Terrain terrain) {
    public static final Codec<TerrainType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(TerrainType::name),
            Codec.STRING.fieldOf("parent").xmap(TerrainType::forName, Terrain::getName).forGetter(TerrainType::parentType)
    ).apply(instance, TerrainType::new));

    public static final Codec<Holder<TerrainType>> CODEC = RegistryFileCodec.create(TerraForged.TERRAIN_TYPE, DIRECT_CODEC);

    public TerrainType(String name, Terrain type) {
    	this(name, type, com.terraforged.mod.level.levelgen.generator.terrain.TerrainType.getOrCreate(name, type));
    }

    private static Terrain forName(String name) {
        return com.terraforged.mod.level.levelgen.generator.terrain.TerrainType.get(name);
    }

    public static TerrainType of(Terrain terrain) {
        if (terrain.getDelegate() instanceof Terrain parent) {
            return new TerrainType(terrain.getName(), parent);
        }
        return new TerrainType(terrain.getName(), terrain);
    }
}
