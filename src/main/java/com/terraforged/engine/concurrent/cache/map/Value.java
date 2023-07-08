/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.cache.map;

public class Value {
    public final int id;

    public Value(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Value value = (Value)o;
        return this.id == value.id;
    }

    public int hashCode() {
        return this.id;
    }
}

