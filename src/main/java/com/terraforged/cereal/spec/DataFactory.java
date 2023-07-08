/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.spec;

import com.terraforged.cereal.value.DataObject;

public interface DataFactory<T> {
    public T create(DataObject var1, DataSpec<T> var2, Context var3);
}

