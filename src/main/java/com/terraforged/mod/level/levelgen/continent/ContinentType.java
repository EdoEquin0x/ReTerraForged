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
import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.seed.Seed;

public enum ContinentType {
    MULTI(0) {

        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new MultiContinentGenerator(seed, context);
        }
    }
    ,
    SINGLE(1) {

        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new SingleContinentGenerator(seed, context);
        }
    }
    ,
    MULTI_IMPROVED(2) {

        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new AdvancedContinentGenerator(seed, context);
        }
    }
    ,
    FANCY(3) {

        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new FancyContinentGenerator(seed, context);
        }
    };

	public static final Codec<ContinentType> CODEC = TFCodecs.forEnum(ContinentType::valueOf);
	
    public final int index;

    private ContinentType(int index) {
        this.index = index;
    }

    public abstract Continent create(Seed seed, GeneratorContext context);
}

