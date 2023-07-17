/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ThreadPool {
    public int size();

    public void shutdown();

    public void shutdownNow();

    default public boolean isManaged() {
        return false;
    }

    public Future<?> submit(Runnable runnable);

    public <T> Future<T> submit(Callable<T> callable);
}

