/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent;

import com.terraforged.mod.concurrent.cache.SafeCloseable;

public interface Resource<T> extends SafeCloseable {
    public static final Resource<?> NONE = new Resource<>() {

        public Object get() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public void close() {
        }
    };

    public T get();

    public boolean isOpen();

    @SuppressWarnings("unchecked")
	public static <T> Resource<T> empty() {
        return (Resource<T>) NONE;
    }
}

