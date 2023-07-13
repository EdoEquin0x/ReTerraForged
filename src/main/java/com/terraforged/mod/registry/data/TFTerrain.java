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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.asset.TerrainNoise;
import com.terraforged.mod.level.levelgen.heightmap.Levels;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.level.levelgen.settings.TerrainSettings;
import com.terraforged.mod.level.levelgen.settings.TerrainSettings.General;
import com.terraforged.mod.level.levelgen.terrain.LandForms;
import com.terraforged.mod.level.levelgen.terrain.Terrain;
import com.terraforged.mod.level.levelgen.terrain.TerrainType;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.domain.Domain;
import com.terraforged.mod.util.seed.RandSeed;

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
    	var seed = Factory.createSeed();
        ctx.register(STEPPE, Factory.create(seed, TerrainType.FLATS, LandForms::steppe));
        ctx.register(PLAINS, Factory.create(seed, TerrainType.FLATS, LandForms::plains));
        ctx.register(HILLS_1, Factory.create(seed, TerrainType.HILLS, LandForms::hills1));
        ctx.register(HILLS_2, Factory.create(seed, TerrainType.HILLS, LandForms::hills1));
        ctx.register(DALES, Factory.create(seed, TerrainType.HILLS, LandForms::dales));
        ctx.register(PLATEAU, Factory.create(seed, TerrainType.PLATEAU, LandForms::plateau));
        ctx.register(BADLANDS, Factory.create(seed, TerrainType.BADLANDS, LandForms::badlands));
        ctx.register(TORRIDONIAN, Factory.create(seed, TerrainType.HILLS, LandForms::torridonian));
        ctx.register(MOUNTAINS_1, Factory.create(seed, TerrainType.MOUNTAINS, LandForms::mountains));
        ctx.register(MOUNTAINS_2, Factory.create(seed, TerrainType.MOUNTAINS, LandForms::mountains2));
        ctx.register(MOUNTAINS_3, Factory.create(seed, TerrainType.MOUNTAINS, LandForms::mountains3));
        ctx.register(DOLOMITES, Factory.createDolomite(seed, TerrainType.MOUNTAINS));
        ctx.register(MOUNTAINS_RIDGE_1, Factory.createNF(seed, TerrainType.MOUNTAINS, LandForms::mountains2));
        ctx.register(MOUNTAINS_RIDGE_2, Factory.createNF(seed, TerrainType.MOUNTAINS, LandForms::mountains3));
    }
    
    private static ResourceKey<TerrainNoise> resolve(String path) {
		return TerraForged.resolve(TerraForged.TERRAIN, path);
	}

    class Factory {
        static final LandForms LAND_FORMS = new LandForms(TerrainSettings.DEFAULT, new Levels(63, 255), Source.ZERO);
        static final LandForms LAND_FORMS_NF = new LandForms(nonFancy(), new Levels(63, 255), Source.ZERO);

        static Seed createSeed() {
            return new RandSeed(9712416L, 500_000);
        }

        static TerrainNoise create(Seed seed, Terrain type, BiFunction<LandForms, Seed, Module> factory) {
            return new TerrainNoise(type, factory.apply(LAND_FORMS, seed));
        }

        static TerrainNoise createNF(Seed seed, Terrain type, BiFunction<LandForms, Seed, Module> factory) {
            return new TerrainNoise(type, factory.apply(LAND_FORMS_NF, seed));
        }

        static TerrainNoise createDolomite(Seed seed, Terrain type) {
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

            var noise = shape.mul(peaks).max(slopes)
                    .warp(seed.next(), 800, 3, 300)
                    .scale(0.75);

            return new TerrainNoise(type, noise);
        }

        static TerrainSettings nonFancy() {
            return new TerrainSettings(
            	new General(
            		TerrainSettings.DEFAULT.general().seedOffset(),
            		TerrainSettings.DEFAULT.general().regionSize(),
            		TerrainSettings.DEFAULT.general().verticalScale(),
            		TerrainSettings.DEFAULT.general().horizontalScale(),
            		false
            	),
            	TerrainSettings.DEFAULT.steppe(),
            	TerrainSettings.DEFAULT.plains(),
            	TerrainSettings.DEFAULT.hills(),
            	TerrainSettings.DEFAULT.dales(),
            	TerrainSettings.DEFAULT.plateau(),
            	TerrainSettings.DEFAULT.badlands(),
            	TerrainSettings.DEFAULT.torridonian(),
            	TerrainSettings.DEFAULT.mountains()
            );
        }
    }
}
