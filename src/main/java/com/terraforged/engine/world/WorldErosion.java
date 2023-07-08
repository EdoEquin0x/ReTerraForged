/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world;

import java.util.concurrent.locks.StampedLock;
import java.util.function.IntFunction;

public class WorldErosion<T> {
    private volatile T value = null;
    private final IntFunction<T> factory;
    private final Validator<T> validator;
    private final StampedLock lock = new StampedLock();

    public WorldErosion(IntFunction<T> factory, Validator<T> validator) {
        this.factory = factory;
        this.validator = validator;
    }

    public T get(int ctx) {
        T value = this.readValue();
        if (this.validate(value, ctx)) {
            return value;
        }
        return this.writeValue(ctx);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private T readValue() {
        long optRead = this.lock.tryOptimisticRead();
        T value = this.value;
        if (!this.lock.validate(optRead)) {
            long stamp = this.lock.readLock();
            try {
                T t = this.value;
                return t;
            }
            finally {
                this.lock.unlockRead(stamp);
            }
        }
        return value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private T writeValue(int ctx) {
        long stamp = this.lock.writeLock();
        try {
            if (this.validate(this.value, ctx)) {
                T t = this.value;
                return t;
            }
            this.value = this.factory.apply(ctx);
            T t = this.value;
            return t;
        }
        finally {
            this.lock.unlockWrite(stamp);
        }
    }

    private boolean validate(T value, int ctx) {
        return value != null && this.validator.validate(value, ctx);
    }

    public static interface Validator<T> {
        public boolean validate(T var1, int var2);
    }
}

