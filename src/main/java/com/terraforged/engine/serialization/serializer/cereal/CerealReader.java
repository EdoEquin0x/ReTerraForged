/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.serializer.cereal;

import com.terraforged.cereal.value.DataValue;
import com.terraforged.engine.serialization.serializer.Reader;
import java.util.Collection;

public class CerealReader implements Reader {
    private final DataValue value;

    public CerealReader(DataValue value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        if (this.value.isObj()) {
            return this.value.asObj().size();
        }
        if (this.value.isList()) {
            return this.value.asList().size();
        }
        return 0;
    }

    @Override
    public Reader getChild(String key) {
        return new CerealReader(this.value.asObj().get(key));
    }

    @Override
    public Reader getChild(int index) {
        return new CerealReader(this.value.asList().get(index));
    }

    @Override
    public Collection<String> getKeys() {
        return this.value.asObj().getBacking().keySet();
    }

    @Override
    public String getString() {
        return this.value.asString();
    }

    @Override
    public boolean getBool() {
        return this.value.asBool();
    }

    @Override
    public float getFloat() {
        return this.value.asFloat();
    }

    @Override
    public int getInt() {
        return this.value.asInt();
    }
}

