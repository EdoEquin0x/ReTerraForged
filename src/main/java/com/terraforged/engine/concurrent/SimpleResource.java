/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent;

import java.util.function.Consumer;

public class SimpleResource<T> implements Resource<T> {
    private final T value;
    private final Consumer<T> closer;
    private boolean open = false;

    public SimpleResource(T value, Consumer<T> closer) {
        this.value = value;
        this.closer = closer;
    }

    @Override
    public T get() {
        this.open = true;
        return this.value;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public void close() {
        if (this.open) {
            this.open = false;
            this.closer.accept(this.value);
        }
    }
}

