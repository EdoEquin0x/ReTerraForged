/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package com.terraforged.engine.world.biome.map;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.terraforged.engine.world.biome.TempCategory;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiomes;
import com.terraforged.engine.world.biome.map.defaults.FallbackBiomes;
import com.terraforged.engine.world.biome.type.BiomeType;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class BiomeMapBuilder<T> implements BiomeMap.Builder<T> {
    protected final Map<TempCategory, IntList> rivers = new HashMap<TempCategory, IntList>();
    protected final Map<TempCategory, IntList> lakes = new HashMap<TempCategory, IntList>();
    protected final Map<TempCategory, IntList> coasts = new HashMap<TempCategory, IntList>();
    protected final Map<TempCategory, IntList> beaches = new HashMap<TempCategory, IntList>();
    protected final Map<TempCategory, IntList> oceans = new HashMap<TempCategory, IntList>();
    protected final Map<TempCategory, IntList> deepOceans = new HashMap<TempCategory, IntList>();
    protected final Map<TempCategory, IntList> mountains = new HashMap<TempCategory, IntList>();
    protected final Map<TempCategory, IntList> volcanoes = new HashMap<TempCategory, IntList>();
    protected final Map<TempCategory, IntList> wetlands = new HashMap<TempCategory, IntList>();
    protected final Map<BiomeType, IntList> map = new EnumMap<BiomeType, IntList>(BiomeType.class);
    protected final BiomeContext<T> context;
    protected final DefaultBiomes defaults;
    protected final FallbackBiomes<T> fallbacks;
    private final Function<BiomeMapBuilder<T>, BiomeMap<T>> constructor;

    BiomeMapBuilder(BiomeContext<T> context, Function<BiomeMapBuilder<T>, BiomeMap<T>> constructor) {
        this.context = context;
        this.constructor = constructor;
        this.defaults = context.getDefaults().getDefaults();
        this.fallbacks = context.getDefaults().getFallbacks();
    }

    @Override
    public BiomeMap.Builder<T> addOcean(T biome, int count) {
        TempCategory category = this.context.getProperties().getTempCategory(biome);
        if (this.context.getProperties().getDepth(biome) < -1.0f) {
            this.add(this.deepOceans.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        } else {
            this.add(this.oceans.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        }
        return this;
    }

    @Override
    public BiomeMap.Builder<T> addBeach(T biome, int count) {
        TempCategory category = this.context.getProperties().getTempCategory(biome);
        this.add(this.beaches.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        return this;
    }

    @Override
    public BiomeMap.Builder<T> addCoast(T biome, int count) {
        TempCategory category = this.context.getProperties().getTempCategory(biome);
        this.add(this.coasts.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        return this;
    }

    @Override
    public BiomeMap.Builder<T> addRiver(T biome, int count) {
        TempCategory category = this.context.getProperties().getTempCategory(biome);
        this.add(this.rivers.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        return this;
    }

    @Override
    public BiomeMap.Builder<T> addLake(T biome, int count) {
        TempCategory category = this.context.getProperties().getTempCategory(biome);
        this.add(this.lakes.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        return this;
    }

    @Override
    public BiomeMap.Builder<T> addWetland(T biome, int count) {
        TempCategory category = this.context.getProperties().getTempCategory(biome);
        this.add(this.wetlands.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        return this;
    }

    @Override
    public BiomeMap.Builder<T> addMountain(T biome, int count) {
        TempCategory category = this.context.getProperties().getMountainCategory(biome);
        this.add(this.mountains.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        return this;
    }

    @Override
    public BiomeMap.Builder<T> addVolcano(T biome, int count) {
        TempCategory category = this.context.getProperties().getTempCategory(biome);
        this.add(this.volcanoes.computeIfAbsent(category, c -> new IntArrayList()), biome, count);
        return this;
    }

    @Override
    public BiomeMap.Builder<T> addLand(BiomeType type, T biome, int count) {
        this.add(this.map.computeIfAbsent(type, t -> new IntArrayList()), biome, count);
        return this;
    }

    @Override
    public BiomeMap<T> build() {
        this.makeSafe();
        return this.constructor.apply(this);
    }

    private void makeSafe() {
        this.addIfEmpty(this.rivers, this.fallbacks.river, TempCategory.class);
        this.addIfEmpty(this.lakes, this.fallbacks.lake, TempCategory.class);
        this.addIfEmpty(this.beaches, this.fallbacks.beach, TempCategory.class);
        this.addIfEmpty(this.oceans, this.fallbacks.ocean, TempCategory.class);
        this.addIfEmpty(this.deepOceans, this.fallbacks.deepOcean, TempCategory.class);
        this.addIfEmpty(this.wetlands, this.fallbacks.wetland, TempCategory.class);
        this.addIfEmpty(this.map, this.fallbacks.land, BiomeType.class);
    }

    private void add(IntList list, T biome, int count) {
        if (biome != null) {
            int id = this.context.getId(biome);
            for (int i = 0; i < count; ++i) {
                list.add(id);
            }
        }
    }

    private <E extends Enum<E>> void addIfEmpty(Map<E, IntList> map, T biome, Class<E> enumType) {
        for (E e : enumType.getEnumConstants()) {
            this.addIfEmpty(map.computeIfAbsent(e, t -> new IntArrayList()), biome);
        }
    }

    private void addIfEmpty(IntList list, T biome) {
        if (list.isEmpty()) {
            list.add(this.context.getId(biome));
        }
    }

    public static <T> BiomeMap.Builder<T> create(BiomeContext<T> context) {
        return new BiomeMapBuilder<T>(context, SimpleBiomeMap::new);
    }
}

