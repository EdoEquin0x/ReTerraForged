/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.thread;

import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.batch.Batcher;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ThreadPool {
    public int size();

    public void shutdown();

    public void shutdownNow();

    default public boolean isManaged() {
        return false;
    }

    public Future<?> submit(Runnable var1);

    public <T> Future<T> submit(Callable<T> var1);

    public Resource<Batcher> batcher();
}

