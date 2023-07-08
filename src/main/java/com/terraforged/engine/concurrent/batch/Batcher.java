/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.batch;

import com.terraforged.engine.concurrent.cache.SafeCloseable;

public interface Batcher extends SafeCloseable {
    public void size(int var1);

    public void submit(Runnable var1);

    default public void submit(BatchTask task) {
        this.submit((Runnable)task);
    }
}

