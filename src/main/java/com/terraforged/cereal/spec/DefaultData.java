/*
	 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.spec;

import java.util.List;
import java.util.function.Supplier;

import com.terraforged.cereal.value.DataValue;

public class DefaultData {
    private final Class<?> type;
    private final Supplier<DataValue> supplier;

    private DefaultData(Class<?> type, Supplier<DataValue> supplier) {
        this.type = type;
        this.supplier = supplier;
    }

    public DefaultData(Class<?> type, DataValue value) {
        this(type, () -> value);
    }

    public DefaultData(DataValue value) {
        this(() -> value);
    }

    public DefaultData(Supplier<DataValue> supplier) {
        this(Object.class, supplier);
    }

    public boolean hasSpec() {
        return this.type != Object.class;
    }

    public boolean hasValue() {
        return this.type == Object.class;
    }

    public List<DataSpec<?>> getSpecs() {
        return DataSpecs.getSpecs(this.type);
    }

    public DataValue getValue() {
        return this.supplier.get();
    }
}

