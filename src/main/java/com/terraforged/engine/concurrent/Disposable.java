/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent;

public interface Disposable {
    public void dispose();

    public static interface Listener<T> {
        public void onDispose(T var1);
    }
}

