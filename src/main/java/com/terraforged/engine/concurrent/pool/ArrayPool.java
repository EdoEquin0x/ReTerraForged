/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.pool;

import com.terraforged.engine.concurrent.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ArrayPool<T> {
    private final int capacity;
    private final IntFunction<T[]> constructor;
    private final List<Item<T>> pool;
    private final Object lock = new Object();

    public ArrayPool(int size, IntFunction<T[]> constructor) {
        this.capacity = size;
        this.constructor = constructor;
        this.pool = new ArrayList<Item<T>>(size);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Resource<T[]> get(int arraySize) {
        Object object = this.lock;
        synchronized (object) {
            Item<T> resource;
            if (this.pool.size() > 0 && (resource = this.pool.remove(this.pool.size() - 1)).get().length >= arraySize) {
                return resource.retain();
            }
        }
        return new Item<>(this.constructor.apply(arraySize), this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean restore(Item<T> item) {
        Object object = this.lock;
        synchronized (object) {
            if (this.pool.size() < this.capacity) {
                this.pool.add(item);
                return true;
            }
        }
        return false;
    }

    public static <T> ArrayPool<T> of(int size, IntFunction<T[]> constructor) {
        return new ArrayPool<T>(size, constructor);
    }

    public static <T> ArrayPool<T> of(int size, Supplier<T> supplier, IntFunction<T[]> constructor) {
        return new ArrayPool<T>(size, new ArrayConstructor<>(supplier, constructor));
    }

    private static class ArrayConstructor<T>
    implements IntFunction<T[]> {
        private final Supplier<T> element;
        private final IntFunction<T[]> array;

        private ArrayConstructor(Supplier<T> element, IntFunction<T[]> array) {
            this.element = element;
            this.array = array;
        }

        @Override
        public T[] apply(int size) {
            T[] t = this.array.apply(size);
            for (int i = 0; i < t.length; ++i) {
                t[i] = this.element.get();
            }
            return t;
        }
    }

    public static class Item<T>
    implements Resource<T[]> {
        private final T[] value;
        private final ArrayPool<T> pool;
        private boolean released = false;

        private Item(T[] value, ArrayPool<T> pool) {
            this.value = value;
            this.pool = pool;
        }

        @Override
        public T[] get() {
            return this.value;
        }

        @Override
        public boolean isOpen() {
            return !this.released;
        }

        @Override
        public void close() {
            if (!this.released) {
                this.released = true;
                this.released = this.pool.restore(this);
            }
        }

        private Item<T> retain() {
            this.released = false;
            return this;
        }
    }
}

