/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.cache;

public interface SafeCloseable extends AutoCloseable {
    public static final SafeCloseable NONE = () -> {};

    @Override
    public void close();
}

