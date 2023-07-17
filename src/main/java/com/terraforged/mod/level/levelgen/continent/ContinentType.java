/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent;

import com.mojang.serialization.Codec;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.level.levelgen.continent.advanced.AdvancedContinentGenerator;
import com.terraforged.mod.level.levelgen.continent.fancy.FancyContinentGenerator;
import com.terraforged.mod.level.levelgen.continent.simple.MultiContinentGenerator;
import com.terraforged.mod.level.levelgen.continent.simple.SingleContinentGenerator;
import com.terraforged.mod.level.levelgen.settings.Levels;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.util.Seed;

public enum ContinentType {
    MULTI(0) {

        @Override
        public Continent create(Seed seed, Levels levels, Settings settings) {
            return new MultiContinentGenerator(seed, levels, settings);
        }
    }
    ,
    SINGLE(1) {

        @Override
        public Continent create(Seed seed, Levels levels, Settings settings) {
            return new SingleContinentGenerator(seed, levels, settings);
        }
    }
    ,
    MULTI_IMPROVED(2) {

        @Override
        public Continent create(Seed seed, Levels levels, Settings settings) {
            return new AdvancedContinentGenerator(seed, levels, settings);
        }
    }
    ,
    FANCY(3) {

        @Override
        public Continent create(Seed seed, Levels levels, Settings settings) {
            return new FancyContinentGenerator(seed, levels, settings);
        }
    };

	public static final Codec<ContinentType> CODEC = TFCodecs.forEnum(ContinentType::valueOf);
	
    public final int index;

    private ContinentType(int index) {
        this.index = index;
    }

    public abstract Continent create(Seed seed, Levels levels, Settings settings);
}

