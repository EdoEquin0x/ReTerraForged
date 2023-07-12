/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 */
package com.terraforged.mod.concurrent.cache.map;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class StampedLongMap<T> implements LongMap<T> {
    private final StampedLock lock;
    private final Long2ObjectOpenHashMap<T> map;

    public StampedLongMap(int size) {
        this.map = new Long2ObjectOpenHashMap<>(size);
        this.lock = new StampedLock();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int size() {
        long stamp = this.lock.readLock();
        try {
            int n = this.map.size();
            return n;
        }
        finally {
            this.lock.unlockRead(stamp);
        }
    }

    @Override
    public void clear() {
        long stamp = this.lock.writeLock();
        try {
            this.map.clear();
        }
        finally {
            this.lock.unlockWrite(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(long key) {
        long stamp = this.lock.writeLock();
        try {
            this.map.remove(key);
        }
        finally {
            this.lock.unlockWrite(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(long key, Consumer<T> consumer) {
        T t;
        long stamp = this.lock.writeLock();
        try {
            t = this.map.remove(key);
        }
        finally {
            this.lock.unlockWrite(stamp);
        }
        if (t != null) {
            consumer.accept(t);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int removeIf(Predicate<T> predicate) {
        long stamp = this.lock.writeLock();
        try {
            int startSize = this.map.size();
            ObjectIterator<Long2ObjectMap.Entry<T>> iterator = this.map.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                if (!predicate.test((iterator.next()).getValue())) continue;
                iterator.remove();
            }
            int n = startSize - this.map.size();
            return n;
        }
        finally {
            this.lock.unlockWrite(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void put(long key, T t) {
        long stamp = this.lock.writeLock();
        try {
            this.map.put(key, t);
        }
        finally {
            this.lock.unlockWrite(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public T get(long key) {
        long stamp = this.lock.readLock();
        try {
            T object = this.map.get(key);
            return object;
        }
        finally {
            this.lock.unlockRead(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public T computeIfAbsent(long key, LongFunction<T> func) {
        long readStamp = this.lock.readLock();
        try {
            T t = this.map.get(key);
            if (t != null) {
                T object = t;
                return object;
            }
        }
        finally {
            this.lock.unlockRead(readStamp);
        }
        long writeStamp = this.lock.writeLock();
        try {
            T object = this.map.computeIfAbsent(key, func);
            return object;
        }
        finally {
            this.lock.unlockWrite(writeStamp);
        }
    }
}

