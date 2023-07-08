/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.cache;

public interface ExpiringEntry {
    public long getTimestamp();

    default public void close() {
    }
}

