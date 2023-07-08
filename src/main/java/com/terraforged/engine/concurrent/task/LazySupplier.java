/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.task;

import java.util.function.Function;
import java.util.function.Supplier;

public class LazySupplier<T> extends LazyCallable<T> {
    private final Supplier<T> supplier;

    public LazySupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected T create() {
        return this.supplier.get();
    }

    public <V> LazySupplier<V> then(Function<T, V> mapper) {
        return LazySupplier.supplied(this, mapper);
    }

    public static <T> LazySupplier<T> of(Supplier<T> supplier) {
        return new LazySupplier<T>(supplier);
    }

    public static <V, T> LazySupplier<T> factory(V value, Function<V, T> function) {
        return LazySupplier.of(() -> function.apply(value));
    }

    public static <V, T> LazySupplier<T> supplied(Supplier<V> supplier, Function<V, T> function) {
        return LazySupplier.of(() -> function.apply(supplier.get()));
    }
}

