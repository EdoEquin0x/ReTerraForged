/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.thread;

import java.util.concurrent.ThreadFactory;

public class SimpleThreadFactory implements ThreadFactory {
    private final String name;

    public SimpleThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(this.name);
        return thread;
    }
}

