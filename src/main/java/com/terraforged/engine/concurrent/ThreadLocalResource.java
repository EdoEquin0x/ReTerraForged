/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ThreadLocalResource<T> extends ThreadLocal<Resource<T>> {
    private final Supplier<Resource<T>> supplier;

    private ThreadLocalResource(Supplier<Resource<T>> supplier) {
        this.supplier = supplier;
    }

    public T open() {
        return ((Resource<T>)this.get()).get();
    }

    public void close() {
        ((Resource<T>)this.get()).close();
    }

    @Override
    protected Resource<T> initialValue() {
        return this.supplier.get();
    }

    public static <T> ThreadLocalResource<T> withInitial(Supplier<T> supplier, Consumer<T> consumer) {
        return new ThreadLocalResource<T>(() -> new SimpleResource<>(supplier.get(), consumer));
    }
}

