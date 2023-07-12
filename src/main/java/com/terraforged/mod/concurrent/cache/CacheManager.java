/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.terraforged.mod.concurrent.thread.ThreadPools;

public class CacheManager {
    private static final CacheManager INSTANCE = new CacheManager();
    private final List<ScheduledFuture<?>> cacheTasks = new ArrayList<>();

    private CacheManager() {
    }

    public synchronized void schedule(Cache<?> cache, long intervalMS) {
        this.cacheTasks.add(ThreadPools.scheduleRepeat(cache, intervalMS));
    }

    public synchronized void clear() {
        if (this.cacheTasks.isEmpty()) {
            return;
        }
        for (ScheduledFuture<?> task : this.cacheTasks) {
            try {
                task.cancel(false);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        this.cacheTasks.clear();
    }

    public static CacheManager get() {
        return INSTANCE;
    }
}

