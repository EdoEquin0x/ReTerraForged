/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.terraforged.cereal.serial.DataWriter;
import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpecs;

public class DataValue {
    public static final DataValue NULL = new DataValue(null);
    protected final Object value;

    protected DataValue(Object value) {
        this.value = value;
    }

    public boolean isObj() {
        return this instanceof DataObject;
    }

    public boolean isList() {
        return this instanceof DataList;
    }

    public boolean isNull() {
        return this == NULL;
    }

    public boolean isNonNull() {
        return !this.isNull();
    }

    public boolean isNum() {
        return this.value instanceof Number;
    }

    public boolean isString() {
        return this.value instanceof String;
    }

    public boolean isBool() {
        return this.value instanceof Boolean;
    }

    public boolean isEnum() {
        return this.value instanceof Enum;
    }

    public Number asNum() {
        return this.value instanceof Number ? (Number)((Number)this.value) : (Number)0;
    }

    public DataValue inc(int amount) {
        return new DataValue(this.asInt() + amount);
    }

    public DataValue inc(double amount) {
        return new DataValue(this.asDouble() + amount);
    }

    public byte asByte() {
        return this.asNum().byteValue();
    }

    public int asInt() {
        return this.asNum().intValue();
    }

    public short aShort() {
        return this.asNum().shortValue();
    }

    public long asLong() {
        return this.asNum().longValue();
    }

    public float asFloat() {
        return this.asNum().floatValue();
    }

    public double asDouble() {
        return this.asNum().doubleValue();
    }

    public boolean asBool() {
        if (this.value instanceof Boolean) {
            return (Boolean)this.value;
        }
        if (this.value instanceof String) {
            return this.value.toString().equalsIgnoreCase("true");
        }
        return this.asNum().byteValue() == 1;
    }

    public String asString() {
        return this.value == null ? "null" : this.value.toString();
    }

    public <E extends Enum<E>> E asEnum(Class<E> type) {
        E[] values;
        int ordinal;
        if (type.isInstance(this.value)) {
            return type.cast(this.value);
        }
        if (this.isString()) {
            return Enum.valueOf(type, this.asString());
        }
        if (this.isNum() && (ordinal = this.asInt()) < (values = type.getEnumConstants()).length) {
            return values[ordinal];
        }
        throw new IllegalArgumentException("Value is not an Enum");
    }

    public DataList asList() {
        return this instanceof DataList ? (DataList)this : DataList.NULL_LIST;
    }

    public DataObject asObj() {
        return this instanceof DataObject ? (DataObject)this : DataObject.NULL_OBJ;
    }

    public DataList toList() {
        return this instanceof DataList ? (DataList)this : new DataList().add(this);
    }

    public <T> T map(Function<DataValue, T> mapper) {
        return mapper.apply(this);
    }

    public <T> Optional<T> map(Predicate<DataValue> predicate, Function<DataValue, T> mapper) {
        return Optional.of(this).filter(predicate).map(mapper);
    }

    public void appendTo(DataWriter writer) throws IOException {
        writer.value(this.value);
    }

    public int hashCode() {
        return this.value == null ? -1 : this.value.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DataValue value1 = (DataValue)o;
        return Objects.equals(this.value, value1.value);
    }

    public String toString() {
        return this.asString();
    }

    public boolean matchesType(DataValue other) {
        if (this.getClass() != other.getClass()) {
            return false;
        }
        if (this.value == null) {
            return other.value == null;
        }
        return other.value != null && this.value.getClass() == other.value.getClass();
    }

    public static DataValue of(Object value) {
        return DataValue.of(value, Context.NONE);
    }

    public static DataValue of(Object value, Context context) {
        String name;
        if (value instanceof DataValue) {
            return (DataValue)value;
        }
        if (value instanceof Number) {
            return new DataValue(value);
        }
        if (value instanceof String) {
            return new DataValue(value);
        }
        if (value instanceof Boolean) {
            return new DataValue(value);
        }
        if (value instanceof Enum) {
            return new DataValue(value);
        }
        if (value instanceof List<?> list) {
            DataList data = new DataList(list.size());
            for (Object o : list) {
                data.add(DataValue.of(o, context));
            }
            return data;
        }
        if (value instanceof Map<?, ?> map) {
            DataObject data = new DataObject();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                data.add(e.getKey().toString(), DataValue.of(e.getValue(), context));
            }
            return data;
        }
        if (value != null && DataSpecs.hasSpec(name = value.getClass().getSimpleName())) {
            return DataSpecs.getSpec(name).serialize(value, context);
        }
        return NULL;
    }

    public static Supplier<DataValue> lazy(final Object value) {
        return new Supplier<DataValue>() {
            private final Object val;
            private DataValue data;
            {
                this.val = value;
                this.data = null;
            }

            @Override
            public DataValue get() {
                if (this.data == null) {
                    this.data = DataValue.of(this.val);
                }
                return this.data;
            }
        };
    }
}

