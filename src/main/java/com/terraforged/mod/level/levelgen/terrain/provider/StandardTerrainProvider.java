/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.heightmap.RegionConfig;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.level.levelgen.settings.TerrainSettings;
import com.terraforged.mod.level.levelgen.terrain.LandForms;
import com.terraforged.mod.level.levelgen.terrain.populator.TerrainPopulator;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;

public class StandardTerrainProvider implements TerrainProvider {
    private final List<TerrainPopulator> mixable = new ArrayList<TerrainPopulator>();
    private final List<TerrainPopulator> unmixable = new ArrayList<TerrainPopulator>();
    private final List<Populator> populators = new ArrayList<>();
    private final Seed seed;
    private final LandForms landForms;
    private final RegionConfig config;
    private final TerrainSettings settings;

    public StandardTerrainProvider(GeneratorContext context, RegionConfig config) {
        this.seed = context.seed.offset(context.settings.terrain().general().seedOffset());
        this.config = config;
        this.settings = context.settings.terrain();
        this.landForms = new LandForms(context.settings.terrain(), context.levels, this.createGroundNoise(context));
        this.init(context);
    }

    protected Module createGroundNoise(GeneratorContext context) {
        return Source.constant(context.levels.ground);
    }

    protected void init(GeneratorContext context) {
        this.registerMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.steppe(this.seed), this.settings.steppe()));
        this.registerMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.plains(this.seed), this.settings.plains()));
        this.registerMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.dales(this.seed), this.settings.dales()));
        this.registerMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.hills1(this.seed), this.settings.hills()));
        this.registerMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.hills2(this.seed), this.settings.hills()));
        this.registerMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.torridonian(this.seed), this.settings.torridonian()));
        this.registerMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.plateau(this.seed), this.settings.plateau()));
        this.registerMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.badlands(this.seed), this.settings.badlands()));
        this.registerUnMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.badlands(this.seed), this.settings.badlands()));
        this.registerUnMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.mountains(this.seed), this.settings.mountains()));
        this.registerUnMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.mountains2(this.seed), this.settings.mountains()));
        this.registerUnMixable(TerrainPopulator.of(this.landForms.getLandBase(), this.landForms.mountains3(this.seed), this.settings.mountains()));
    }

    @Override
    public void registerMixable(TerrainPopulator populator) {
    	this.populators.add(populator);
        this.mixable.add(populator);
    }

    @Override
    public void registerUnMixable(TerrainPopulator populator) {
    	this.populators.add(populator);
        this.unmixable.add(populator);
    }

    @Override
    public LandForms getLandforms() {
        return this.landForms;
    }

    @Override
    public Populator[] getPopulators() {
        List<TerrainPopulator> mixed = StandardTerrainProvider.combine(StandardTerrainProvider.getMixable(this.mixable), (p1, p2) -> this.combine(p1, p2));
        ArrayList<Populator> result = new ArrayList<Populator>(mixed.size() + this.unmixable.size());
        result.addAll(mixed);
        result.addAll(this.unmixable);
        Collections.shuffle(result, new Random(this.seed.next()));
        return result.toArray(Populator[]::new);
    }
    
    private TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2) {
        return this.combine(tp1, tp2, this.seed, this.config.scale / 2);
    }

    private TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2, Seed seed, int scale) {
        Module combined = Source.perlin(seed.next(), scale, 1).warp(seed.next(), scale / 2, 2, (double)scale / 2.0).blend(tp1.getVariance(), tp2.getVariance(), 0.5, 0.25).clamp(0.0, 1.0);
        float weight = (tp1.getWeight() + tp2.getWeight()) / 2.0f;
        return new TerrainPopulator(this.landForms.getLandBase(), combined, weight);
    }

    private static <T> List<T> combine(List<T> input, BiFunction<T, T, T> operator) {
        int i;
        int length = input.size();
        for (int i2 = 1; i2 < input.size(); ++i2) {
            length += input.size() - i2;
        }
        ArrayList<T> result = new ArrayList<T>(length);
        for (i = 0; i < length; ++i) {
            result.add(null);
        }
        int k = input.size();
        for (i = 0; i < input.size(); ++i) {
            T t1 = input.get(i);
            result.set(i, t1);
            int j = i + 1;
            while (j < input.size()) {
                T t2 = input.get(j);
                T t3 = operator.apply(t1, t2);
                result.set(k, t3);
                ++j;
                ++k;
            }
        }
        return result;
    }

    private static List<TerrainPopulator> getMixable(List<TerrainPopulator> input) {
        ArrayList<TerrainPopulator> output = new ArrayList<>(input.size());
        for (TerrainPopulator populator : input) {
            if (!(populator.getWeight() > 0.0f)) continue;
            output.add(populator);
        }
        return output;
    }
}

