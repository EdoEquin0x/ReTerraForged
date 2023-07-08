/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.terraforged.cereal.serial.DataWriter;

public class DataList extends DataValue implements Iterable<DataValue> {
    public static final DataList NULL_LIST = new DataList(Collections.emptyList(), false);
    private final boolean nullable;
    private final List<DataValue> data;

    protected DataList(List<DataValue> data, boolean nullable) {
        super(data);
        this.data = data;
        this.nullable = nullable;
    }

    public DataList() {
        this(false);
    }

    public DataList(int size) {
        this(size, false);
    }

    public DataList(boolean nullable) {
        this(16, nullable);
    }

    public DataList(int size, boolean nullable) {
        this(new ArrayList<DataValue>(size), nullable);
    }

    public int size() {
        return this.data.size();
    }

    public boolean contains(Object value) {
        for (DataValue v : this) {
            if (!value.equals(v.value)) continue;
            return true;
        }
        return false;
    }

    public DataValue get(int index) {
        DataValue value;
        if (index < this.data.size() && (value = this.data.get(index)) != null) {
            return value;
        }
        return DataValue.NULL;
    }

    public DataObject getObj(int index) {
        return this.get(index).asObj();
    }

    public DataList getList(int index) {
        return this.get(index).asList();
    }

    public DataList add(Object value) {
        return this.add(DataValue.of(value));
    }

    public DataList add(DataValue value) {
        if (value.isNonNull() || this.nullable) {
            this.data.add(value);
        }
        return this;
    }

    public DataValue set(int index, Object value) {
        return this.set(index, DataValue.of(value));
    }

    public DataValue set(int index, DataValue value) {
        DataValue removed;
        if ((value.isNonNull() || this.nullable) && (removed = this.data.set(index, value)) != null) {
            return removed;
        }
        return DataValue.NULL;
    }

    public DataValue remove(int index) {
        DataValue value;
        if (index < this.size() && (value = this.data.remove(index)) != null) {
            return value;
        }
        return DataValue.NULL;
    }

    public List<DataValue> getBacking() {
        return this.data;
    }

    @Override
    public void appendTo(DataWriter writer) throws IOException {
        writer.beginList();
        for (DataValue value : this.data) {
            writer.value(value);
        }
        writer.endList();
    }

    @Override
    public Iterator<DataValue> iterator() {
        return this.data.iterator();
    }
}

