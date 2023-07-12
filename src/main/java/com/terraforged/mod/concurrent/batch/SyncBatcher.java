/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent.batch;

import com.terraforged.mod.concurrent.Resource;

public class SyncBatcher
implements Batcher,
Resource<Batcher> {
    @Override
    public void size(int size) {
    }

    @Override
    public void submit(Runnable task) {
        task.run();
    }

    @Override
    public Batcher get() {
        return this;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() {
    }
}

