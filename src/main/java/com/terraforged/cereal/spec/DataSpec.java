/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.spec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.terraforged.cereal.Cereal;
import com.terraforged.cereal.value.DataList;
import com.terraforged.cereal.value.DataObject;
import com.terraforged.cereal.value.DataValue;

public class DataSpec<T> {
    private final String name;
    private final Class<T> type;
    private final DataFactory<T> constructor;
    private final Map<String, DefaultData> defaults;
    private final Map<String, DataAccessor<T, ?>> accessors;

    public DataSpec(Builder<T> builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.constructor = builder.constructor;
        this.defaults = Collections.unmodifiableMap(builder.defaults);
        this.accessors = Collections.unmodifiableMap(builder.accessors);
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getType() {
        return this.type;
    }

    public <V> V get(String key, DataObject holder, Function<DataValue, V> accessor) {
        return accessor.apply(this.getValue(key, holder));
    }

    public <V> V get(String key, DataObject holder, Class<V> type) {
        return this.get(key, holder, type, Context.NONE);
    }

    public <V> V get(String key, DataObject holder, Class<V> type, Context context) {
        DataObject value = holder.get(key).asObj();
        return Cereal.deserialize(value, type, context);
    }

    public <V extends Enum<V>> V getEnum(String key, DataObject holder, Class<V> type) {
        return Enum.valueOf(type, this.getValue(key, holder).asString());
    }

    public <V> List<V> getList(String key, DataObject holder, Class<V> type, Context context) {
        DataList list = holder.get(key).asList();
        return Cereal.deserialize(list, type, context);
    }

    public <V> Stream<V> getStream(String key, DataObject holder, Class<V> type, Context context) {
        return this.getList(key, holder, type, context).stream();
    }

    public DataValue serialize(Object value) {
        return this.serialize(value, Context.NONE);
    }

    public DataValue serialize(Object value, Context context) {
        if (this.getType().isInstance(value)) {
            boolean skipDefaults = context.skipDefaults();
            T t = this.getType().cast(value);
            DataObject root = new DataObject(this.name);
            for (Map.Entry<String, DataAccessor<T, ?>> e : this.accessors.entrySet()) {
                Object o = e.getValue().access(t, context);
                DataValue val = Cereal.serialize(o, context);
                if (skipDefaults && val.equals(this.getDefault(e.getKey()))) continue;
                root.add(e.getKey(), val);
            }
            return root;
        }
        return DataValue.NULL;
    }

    public T deserialize(DataObject data) {
        return this.deserialize(data, Context.NONE);
    }

    public <V> V deserialize(DataObject data, Class<V> type) {
        return this.deserialize(data, type, Context.NONE);
    }

    public T deserialize(DataObject data, Context context) {
        return this.constructor.create(data, this, context);
    }

    public <V> V deserialize(DataObject data, Class<V> type, Context context) {
        if (type.isAssignableFrom(this.getType())) {
            T t = this.deserialize(data, context);
            if (type.isInstance(t)) {
                return type.cast(t);
            }
            throw new RuntimeException("Invalid type: " + type + " for instance: " + t.getClass());
        }
        throw new RuntimeException("Invalid type: " + type);
    }

    public Map<String, DefaultData> getDefaults() {
        return this.defaults;
    }

    private DataValue getValue(String key, DataObject holder) {
        DataValue value = holder.get(key);
        if (value.isNonNull()) {
            return value;
        }
        return this.getDefault(key);
    }

    private DataValue getDefault(String name) {
        DefaultData data = this.defaults.get(name);
        if (data.hasValue()) {
            return data.getValue();
        }
        return DataValue.NULL;
    }

    public static <T> Builder<T> builder(Class<T> type, DataFactory<T> constructor) {
        return DataSpec.builder(type.getSimpleName(), type, constructor);
    }

    public static <T> Builder<T> builder(String name, Class<T> type, DataFactory<T> constructor) {
        return new Builder<T>(name, type, constructor);
    }

    public static class Builder<T> {
        private final String name;
        private final Class<T> type;
        private final DataFactory<T> constructor;
        private final Map<String, DefaultData> defaults = new LinkedHashMap<String, DefaultData>();
        private final Map<String, DataAccessor<T, ?>> accessors = new LinkedHashMap<>();

        public Builder(String name, Class<T> type, DataFactory<T> constructor) {
            this.name = name;
            this.type = type;
            this.constructor = constructor;
        }

        public <V> Builder<T> add(String key, Object value, Function<T, V> accessor) {
            if (value instanceof Enum<?> e) {
                return this.add(key, e.name(), (T t) -> ((Enum<?>) accessor.apply(t)).name());
            }
            return this.add(key, value, DataAccessor.wrap(accessor));
        }

        public <V> Builder<T> add(String key, Object value, DataAccessor<T, V> accessor) {
            this.accessors.put(key, accessor);
            this.defaults.put(key, new DefaultData(DataValue.lazy(value)));
            return this;
        }

        public <V> Builder<T> add(String key, DataValue value, Function<T, V> accessor) {
            return this.add(key, value, DataAccessor.wrap(accessor));
        }

        public <V> Builder<T> adds(String key, Class<V> superType, Function<T, V> accessor) {
            this.accessors.put(key, DataAccessor.wrap(accessor));
            return this;
        }

        public <V> Builder<T> add(String key, DataValue value, DataAccessor<T, V> accessor) {
            this.accessors.put(key, accessor);
            this.defaults.put(key, new DefaultData(value));
            return this;
        }

        public <V> Builder<T> addObj(String key, Function<T, V> accessor) {
            return this.addObj(key, DataAccessor.wrap(accessor));
        }

        public <V> Builder<T> addObj(String key, DataAccessor<T, V> accessor) {
            this.accessors.put(key, accessor);
            this.defaults.put(key, new DefaultData(DataObject.NULL_OBJ));
            return this;
        }

        public <V> Builder<T> addObj(String key, Class<V> type, Function<T, ? extends V> accessor) {
            return this.addObj(key, type, DataAccessor.wrap(accessor));
        }

        public <V> Builder<T> addObj(String key, Class<V> type, DataAccessor<T, ? extends V> accessor) {
            this.accessors.put(key, accessor);
            this.defaults.put(key, new DefaultData(type, DataObject.NULL_OBJ));
            return this;
        }

        public <V> Builder<T> addList(String key, Function<T, List<V>> accessor) {
            return this.addList(key, DataAccessor.wrap(accessor));
        }

        public <V> Builder<T> addList(String key, DataAccessor<T, List<V>> accessor) {
            this.accessors.put(key, accessor);
            this.defaults.put(key, new DefaultData(DataList.NULL_LIST));
            return this;
        }

        public <V> Builder<T> addList(String key, Class<V> type, Function<T, List<? extends V>> accessor) {
            return this.addList(key, type, DataAccessor.wrap(accessor));
        }

        public <V> Builder<T> addList(String key, Class<V> type, DataAccessor<T, List<? extends V>> accessor) {
            this.accessors.put(key, accessor);
            this.defaults.put(key, new DefaultData(type, DataList.NULL_LIST));
            return this;
        }

        public DataSpec<T> build() {
            Objects.requireNonNull(this.constructor, "constructor");
            return new DataSpec<>(this);
        }
    }
}

