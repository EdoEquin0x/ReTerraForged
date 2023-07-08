/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntComparator
 *  it.unimi.dsi.fastutil.ints.IntSet
 */
package com.terraforged.engine.world.biome.map;

import com.terraforged.engine.world.biome.TempCategory;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiomes;
import com.terraforged.engine.world.biome.map.defaults.FallbackBiomes;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntSet;

public interface BiomeContext<T> extends IntComparator {
    public int getId(T var1);

    public T getValue(int var1);

    public String getName(int var1);

    public IntSet getRiverOverrides();

    public Defaults<T> getDefaults();

    public Properties<T> getProperties();

    default public int compare(int a, int b) {
        return this.getName(a).compareTo(this.getName(b));
    }

    public static interface Properties<T> {
        public BiomeContext<T> getContext();

        public float getDepth(T var1);

        public float getMoisture(T var1);

        public float getTemperature(T var1);

        public TempCategory getTempCategory(T var1);

        public TempCategory getMountainCategory(T var1);

        default public float getDepth(int id) {
            return this.getDepth(this.getContext().getValue(id));
        }

        default public float getMoisture(int id) {
            return this.getMoisture(this.getContext().getValue(id));
        }

        default public float getTemperature(int id) {
            return this.getTemperature(this.getContext().getValue(id));
        }

        default public TempCategory getTempCategory(int id) {
            return this.getTempCategory(this.getContext().getValue(id));
        }

        default public TempCategory getMountainCategory(int id) {
            return this.getMountainCategory(this.getContext().getValue(id));
        }
    }

    public static interface Defaults<T> {
        public DefaultBiomes getDefaults();

        public FallbackBiomes<T> getFallbacks();
    }
}

