/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Serializable;
import com.terraforged.engine.serialization.annotation.legacy.LegacyBool;
import com.terraforged.engine.serialization.annotation.legacy.LegacyFloat;
import com.terraforged.engine.serialization.annotation.legacy.LegacyInt;
import com.terraforged.engine.serialization.annotation.legacy.LegacyString;
import com.terraforged.noise.util.NoiseUtil;

public class Deserializer {
    private static final BiFunction<Reader, String, Integer> INT_GETTER = Reader::getInt;
    private static final BiFunction<Reader, String, Float> FLOAT_GETTER = Reader::getFloat;
    private static final BiFunction<Reader, String, String> STRING_GETTER = Reader::getString;
    private static final BiFunction<Reader, String, Boolean> BOOLEAN_GETTER = Reader::getBool;

    public static boolean deserialize(Reader reader, Object object) throws Throwable {
        boolean valid = true;
        Class<?> type = object.getClass();
        for (Field field : type.getFields()) {
            if (!Serializer.isSerializable(field)) continue;
            field.setAccessible(true);
            try {
                valid &= Deserializer.fromValue(reader, object, field);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return valid;
    }

    private static boolean fromValue(Reader reader, Object object, Field field) throws Throwable {
        Class<?> valueType;
        if (field.getType() == Integer.TYPE) {
            return Deserializer.set(object, field, reader, INT_GETTER, LegacyInt.GETTER);
        }
        if (field.getType() == Float.TYPE) {
            return Deserializer.set(object, field, reader, FLOAT_GETTER, LegacyFloat.GETTER);
        }
        if (field.getType() == Boolean.TYPE) {
            return Deserializer.set(object, field, reader, BOOLEAN_GETTER, LegacyBool.GETTER);
        }
        if (field.getType() == String.class) {
            return Deserializer.set(object, field, reader, STRING_GETTER, LegacyString.GETTER);
        }
        if (field.getType().isEnum()) {
            return Deserializer.setEnum(object, field, reader);
        }
        if (field.getType().isAnnotationPresent(Serializable.class)) {
            return Deserializer.setObject(object, field, reader);
        }
        if (field.getType().isArray()) {
            return Deserializer.setArray(object, field, reader);
        }
        if (Map.class.isAssignableFrom(field.getType()) && (valueType = Serializer.getMapValueType(field)) != null && valueType.isAnnotationPresent(Serializable.class)) {
            return Deserializer.setMap(object, field, valueType, reader);
        }
        return true;
    }

    private static <T> boolean set(Object owner, Field field, Reader reader, BiFunction<Reader, String, T> getter, Function<Field, T> legacy) throws IllegalAccessException {
        T value = reader.has(field.getName()) ? getter.apply(reader, field.getName()) : legacy.apply(field);
        if (value == null) {
            return false;
        }
        if (value instanceof Number) {
            return Deserializer.set(owner, field, Deserializer.clamp((Number)value, field));
        }
        field.set(owner, value);
        return true;
    }

    private static boolean setEnum(Object object, Field field, Reader reader) throws IllegalAccessException {
        String name = reader.has(field.getName()) ? reader.getString(field.getName()) : LegacyString.GETTER.apply(field);
        if (name == null) {
            return false;
        }
        for (Enum<?> e : field.getType().asSubclass(Enum.class).getEnumConstants()) {
            if (!e.name().equals(name)) continue;
            field.set(object, e);
            return true;
        }
        return false;
    }

    private static boolean setObject(Object object, Field field, Reader reader) throws Throwable {
        if (reader.has(field.getName())) {
            Reader child = reader.getChild(field.getName());
            Object value = field.getType().newInstance();
            Deserializer.deserialize(child, value);
            field.set(object, value);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
	private static boolean setMap(Object object, Field field, Class<?> valueType, Reader reader) throws Throwable {
        if (reader.has(field.getName())) {
            Map<String, Object> map = (Map<String, Object>) field.get(object);
            map.clear();
            Reader child = reader.getChild(field.getName());
            for (String key : child.getKeys()) {
                if (key.charAt(0) == '#') continue;
                Object value = valueType.newInstance();
                Deserializer.deserialize(child.getChild(key), value);
                map.put(key, value);
            }
            return true;
        }
        return false;
    }

    private static boolean setArray(Object object, Field field, Reader reader) throws Throwable {
        Class<?> type;
        if (reader.has(field.getName()) && (type = field.getType().getComponentType()).isAnnotationPresent(Serializable.class)) {
            Reader child = reader.getChild(field.getName());
            Object array = Array.newInstance(type, child.getSize());
            for (int i = 0; i < child.getSize(); ++i) {
                Object value = type.newInstance();
                Deserializer.deserialize(child.getChild(i), value);
                Array.set(array, i, value);
            }
            field.set(object, array);
            return true;
        }
        return false;
    }

    private static Number clamp(Number value, Field field) {
        Range range = field.getAnnotation(Range.class);
        if (range != null) {
            return Float.valueOf(NoiseUtil.clamp(value.floatValue(), range.min(), range.max()));
        }
        return value;
    }

    private static boolean set(Object owner, Field field, Number value) throws IllegalAccessException {
        if (field.getType() == Integer.TYPE) {
            field.set(owner, value.intValue());
            return true;
        }
        if (field.getType() == Float.TYPE) {
            field.set(owner, Float.valueOf(value.floatValue()));
            return true;
        }
        if (field.getType() == Long.TYPE) {
            field.set(owner, value.longValue());
            return true;
        }
        if (field.getType() == Double.TYPE) {
            field.set(owner, value.doubleValue());
            return true;
        }
        return false;
    }
}

