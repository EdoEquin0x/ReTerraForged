/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.HashCommon
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator
 */
package com.terraforged.mod.concurrent.cache.map;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

public class LoadBalanceLongMap<T> implements LongMap<T> {
    private final int mask;
    private final int sectionCapacity;
    private final Long2ObjectLinkedOpenHashMap<T>[] maps;
    private final StampedLock[] locks;

    @SuppressWarnings("unchecked")
	public LoadBalanceLongMap(int factor, int size) {
        factor = LoadBalanceLongMap.getNearestFactor(factor);
        size = LoadBalanceLongMap.getSectionSize(size, factor);
        this.mask = factor - 1;
        this.sectionCapacity = size - 2;
        this.maps = new Long2ObjectLinkedOpenHashMap[factor];
        this.locks = new StampedLock[factor];
        for (int i = 0; i < factor; ++i) {
            this.maps[i] = new Long2ObjectLinkedOpenHashMap<>(size);
            this.locks[i] = new StampedLock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < this.locks.length; ++i) {
            StampedLock lock = this.locks[i];
            long stamp = lock.readLock();
            try {
                size += this.maps[i].size();
                continue;
            }
            finally {
                lock.unlockRead(stamp);
            }
        }
        return size;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clear() {
        for (int i = 0; i < this.locks.length; ++i) {
            StampedLock lock = this.locks[i];
            long stamp = lock.writeLock();
            try {
                this.maps[i].clear();
                continue;
            }
            finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(long key) {
        int index = this.getIndex(key);
        StampedLock lock = this.locks[index];
        long stamp = lock.writeLock();
        try {
            this.maps[index].remove(key);
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void remove(long key, Consumer<T> consumer) {
        int index = this.getIndex(key);
        StampedLock lock = this.locks[index];
        long stamp = lock.writeLock();
        try {
            this.maps[index].remove(key, consumer);
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int removeIf(Predicate<T> predicate) {
        int count = 0;
        for (int i = 0; i < this.locks.length; ++i) {
            StampedLock lock = this.locks[i];
            Long2ObjectLinkedOpenHashMap<T> map = this.maps[i];
            long stamp = lock.writeLock();
            try {
                int startSize = map.size();
                ObjectBidirectionalIterator<Long2ObjectMap.Entry<T>> iterator = map.long2ObjectEntrySet().fastIterator();
                while (iterator.hasNext()) {
                    Long2ObjectMap.Entry<T> entry = iterator.next();
                    if (!predicate.test(entry.getValue())) continue;
                    iterator.remove();
                }
                count += startSize - map.size();
                continue;
            }
            finally {
                lock.unlockWrite(stamp);
            }
        }
        return count;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void put(long key, T value) {
        int index = this.getIndex(key);
        StampedLock lock = this.locks[index];
        Long2ObjectLinkedOpenHashMap<T> map = this.maps[index];
        long stamp = lock.writeLock();
        try {
            if (map.size() > this.sectionCapacity) {
                map.removeFirst();
            }
            map.put(key, value);
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public T get(long key) {
        int index = this.getIndex(key);
        StampedLock lock = this.locks[index];
        long stamp = lock.readLock();
        try {
            T object = this.maps[index].get(key);
            return object;
        }
        finally {
            lock.unlockRead(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public T computeIfAbsent(long key, LongFunction<T> factory) {
        int index = this.getIndex(key);
        StampedLock lock = this.locks[index];
        Long2ObjectLinkedOpenHashMap<T> map = this.maps[index];
        long readStamp = lock.readLock();
        try {
            T t = map.get(key);
            if (t != null) {
                T object = t;
                return object;
            }
        }
        finally {
            lock.unlockRead(readStamp);
        }
        long writeStamp = lock.writeLock();
        try {
            if (map.size() > this.sectionCapacity) {
                map.removeFirst();
            }
            T object = map.computeIfAbsent(key, factory);
            return object;
        }
        finally {
            lock.unlockWrite(writeStamp);
        }
    }

    private int getIndex(long key) {
        return HashCommon.long2int((long)key) & this.mask;
    }

    private static int getSectionSize(int size, int factor) {
        int section = size / factor;
        if (section * factor < size) {
            ++section;
        }
        return section;
    }

    private static int getNearestFactor(int i) {
        int j = 0;
        while (i != 0) {
            i >>= 1;
            ++j;
        }
        return j;
    }
}

