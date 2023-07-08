/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.batch.Batcher;
import com.terraforged.engine.concurrent.batch.SyncBatcher;
import com.terraforged.engine.concurrent.task.LazyCallable;

public class EmptyThreadPool implements ThreadPool {
    private final ThreadLocal<SyncBatcher> batcher = ThreadLocal.withInitial(SyncBatcher::new);

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return LazyCallable.adapt(runnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return LazyCallable.adaptComplete(callable);
    }

    @Override
    public void shutdown() {
        ThreadPools.markShutdown(this);
    }

    @Override
    public void shutdownNow() {
    }

    @Override
    public Resource<Batcher> batcher() {
        return this.batcher.get();
    }
}

