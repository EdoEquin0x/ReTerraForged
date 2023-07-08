/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.serializer;

public interface Writer {
    public Writer name(String var1);

    public Writer beginObject();

    public Writer endObject();

    public Writer beginArray();

    public Writer endArray();

    public Writer value(String var1);

    public Writer value(float var1);

    public Writer value(int var1);

    public Writer value(boolean var1);

    default public void readFrom(Object value) throws IllegalAccessException {
        new Serializer();
        Serializer.serialize(value, this);
    }
}

