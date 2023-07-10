/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.terrain.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.terraforged.engine.Seed;
import com.terraforged.engine.cell.Populator;
import com.terraforged.engine.settings.TerrainSettings;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.heightmap.RegionConfig;
import com.terraforged.engine.world.terrain.LandForms;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.engine.world.terrain.populator.TerrainPopulator;
import com.terraforged.engine.world.terrain.special.VolcanoPopulator;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public class StandardTerrainProvider implements TerrainProvider {
    private final List<TerrainPopulator> mixable = new ArrayList<TerrainPopulator>();
    private final List<TerrainPopulator> unmixable = new ArrayList<TerrainPopulator>();
    private final Map<Terrain, List<Populator>> populators = new HashMap<Terrain, List<Populator>>();
    private final Seed seed;
    private final Levels levels;
    private final LandForms landForms;
    private final RegionConfig config;
    private final TerrainSettings settings;

    public StandardTerrainProvider(GeneratorContext context, RegionConfig config) {
        this.seed = context.seed.offset(context.settings.terrain.general.terrainSeedOffset);
        this.config = config;
        this.levels = context.levels;
        this.settings = context.settings.terrain;
        this.landForms = new LandForms(context.settings.terrain, context.levels, this.createGroundNoise(context));
        this.init(context);
    }

    protected Module createGroundNoise(GeneratorContext context) {
        return Source.constant(context.levels.ground);
    }

    protected void init(GeneratorContext context) {
        this.registerMixable(TerrainType.FLATS, this.landForms.getLandBase(), this.landForms.steppe(this.seed), this.settings.steppe);
        this.registerMixable(TerrainType.FLATS, this.landForms.getLandBase(), this.landForms.plains(this.seed), this.settings.plains);
        this.registerMixable(TerrainType.HILLS, this.landForms.getLandBase(), this.landForms.dales(this.seed), this.settings.dales);
        this.registerMixable(TerrainType.HILLS, this.landForms.getLandBase(), this.landForms.hills1(this.seed), this.settings.hills);
        this.registerMixable(TerrainType.HILLS, this.landForms.getLandBase(), this.landForms.hills2(this.seed), this.settings.hills);
        this.registerMixable(TerrainType.HILLS, this.landForms.getLandBase(), this.landForms.torridonian(this.seed), this.settings.torridonian);
        this.registerMixable(TerrainType.PLATEAU, this.landForms.getLandBase(), this.landForms.plateau(this.seed), this.settings.plateau);
        this.registerMixable(TerrainType.BADLANDS, this.landForms.getLandBase(), this.landForms.badlands(this.seed), this.settings.badlands);
        this.registerUnMixable(TerrainType.BADLANDS, this.landForms.getLandBase(), this.landForms.badlands(this.seed), this.settings.badlands);
        this.registerUnMixable(TerrainType.MOUNTAINS, this.landForms.getLandBase(), this.landForms.mountains(this.seed), this.settings.mountains);
        this.registerUnMixable(TerrainType.MOUNTAINS, this.landForms.getLandBase(), this.landForms.mountains2(this.seed), this.settings.mountains);
        this.registerUnMixable(TerrainType.MOUNTAINS, this.landForms.getLandBase(), this.landForms.mountains3(this.seed), this.settings.mountains);
        this.registerUnMixable(new VolcanoPopulator(this.seed, this.config, this.levels, this.settings.volcano.weight));
    }

    @Override
    public void forEach(Consumer<TerrainPopulator> consumer) {
        this.mixable.forEach(consumer);
        this.unmixable.forEach(consumer);
    }

    @Override
    public Terrain getTerrain(String name) {
        for (Terrain terrain : this.populators.keySet()) {
            if (!terrain.getName().equalsIgnoreCase(name)) continue;
            return terrain;
        }
        return null;
    }

    @Override
    public void registerMixable(TerrainPopulator populator) {
        this.populators.computeIfAbsent(populator.getType(), t -> new ArrayList<>()).add(populator);
        this.mixable.add(populator);
    }

    @Override
    public void registerUnMixable(TerrainPopulator populator) {
        this.populators.computeIfAbsent(populator.getType(), t -> new ArrayList<>()).add(populator);
        this.unmixable.add(populator);
    }

    @Override
    public int getVariantCount(Terrain terrain) {
        List<Populator> list = this.populators.get(terrain);
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public LandForms getLandforms() {
        return this.landForms;
    }

    @Override
    public List<Populator> getPopulators() {
        List<TerrainPopulator> mixed = StandardTerrainProvider.combine(StandardTerrainProvider.getMixable(this.mixable), (arg_0, arg_1) -> this.combine(arg_0, arg_1));
        ArrayList<Populator> result = new ArrayList<Populator>(mixed.size() + this.unmixable.size());
        result.addAll(mixed);
        result.addAll(this.unmixable);
        Collections.shuffle(result, new Random(this.seed.next()));
        return result;
    }

    public List<TerrainPopulator> getTerrainPopulators() {
        ArrayList<TerrainPopulator> populators = new ArrayList<TerrainPopulator>();
        populators.addAll(this.mixable);
        populators.addAll(this.unmixable);
        return populators;
    }

    private TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2) {
        return this.combine(tp1, tp2, this.seed, this.config.scale / 2);
    }

    private TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2, Seed seed, int scale) {
        Terrain type = TerrainType.registerComposite(tp1.getType(), tp2.getType());
        Module combined = Source.perlin(seed.next(), scale, 1).warp(seed.next(), scale / 2, 2, (double)scale / 2.0).blend(tp1.getVariance(), tp2.getVariance(), 0.5, 0.25).clamp(0.0, 1.0);
        float weight = (tp1.getWeight() + tp2.getWeight()) / 2.0f;
        return new TerrainPopulator(type, this.landForms.getLandBase(), combined, weight);
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
        ArrayList<TerrainPopulator> output = new ArrayList<TerrainPopulator>(input.size());
        for (TerrainPopulator populator : input) {
            if (!(populator.getWeight() > 0.0f)) continue;
            output.add(populator);
        }
        return output;
    }
}

