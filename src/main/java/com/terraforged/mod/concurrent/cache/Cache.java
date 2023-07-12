/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import com.terraforged.mod.concurrent.cache.map.LongMap;
import com.terraforged.mod.concurrent.cache.map.StampedBoundLongMap;

public class Cache<V extends ExpiringEntry> implements Runnable, Predicate<V> {
    private final String name;
    private final LongMap<V> map;
    private final long lifetimeMS;
    private volatile long timeout = 0L;

    public Cache(String name, long expireTime, long interval, TimeUnit unit) {
        this(name, 256, expireTime, interval, unit);
    }

    public Cache(String name, int capacity, long expireTime, long interval, TimeUnit unit) {
        this(name, capacity, expireTime, interval, unit, StampedBoundLongMap::new);
    }

    public Cache(String name, int capacity, long expireTime, long interval, TimeUnit unit, IntFunction<LongMap<V>> mapFunc) {
        this.name = name;
        this.map = mapFunc.apply(capacity);
        this.lifetimeMS = unit.toMillis(expireTime);
        CacheManager.get().schedule(this, unit.toMillis(interval));
    }

    public String getName() {
        return this.name;
    }

    public void remove(long key) {
        this.map.remove(key, ExpiringEntry::close);
    }

    public V get(long key) {
        return this.map.get(key);
    }

    public V computeIfAbsent(long key, LongFunction<V> func) {
        return this.map.computeIfAbsent(key, func);
    }

    public <T> T map(long key, LongFunction<V> func, Function<V, T> mapper) {
        return this.map.map(key, func, mapper);
    }

    @Override
    public void run() {
        this.timeout = System.currentTimeMillis() - this.lifetimeMS;
        this.map.removeIf(this);
    }

    @Override
    public boolean test(V v) {
        return v.getTimestamp() < this.timeout;
    }
}

