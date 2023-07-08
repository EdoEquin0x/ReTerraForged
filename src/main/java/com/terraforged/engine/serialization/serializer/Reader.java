/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.serializer;

import java.util.Collection;

public interface Reader {
    public int getSize();

    public Reader getChild(String var1);

    public Reader getChild(int var1);

    public Collection<String> getKeys();

    public String getString();

    public boolean getBool();

    public float getFloat();

    public int getInt();

    default public boolean has(String key) {
        return this.getKeys().contains(key);
    }

    default public String getString(String key) {
        return this.getChild(key).getString();
    }

    default public boolean getBool(String key) {
        return this.getChild(key).getBool();
    }

    default public float getFloat(String key) {
        return this.getChild(key).getFloat();
    }

    default public int getInt(String key) {
        return this.getChild(key).getInt();
    }

    default public String getString(int index) {
        return this.getChild(index).getString();
    }

    default public boolean getBool(int index) {
        return this.getChild(index).getBool();
    }

    default public float getFloat(int index) {
        return this.getChild(index).getFloat();
    }

    default public int getInt(int index) {
        return this.getChild(index).getInt();
    }

    default public boolean writeTo(Object object) throws Throwable {
        return Deserializer.deserialize(this, object);
    }
}

