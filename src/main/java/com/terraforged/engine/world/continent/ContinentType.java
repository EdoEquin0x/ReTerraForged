/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.continent;

import com.terraforged.engine.Seed;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.continent.advanced.AdvancedContinentGenerator;
import com.terraforged.engine.world.continent.fancy.FancyContinentGenerator;
import com.terraforged.engine.world.continent.simple.MultiContinentGenerator;
import com.terraforged.engine.world.continent.simple.SingleContinentGenerator;

public enum ContinentType {
    MULTI(0){

        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new MultiContinentGenerator(seed, context);
        }
    }
    ,
    SINGLE(1){

        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new SingleContinentGenerator(seed, context);
        }
    }
    ,
    MULTI_IMPROVED(2){

        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new AdvancedContinentGenerator(seed, context);
        }
    }
    ,
    FANCY(3){

        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new FancyContinentGenerator(seed, context);
        }
    };

    public final int index;

    private ContinentType(int index) {
        this.index = index;
    }

    public abstract Continent create(Seed var1, GeneratorContext var2);
}

