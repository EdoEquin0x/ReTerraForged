/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongArrays
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package com.terraforged.engine.util.fastpoisson;

import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Random;

public class LongIterSet {
    public static final long NULL = Long.MAX_VALUE;
    private int size = 0;
    private int index = -1;
    private long[] order = new long[32];
    private final LongSet points = new LongOpenHashSet(32);

    public boolean contains(long value) {
        return this.points.contains(value);
    }

    public boolean add(long value) {
        if (this.points.add(value)) {
            this.order = LongIterSet.ensureCapacity(this.order, this.size);
            this.order[this.size++] = value;
            return true;
        }
        return false;
    }

    public void clear() {
        this.points.clear();
        this.index = -1;
        this.size = 0;
    }

    public void shuffle(Random random) {
        LongArrays.shuffle((long[])this.order, (int)0, (int)this.size, (Random)random);
    }

    public boolean hasNext() {
        return this.index + 1 < this.size;
    }

    public long nextLong() {
        while (++this.index < this.size) {
            long value = this.order[this.index];
            if (value == Long.MAX_VALUE || !this.points.contains(value)) continue;
            return value;
        }
        return Long.MAX_VALUE;
    }

    public void remove() {
        long value = this.order[this.index];
        this.points.remove(value);
        this.order[this.index] = Long.MAX_VALUE;
    }

    public void reset() {
        this.index = -1;
    }

    private static long[] ensureCapacity(long[] backing, int index) {
        if (backing.length <= index) {
            long[] next = new long[backing.length << 1];
            System.arraycopy(backing, 0, next, 0, backing.length);
            return next;
        }
        return backing;
    }
}

