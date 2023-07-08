/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util.metric;

import java.util.concurrent.atomic.AtomicLong;

public class Metrics {
    public static final Metric BATCHER = new Metric();
    public static final Metric HEIGHTMAP = new Metric();
    public static final Metric RIVER_GEN = new Metric();
    private static final AtomicLong timer = new AtomicLong(System.currentTimeMillis());

    public static void print() {
        long now = System.currentTimeMillis();
        if (now - timer.get() > 5000L) {
            timer.set(now);
            System.out.println("Heightmap: " + HEIGHTMAP.average());
            System.out.println("River Gen: " + RIVER_GEN.average());
            System.out.println("Batching:  " + BATCHER.average());
        }
    }
}

