/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.batch;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ForkJoinBatcher
implements Batcher {
    private static final ForkJoinTask<?>[] empty = new ForkJoinTask[0];
    private final ForkJoinPool pool;
    private int size = 0;
    private int count = 0;
    private ForkJoinTask<?>[] tasks = empty;

    public ForkJoinBatcher(ForkJoinPool pool) {
        this.pool = pool;
    }

    @Override
    public void size(int newSize) {
        if (this.tasks.length < newSize) {
            this.count = 0;
            this.size = newSize;
            this.tasks = new ForkJoinTask[newSize];
        }
    }

    @Override
    public void submit(Runnable task) {
        if (this.count < this.size) {
            this.tasks[this.count++] = this.pool.submit(task);
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < this.size; ++i) {
            this.tasks[i].quietlyJoin();
            this.tasks[i] = null;
        }
    }
}

