/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.spec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataSpecs {
    private static final Map<String, DataSpec<?>> specs = new ConcurrentHashMap<>();
    private static final Map<Class<?>, SubSpec<?>> subSpecs = new ConcurrentHashMap<>();
    private static final Map<Class<?>, SubSpec<?>> subSpecLookup = new ConcurrentHashMap<>();

    public static void register(DataSpec<?> spec) {
        specs.put(spec.getName(), spec);
    }

    public static <T, V extends T> void registerSub(Class<T> type, DataSpec<V> subSpec) {
        @SuppressWarnings("unchecked")
		SubSpec<T> spec = (SubSpec<T>) subSpecs.computeIfAbsent(type, SubSpec::new);
        spec.register(subSpec.getType(), subSpec);
        subSpecLookup.put(subSpec.getType(), spec);
    }

    public static boolean hasSpec(String name) {
        return specs.containsKey(name);
    }

    public static boolean isSubSpec(Object instance) {
        return subSpecLookup.containsKey(instance.getClass());
    }

    public static DataSpec<?> getSpec(String name) {
        DataSpec<?> spec = specs.get(name);
        if (spec == null) {
            throw new NullPointerException("Missing spec: '" + name + '\'');
        }
        return spec;
    }

    public static SubSpec<?> getSubSpec(Class<?> type) {
        return subSpecs.get(type);
    }

    public static SubSpec<?> getSubSpec(Object instance) {
        return subSpecLookup.get(instance.getClass());
    }

    public static <T> List<DataSpec<?>> getSpecs(Class<T> type) {
        List<DataSpec<?>> all = new ArrayList<>(specs.values());
        all.sort(Comparator.comparing(DataSpec::getName));
        List<DataSpec<?>> list = new ArrayList<>(all.size());
        for (DataSpec<?> dataSpec : all) {
            if (!type.isAssignableFrom(dataSpec.getType())) continue;
            list.add(dataSpec);
        }
        return list;
    }
}

