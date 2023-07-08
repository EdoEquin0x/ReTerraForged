/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator
 */
package com.terraforged.engine.concurrent.cache.map;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

public class StampedBoundLongMap<T> implements LongMap<T> {
    private final int capacity;
    private final StampedLock lock;
    private final Long2ObjectLinkedOpenHashMap<T> map;

    public StampedBoundLongMap(int size) {
        this.capacity = size;
        this.lock = new StampedLock();
        this.map = new Long2ObjectLinkedOpenHashMap<>(size);
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
            ObjectBidirectionalIterator<Long2ObjectMap.Entry<T>> iterator = this.map.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2ObjectMap.Entry<T> entry = iterator.next();
                if (!predicate.test(entry.getValue())) continue;
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
            if (this.map.size() >= this.capacity) {
                this.map.removeFirst();
            }
            T object = this.map.computeIfAbsent(key, func);
            return object;
        }
        finally {
            this.lock.unlockWrite(writeStamp);
        }
    }
}

