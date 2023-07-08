/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 */
package com.terraforged.engine.util;

import com.terraforged.noise.util.NoiseUtil;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListUtils {
    public static <T> T get(List<T> list, float value, T def) {
        if (list.isEmpty()) {
            return def;
        }
        return ListUtils.get(list, list.size() - 1, value, def);
    }

    public static int get(IntList list, float value, int def) {
        if (list.isEmpty()) {
            return def;
        }
        return ListUtils.get(list, list.size() - 1, value, def);
    }

    public static <T> T get(List<T> list, int maxIndex, float value, T def) {
        if (maxIndex <= 0 || list.isEmpty()) {
            return def;
        }
        int index = NoiseUtil.round(value * (float)maxIndex);
        if (index < list.size()) {
            return list.get(index);
        }
        return def;
    }

    public static int get(IntList list, int maxIndex, float value, int def) {
        if (maxIndex <= 0 || list.isEmpty()) {
            return def;
        }
        int index = NoiseUtil.round(value * (float)maxIndex);
        if (index < list.size()) {
            return list.getInt(index);
        }
        return def;
    }

    public static <T> List<T> minimize(List<T> list) {
        Map<T, Integer> counts = ListUtils.count(list);
        ArrayList<T> result = new ArrayList<T>(list.size());
        int min = counts.values().stream().min(Integer::compareTo).orElse(1);
        for (T t : list) {
            int count = counts.get(t);
            int amount = count / min;
            for (int i = 0; i < amount; ++i) {
                result.add(t);
            }
        }
        return result;
    }

    public static <T> Map<T, Integer> count(List<T> list) {
        HashMap<T, Integer> map = new HashMap<T, Integer>(list.size());
        for (T t : list) {
            int count = map.getOrDefault(t, 0);
            map.put(t, ++count);
        }
        return map;
    }

    public static IntSet combine(IntList a, IntList b) {
        IntOpenHashSet set = new IntOpenHashSet((IntCollection)a);
        set.addAll((IntCollection)b);
        return set;
    }
}

