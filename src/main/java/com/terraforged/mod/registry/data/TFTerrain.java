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

import java.util.function.BiFunction;

import com.terraforged.engine.Seed;
import com.terraforged.engine.settings.TerrainSettings;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.LandForms;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.seed.RandSeed;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;

public interface TFTerrain {
	ResourceKey<TerrainNoise> STEPPE = resolve("steppe");
	ResourceKey<TerrainNoise> PLAINS = resolve("plains");
	ResourceKey<TerrainNoise> HILLS_1 = resolve("hills_1");
	ResourceKey<TerrainNoise> HILLS_2 = resolve("hills_2");
	ResourceKey<TerrainNoise> DALES = resolve("dales");
	ResourceKey<TerrainNoise> PLATEAU = resolve("plateau");
	ResourceKey<TerrainNoise> BADLANDS = resolve("badlands");
	ResourceKey<TerrainNoise> TORRIDONIAN = resolve("torridonian");
	ResourceKey<TerrainNoise> MOUNTAINS_1 = resolve("mountains_1");
	ResourceKey<TerrainNoise> MOUNTAINS_2 = resolve("mountains_2");
	ResourceKey<TerrainNoise> MOUNTAINS_3 = resolve("mountains_3");
	ResourceKey<TerrainNoise> DOLOMITES = resolve("dolomites");
	ResourceKey<TerrainNoise> MOUNTAINS_RIDGE_1 = resolve("mountains_ridge_1");
	ResourceKey<TerrainNoise> MOUNTAINS_RIDGE_2 = resolve("mountains_ridge_2");
	
    static void register(BootstapContext<TerrainNoise> ctx) {
    	HolderGetter<com.terraforged.mod.worldgen.asset.TerrainType> holder = ctx.lookup(TerraForged.TERRAIN_TYPE);
    	
        var seed = Factory.createSeed();
        ctx.register(STEPPE, Factory.create(holder, seed, TerrainType.FLATS, LandForms::steppe));
        ctx.register(PLAINS, Factory.create(holder, seed, TerrainType.FLATS, LandForms::plains));
        ctx.register(HILLS_1, Factory.create(holder, seed, TerrainType.HILLS, LandForms::hills1));
        ctx.register(HILLS_2, Factory.create(holder, seed, TerrainType.HILLS, LandForms::hills1));
        ctx.register(DALES, Factory.create(holder, seed, TerrainType.HILLS, LandForms::dales));
        ctx.register(PLATEAU, Factory.create(holder, seed, TerrainType.PLATEAU, LandForms::plateau));
        ctx.register(BADLANDS, Factory.create(holder, seed, TerrainType.BADLANDS, LandForms::badlands));
        ctx.register(TORRIDONIAN, Factory.create(holder, seed, TFTerrainTypes.TORRIDONIAN, LandForms::torridonian));
        ctx.register(MOUNTAINS_1, Factory.create(holder, seed, TerrainType.MOUNTAINS, LandForms::mountains));
        ctx.register(MOUNTAINS_2, Factory.create(holder, seed, TerrainType.MOUNTAINS, LandForms::mountains2));
        ctx.register(MOUNTAINS_3, Factory.create(holder, seed, TerrainType.MOUNTAINS, LandForms::mountains3));
        ctx.register(DOLOMITES, Factory.createDolomite(holder, seed, TFTerrainTypes.DOLOMITES));
        ctx.register(MOUNTAINS_RIDGE_1, Factory.createNF(holder, seed, TerrainType.MOUNTAINS, LandForms::mountains2));
        ctx.register(MOUNTAINS_RIDGE_2, Factory.createNF(holder, seed, TerrainType.MOUNTAINS, LandForms::mountains3));
    }
    
    private static ResourceKey<TerrainNoise> resolve(String path) {
		return TerraForged.resolve(TerraForged.TERRAIN, path);
	}

    class Factory {
        static final LandForms LAND_FORMS = new LandForms(settings(), new Levels(63, 255), Source.ZERO);
        static final LandForms LAND_FORMS_NF = new LandForms(nonFancy(), new Levels(63, 255), Source.ZERO);

        static Seed createSeed() {
            return new RandSeed(9712416L, 500_000);
        }

        static TerrainNoise create(HolderGetter<com.terraforged.mod.worldgen.asset.TerrainType> getter, Seed seed, Terrain type, BiFunction<LandForms, Seed, Module> factory) {
            return new TerrainNoise(getType(getter, type), factory.apply(LAND_FORMS, seed));
        }

        static TerrainNoise createNF(HolderGetter<com.terraforged.mod.worldgen.asset.TerrainType> getter, Seed seed, Terrain type, BiFunction<LandForms, Seed, Module> factory) {
            return new TerrainNoise(getType(getter, type), factory.apply(LAND_FORMS_NF, seed));
        }

        static TerrainNoise createDolomite(HolderGetter<com.terraforged.mod.worldgen.asset.TerrainType> getter, Seed seed, Terrain type) {
            // Valley floor terrain
            var base = Source.simplex(seed.next(), 80, 4).scale(0.1);

            // Controls where the ridges show up
            var shape = Source.simplex(seed.next(), 475, 4)
                    .clamp(0.3, 1.0).map(0, 1)
                    .warp(seed.next(), 10, 2, 8);

            // More gradual slopes up to the ridges
            var slopes = shape.pow(2.2).scale(0.65).add(base);

            // Sharp ridges
            var peaks = Source.build(seed.next(), 400, 5).lacunarity(2.7).gain(0.6).simplexRidge()
                    .clamp(0, 0.675).map(0, 1)
                    .warp(Domain.warp(Source.SIMPLEX, seed.next(), 40, 5, 30))
                    .alpha(0.875);

            var noise = shape.mult(peaks).max(slopes)
                    .warp(seed.next(), 800, 3, 300)
                    .scale(0.75);

            return new TerrainNoise(getType(getter, type), noise);
        }

        static Holder<com.terraforged.mod.worldgen.asset.TerrainType> getType(HolderGetter<com.terraforged.mod.worldgen.asset.TerrainType> getter, Terrain terrain) {
            return getter.getOrThrow(TerraForged.resolve(TerraForged.TERRAIN_TYPE, terrain.getName()));
        }

        static TerrainSettings settings() {
            var settings = new TerrainSettings();
            settings.general.globalVerticalScale = 1F;
            return settings;
        }

        static TerrainSettings nonFancy() {
            var settings = settings();
            settings.general.fancyMountains = false;
            return settings;
        }
    }
}
