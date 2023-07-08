/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.spec;

import com.terraforged.cereal.value.DataObject;

public class Context {
    public static final Context NONE = new Context(DataObject.NULL_OBJ);
    private final DataObject data;

    public Context() {
        this(new DataObject());
    }

    public Context(DataObject data) {
        this.data = data;
    }

    public Context skipDefaultValues() {
        this.data.add("skip_defaults", true);
        return this;
    }

    public boolean skipDefaults() {
        return this.data.get("skip_defaults").asBool();
    }

    public DataObject getData() {
        return this.data;
    }
}

