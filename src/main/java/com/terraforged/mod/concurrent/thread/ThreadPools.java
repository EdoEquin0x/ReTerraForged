/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent.thread;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThreadPools {
    public static final ThreadPool NONE = new EmptyThreadPool();
    private static final Object lock = new Object();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory("TF-Scheduler"));
    private static WeakReference<ThreadPool> instance = new WeakReference<>(null);

    public static ThreadPool createDefault() {
        return ThreadPools.create(ThreadPools.defaultPoolSize());
    }

    public static ThreadPool create(int poolSize) {
        return ThreadPools.create(poolSize, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ThreadPool create(int poolSize, boolean keepAlive) {
        Object object = lock;
        synchronized (object) {
            ThreadPool next;
            ThreadPool current = (ThreadPool)instance.get();
            if (current != null && current.isManaged()) {
                if (poolSize == current.size()) {
                    return current;
                }
                current.shutdown();
            }
            if ((next = BatchingThreadPool.of(poolSize, !keepAlive)).isManaged()) {
                instance = new WeakReference<ThreadPool>(next);
            }
            return next;
        }
    }

    public static int defaultPoolSize() {
        return Math.max(2, Runtime.getRuntime().availableProcessors());
    }

    public static void scheduleDelayed(Runnable runnable, long delayMS) {
        scheduler.schedule(runnable, delayMS, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> scheduleRepeat(Runnable runnable, long intervalMS) {
        return scheduler.scheduleAtFixedRate(runnable, intervalMS, intervalMS, TimeUnit.MILLISECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void markShutdown(ThreadPool threadPool) {
        Object object = lock;
        synchronized (object) {
            if (threadPool == instance.get()) {
                instance.clear();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void shutdownAll() {
        scheduler.shutdownNow();
        Object object = lock;
        synchronized (object) {
            ThreadPool pool = (ThreadPool)instance.get();
            if (pool != null) {
                pool.shutdown();
                instance.clear();
            }
        }
    }
}

