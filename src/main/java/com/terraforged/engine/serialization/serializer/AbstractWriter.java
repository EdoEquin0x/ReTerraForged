/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.serializer;

public abstract class AbstractWriter<T, O, A, S extends AbstractWriter<T, ?, ?, ?>> implements Writer {
    private final Context root = new Context(null);
    private String name = "";
    private Context context = this.root;

    public T get() {
        return (T)this.root.build();
    }

    public S name(String name) {
        this.name = name;
        return this.self();
    }

    public S beginObject() {
        return this.begin(this.createObject(), null);
    }

    public S endObject() {
        return this.end();
    }

    public S beginArray() {
        return this.begin(null, this.createArray());
    }

    public S endArray() {
        return this.end();
    }

    public S value(String value) {
        return this.append(this.create(value));
    }

    public S value(float value) {
        return this.append(this.create(value));
    }

    public S value(int value) {
        return this.append(this.create(value));
    }

    public S value(boolean value) {
        return this.append(this.create(value));
    }

    private S begin(O object, A array) {
        if (this.root.isPresent()) {
            this.context = new Context(this.context);
            this.context.set(this.name, object, array);
        } else {
            this.root.set(this.name, object, array);
        }
        return this.self();
    }

    private S end() {
        if (this.context != this.root && this.context.isPresent()) {
            String name = this.context.name;
            T value = this.context.build();
            this.context = this.context.parent;
            this.append(name, value);
        }
        return this.self();
    }

    private S append(T value) {
        return this.append(this.name, value);
    }

    private S append(String name, T value) {
        if (this.context.objectValue != null) {
            this.add(this.context.objectValue, name, value);
        } else if (this.context.arrayValue != null) {
            this.add(this.context.arrayValue, value);
        }
        return this.self();
    }

    protected abstract S self();

    protected abstract boolean isObject(T var1);

    protected abstract boolean isArray(T var1);

    protected abstract void add(O var1, String var2, T var3);

    protected abstract void add(A var1, T var2);

    protected abstract O createObject();

    protected abstract A createArray();

    protected abstract T closeObject(O var1);

    protected abstract T closeArray(A var1);

    protected abstract T create(String var1);

    protected abstract T create(int var1);

    protected abstract T create(float var1);

    protected abstract T create(boolean var1);

    private class Context {
        private final Context parent;
        private String name;
        private O objectValue;
        private A arrayValue;

        private Context(Context root) {
            this.parent = root != null ? root : this;
        }

        private T build() {
            if (this.objectValue != null) {
                return AbstractWriter.this.closeObject(this.objectValue);
            }
            if (this.arrayValue != null) {
                return AbstractWriter.this.closeArray(this.arrayValue);
            }
            return null;
        }

        private void set(String n, O o, A a) {
            this.name = n;
            this.objectValue = o;
            this.arrayValue = a;
        }

        private boolean isPresent() {
            return this.objectValue != null || this.arrayValue != null;
        }
    }
}

