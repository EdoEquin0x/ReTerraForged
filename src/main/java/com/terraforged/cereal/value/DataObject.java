/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.value;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.terraforged.cereal.serial.DataWriter;

public class DataObject extends DataValue implements Iterable<Map.Entry<String, DataValue>> {
    public static final DataObject NULL_OBJ = new DataObject("null", Collections.emptyMap(), false);
    private final String type;
    private final boolean nullable;
    private final Map<String, DataValue> data;

    protected DataObject(String type, Map<String, DataValue> data, boolean nullable) {
        super(data);
        this.type = type;
        this.data = data;
        this.nullable = nullable;
    }

    public DataObject() {
        this("");
    }

    public DataObject(String type) {
        this(type, new LinkedHashMap<String, DataValue>(), false);
    }

    public String getType() {
        return this.type;
    }

    public int size() {
        return this.data.size();
    }

    public boolean has(String key) {
        return this.data.containsKey(key);
    }

    public boolean contains(Object value) {
        for (DataValue v : this.data.values()) {
            if (!value.equals(v.value)) continue;
            return true;
        }
        return false;
    }

    public DataValue get(String key) {
        return this.data.getOrDefault(key, NULL);
    }

    public DataObject getObj(String key) {
        return this.get(key).asObj();
    }

    public DataList getList(String key) {
        return this.get(key).asList();
    }

    public DataObject add(String key, Object value) {
        return this.add(key, DataValue.of(value));
    }

    public DataObject add(String key, DataValue value) {
        if (value.isNonNull() || this.nullable) {
            this.data.put(key, value);
        }
        return this;
    }

    public DataValue remove(String key) {
        DataValue value = this.data.remove(key);
        if (value == null) {
            return DataValue.NULL;
        }
        return value;
    }

    public void forEach(BiConsumer<String, DataValue> consumer) {
        this.data.forEach(consumer);
    }

    public Map<String, DataValue> getBacking() {
        return this.data;
    }

    @Override
    public void appendTo(DataWriter writer) throws IOException {
        writer.type(this.type);
        writer.beginObj();
        for (Map.Entry<String, DataValue> entry : this.data.entrySet()) {
            writer.name(entry.getKey());
            writer.value(entry.getValue());
        }
        writer.endObj();
    }

    @Override
    public Iterator<Map.Entry<String, DataValue>> iterator() {
        return this.data.entrySet().iterator();
    }
}

