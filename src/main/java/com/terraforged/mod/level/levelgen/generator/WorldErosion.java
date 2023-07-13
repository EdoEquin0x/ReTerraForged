/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator;

import java.util.concurrent.locks.StampedLock;
import java.util.function.IntFunction;

import com.terraforged.mod.level.levelgen.filter.Erosion;

public class WorldErosion {
    private volatile Erosion value = null;
    private final IntFunction<Erosion> factory;
    private final StampedLock lock = new StampedLock();

    public WorldErosion(GeneratorContext ctx) {
    	this.factory = Erosion.factory(ctx);
    }

    public Erosion get(int size) {
    	Erosion value = this.readValue();
        if (this.validate(value, size)) {
            return value;
        }
        return this.writeValue(size);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Erosion readValue() {
        long optRead = this.lock.tryOptimisticRead();
        Erosion value = this.value;
        if (!this.lock.validate(optRead)) {
            long stamp = this.lock.readLock();
            try {
            	Erosion t = this.value;
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
    private Erosion writeValue(int size) {
        long stamp = this.lock.writeLock();
        try {
            if (this.validate(this.value, size)) {
            	Erosion t = this.value;
                return t;
            }
            this.value = this.factory.apply(size);
            Erosion t = this.value;
            return t;
        }
        finally {
            this.lock.unlockWrite(stamp);
        }
    }

    private boolean validate(Erosion value, int size) {
        return value != null && value.getSize() == size;
    }
}

