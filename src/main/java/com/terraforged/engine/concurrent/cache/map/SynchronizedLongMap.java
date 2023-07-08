/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 */
package com.terraforged.engine.concurrent.cache.map;

import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class SynchronizedLongMap<T> implements LongMap<T> {
    private final Object lock;
    private final Long2ObjectOpenHashMap<T> map;

    public SynchronizedLongMap(int size) {
        this.map = new Long2ObjectOpenHashMap<>(size);
        this.lock = this;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int size() {
        Object object = this.lock;
        synchronized (object) {
            return this.map.size();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clear() {
        Object object = this.lock;
        synchronized (object) {
            this.map.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(long key) {
        Object object = this.lock;
        synchronized (object) {
            this.map.remove(key);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(long key, Consumer<T> consumer) {
        Object object = this.lock;
        synchronized (object) {
            T t = this.map.remove(key);
            if (t != null) {
                consumer.accept(t);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int removeIf(Predicate<T> predicate) {
        Object object = this.lock;
        synchronized (object) {
            int startSize = this.map.size();
            ObjectIterator<Long2ObjectMap.Entry<T>> iterator = this.map.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                if (!predicate.test((iterator.next()).getValue())) continue;
                iterator.remove();
            }
            return startSize - this.map.size();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void put(long key, T t) {
        Object object = this.lock;
        synchronized (object) {
            this.map.put(key, t);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public T get(long key) {
        Object object = this.lock;
        synchronized (object) {
            return (T)this.map.get(key);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public T computeIfAbsent(long key, LongFunction<T> func) {
        Object object = this.lock;
        synchronized (object) {
            return (T)this.map.computeIfAbsent(key, func);
        }
    }
}

