/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.terraforged.cereal.serial.DataReader;
import com.terraforged.cereal.serial.DataWriter;
import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpecs;
import com.terraforged.cereal.spec.SpecName;
import com.terraforged.cereal.spec.SubSpec;
import com.terraforged.cereal.value.DataList;
import com.terraforged.cereal.value.DataObject;
import com.terraforged.cereal.value.DataValue;

public class Cereal {

	@SuppressWarnings("resource")
    public static <T> T read(Reader reader, Class<T> type) throws IOException {
		DataValue data = new DataReader(reader).read();
        return Cereal.deserialize(data.asObj(), type, Context.NONE);
    }

	@SuppressWarnings("resource")
    public static <T> T read(Reader reader, Class<T> type, Context context) throws IOException {
        DataValue data = new DataReader(reader).read();
        return Cereal.deserialize(data.asObj(), type, context);
    }

	@SuppressWarnings("resource")
    public static <T> List<T> readList(Reader reader, Class<T> type) throws IOException {
        DataValue data = new DataReader(reader).read();
        return Cereal.deserialize(data.asList(), type, Context.NONE);
    }

	@SuppressWarnings("resource")
    public static <T> List<T> readList(Reader reader, Class<T> type, Context context) throws IOException {
        DataValue data = new DataReader(reader).read();
        return Cereal.deserialize(data.asList(), type, context);
    }

    public static void write(Object object, Writer writer) throws IOException {
        Cereal.write(object, writer, Context.NONE);
    }

    @SuppressWarnings("resource")
    public static void write(Object object, Writer writer, Context context) throws IOException {
        DataWriter dataWriter = new DataWriter(writer);
        DataValue value = Cereal.serialize(object, context);
        dataWriter.write(value);
    }

    public static void write(Object object, String type, Writer writer) throws IOException {
        Cereal.write(object, type, writer, Context.NONE);
    }

    @SuppressWarnings("resource")
    public static void write(Object object, String type, Writer writer, Context context) throws IOException {
        DataWriter dataWriter = new DataWriter(writer);
        DataValue value = Cereal.serialize(type, object, context);
        dataWriter.write(value);
    }

    public static DataValue serialize(Object value) {
        return Cereal.serialize(value, Context.NONE);
    }

    public static DataValue serialize(Object value, Context context) {
        return Cereal.serializeInferred(value, context);
    }

    public static DataValue serialize(String type, Object value) {
        return Cereal.serialize(type, value, Context.NONE);
    }

    public static <V> DataValue serialize(String type, V value, Context context) {
        if (DataSpecs.hasSpec(type)) {
            return DataSpecs.getSpec(type).serialize(value, context);
        }
        if (DataSpecs.isSubSpec(value)) {
            @SuppressWarnings("unchecked")
			SubSpec<V> spec = (SubSpec<V>) DataSpecs.getSubSpec(value);
            return spec.serialize(value, context);
        }
        return DataValue.of(value, context);
    }

    private static <V> DataValue serializeInferred(V value, Context context) {
        String name;
        if (value.getClass().isArray()) {
            int size = Array.getLength(value);
            DataList list = new DataList();
            for (int i = 0; i < size; ++i) {
                list.add(Cereal.serializeInferred(Array.get(value, i), context));
            }
            return list;
        }
        if (value instanceof Iterable) {
            DataList list = new DataList();
            for (Object child : (Iterable<?>) value) {
                list.add(Cereal.serializeInferred(child, context));
            }
            return list;
        }
        if (value instanceof Map) {
            DataObject object = new DataObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>)value).entrySet()) {
                if (!(entry.getKey() instanceof String)) continue;
                String key = entry.getKey().toString();
                DataValue child = Cereal.serializeInferred(entry.getValue(), context);
                object.add(key, child);
            }
            return object;
        }
        if (value instanceof SpecName && DataSpecs.hasSpec(name = ((SpecName)value).getSpecName())) {
            return DataSpecs.getSpec(name).serialize(value, context);
        }
        if (DataSpecs.isSubSpec(value)) {
            @SuppressWarnings("unchecked")
			SubSpec<V> spec = (SubSpec<V>) DataSpecs.getSubSpec(value);
            return spec.serialize(value, context);
        }
        return DataValue.of(value, context);
    }

    public static <T> T deserialize(DataObject data, Class<T> type, Context context) {
        String spec = data.getType();
        if (DataSpecs.hasSpec(spec)) {
            return DataSpecs.getSpec(spec).deserialize(data, type, context);
        }
        SubSpec<?> subSpec = DataSpecs.getSubSpec(type);
        if (subSpec == null) {
            throw new RuntimeException(String.format("No spec registered for name: '%s' or type: '%s'", spec, type));
        }
        return type.cast(subSpec.deserialize(data, context));
    }

    public static <T> List<T> deserialize(DataList data, Class<T> type) {
        return Cereal.deserialize(data, type, Context.NONE);
    }

    public static <T> List<T> deserialize(DataList data, Class<T> type, Context context) {
        ArrayList<T> list = new ArrayList<T>(data.size());
        for (DataValue value : data) {
            if (!value.isObj()) continue;
            list.add(Cereal.deserialize(value.asObj(), type, context));
        }
        return list;
    }
}

