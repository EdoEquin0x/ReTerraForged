/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.pool;

import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.cache.SafeCloseable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final int capacity;
    private final List<Item<T>> pool;
    private final Object lock = new Object();
    private final Supplier<? extends T> supplier;

    public ObjectPool(int size, Supplier<? extends T> supplier) {
        this.capacity = size;
        this.pool = new ArrayList<Item<T>>(size);
        this.supplier = supplier;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Resource<T> get() {
        Object object = this.lock;
        synchronized (object) {
            if (this.pool.size() > 0) {
                return this.pool.remove(this.pool.size() - 1).retain();
            }
        }
        return new Item<>(this.supplier.get(), this);
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

    public static class Item<T>
    implements Resource<T> {
        private final T value;
        private final ObjectPool<T> pool;
        private boolean released = false;

        private Item(T value, ObjectPool<T> pool) {
            this.value = value;
            this.pool = pool;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public boolean isOpen() {
            return !this.released;
        }

        @Override
        public void close() {
            if (this.value instanceof SafeCloseable) {
                ((SafeCloseable)this.value).close();
            }
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

