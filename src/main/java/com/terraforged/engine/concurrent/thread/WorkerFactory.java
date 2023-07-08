/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerFactory implements ThreadFactory {
    protected final String prefix;
    protected final ThreadGroup group;
    protected final AtomicInteger threadNumber = new AtomicInteger(1);

    public WorkerFactory(String name) {
        this.group = Thread.currentThread().getThreadGroup();
        this.prefix = name + "-Worker-";
    }

    @Override
    public Thread newThread(Runnable task) {
        Thread thread = new Thread(this.group, task);
        thread.setDaemon(true);
        thread.setName(this.prefix + this.threadNumber.getAndIncrement());
        return thread;
    }
}

