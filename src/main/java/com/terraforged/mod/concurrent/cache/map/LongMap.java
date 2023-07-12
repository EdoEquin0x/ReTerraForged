/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent.cache.map;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Predicate;

public interface LongMap<T> {
    public int size();

    public void clear();

    public void remove(long var1);

    public void remove(long var1, Consumer<T> var3);

    public int removeIf(Predicate<T> var1);

    public void put(long var1, T var3);

    public T get(long var1);

    public T computeIfAbsent(long var1, LongFunction<T> var3);

    default public <V> V map(long key, LongFunction<T> factory, Function<T, V> mapper) {
        return mapper.apply(this.computeIfAbsent(key, factory));
    }
}

