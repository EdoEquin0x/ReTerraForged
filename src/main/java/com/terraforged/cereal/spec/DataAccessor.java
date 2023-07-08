/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.spec;

import java.util.function.Function;

public interface DataAccessor<T, V> {
    public V access(T var1, Context var2);

    public static <T, V> DataAccessor<T, V> wrap(Function<T, V> func) {
        return (owner, context) -> func.apply(owner);
    }
}

