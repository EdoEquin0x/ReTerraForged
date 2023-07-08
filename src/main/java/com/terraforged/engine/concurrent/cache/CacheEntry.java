/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.function.Function;

import com.terraforged.engine.concurrent.task.LazyCallable;
import com.terraforged.engine.concurrent.thread.ThreadPool;

public class CacheEntry<T> extends LazyCallable<T> implements ExpiringEntry {
    private volatile long timestamp;
    private final Future<T> task;

    public CacheEntry(Future<T> task) {
        this.task = task;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public T get() {
        this.timestamp = System.currentTimeMillis();
        return super.get();
    }

    @Override
    public boolean isDone() {
        return this.task.isDone();
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public void close() {
        if (this.value instanceof SafeCloseable) {
            ((SafeCloseable)this.value).close();
            return;
        }
        if (this.value instanceof AutoCloseable) {
            try {
                ((AutoCloseable)this.value).close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected T create() {
        if (this.task instanceof ForkJoinTask<T> task) {
            return task.join();
        }
        try {
            return this.task.get();
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public <V> CacheEntry<V> then(ThreadPool executor, Function<T, V> function) {
        return CacheEntry.computeAsync(() -> function.apply(this.get()), executor);
    }

    public static <T> CacheEntry<T> supply(Future<T> task) {
        return new CacheEntry<T>(task);
    }

    public static <T> CacheEntry<T> computeAsync(Callable<T> callable, ThreadPool executor) {
        return new CacheEntry<T>(executor.submit(callable));
    }
}

