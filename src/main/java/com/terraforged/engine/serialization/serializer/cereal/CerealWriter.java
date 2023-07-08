/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.serializer.cereal;

import com.terraforged.cereal.value.DataList;
import com.terraforged.cereal.value.DataObject;
import com.terraforged.cereal.value.DataValue;
import com.terraforged.engine.serialization.serializer.AbstractWriter;

public class CerealWriter extends AbstractWriter<DataValue, DataObject, DataList, CerealWriter> {
    @Override
    protected CerealWriter self() {
        return this;
    }

    @Override
    protected boolean isObject(DataValue value) {
        return value.isObj();
    }

    @Override
    protected boolean isArray(DataValue value) {
        return value.isList();
    }

    @Override
    protected void add(DataObject parent, String key, DataValue value) {
        parent.add(key, value);
    }

    @Override
    protected void add(DataList parent, DataValue value) {
        parent.add(value);
    }

    @Override
    protected DataObject createObject() {
        return new DataObject();
    }

    @Override
    protected DataList createArray() {
        return new DataList();
    }

    @Override
    protected DataValue closeObject(DataObject o) {
        return o;
    }

    @Override
    protected DataValue closeArray(DataList a) {
        return a;
    }

    @Override
    protected DataValue create(String value) {
        return DataValue.of(value);
    }

    @Override
    protected DataValue create(int value) {
        return DataValue.of(value);
    }

    @Override
    protected DataValue create(float value) {
        return DataValue.of(Float.valueOf(value));
    }

    @Override
    protected DataValue create(boolean value) {
        return DataValue.of(value);
    }
}

