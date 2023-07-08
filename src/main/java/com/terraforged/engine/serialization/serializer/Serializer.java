/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import com.terraforged.engine.Engine;
import com.terraforged.engine.serialization.annotation.Comment;
import com.terraforged.engine.serialization.annotation.Limit;
import com.terraforged.engine.serialization.annotation.Name;
import com.terraforged.engine.serialization.annotation.NoName;
import com.terraforged.engine.serialization.annotation.Rand;
import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Restricted;
import com.terraforged.engine.serialization.annotation.Serializable;
import com.terraforged.engine.serialization.annotation.Sorted;
import com.terraforged.engine.serialization.annotation.Unstable;
import com.terraforged.engine.util.NameUtil;

public class Serializer {
    public static final char META_PREFIX = '#';
    public static final String HIDE = "#hide";
    public static final String KEY = "key";
    public static final String ORDER = "order";
    public static final String DISPLAY = "display";
    public static final String NO_NAME = "noname";
    public static final String COMMENT = "comment";
    public static final String RANDOM = "random";
    public static final String OPTIONS = "options";
    public static final String BOUND_MIN = "min";
    public static final String BOUND_MAX = "max";
    public static final String LINK_PAD = "pad";
    public static final String LINK_LOWER = "limit_lower";
    public static final String LINK_UPPER = "limit_upper";
    public static final String RESTRICTED = "restricted";
    public static final String RESTRICTED_NAME = "name";
    public static final String RESTRICTED_OPTIONS = "options";

    public static void serialize(Object object, Writer writer) throws IllegalAccessException {
        Serializer.serialize(object, writer, true);
    }

    public static void serialize(Object object, Writer writer, boolean meta) throws IllegalAccessException {
        Serializer.serialize(object, writer, "", meta);
    }

    public static void serialize(Object object, Writer writer, String parentId, boolean meta) throws IllegalAccessException {
        if (object instanceof Map<?, ?> map) {
            Serializer.serializeMap(map, writer, parentId, meta, false);
        } else if (object.getClass().isArray()) {
            writer.beginArray();
            int length = Array.getLength(object);
            for (int i = 0; i < length; ++i) {
                Object element = Array.get(object, i);
                Serializer.serialize(element, writer);
            }
            writer.endArray();
        } else if (!object.getClass().isPrimitive()) {
            int order = 0;
            writer.beginObject();
            for (Field field : object.getClass().getFields()) {
                if (Serializer.isSerializable(field)) {
                    field.setAccessible(true);
                    Serializer.write(object, field, order, writer, parentId, meta);
                    ++order;
                    continue;
                }
                if (!meta || !Serializer.isHideMarker(field)) continue;
                writer.name(HIDE).value((Boolean)field.get(object));
            }
            writer.endObject();
        }
    }

    private static void write(Object object, Field field, int order, Writer writer, String parentId, boolean meta) throws IllegalAccessException {
        if (field.getType() == Integer.TYPE) {
            writer.name(field.getName()).value((Integer)field.get(object));
            Serializer.writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType() == Float.TYPE) {
            writer.name(field.getName()).value(((Float)field.get(object)).floatValue());
            Serializer.writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType() == String.class) {
            writer.name(field.getName()).value((String)field.get(object));
            Serializer.writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType() == Boolean.TYPE) {
            writer.name(field.getName()).value((Boolean)field.get(object));
            Serializer.writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType().isEnum()) {
            writer.name(field.getName()).value(((Enum<?>)field.get(object)).name());
            Serializer.writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType().isArray()) {
            if (field.getType().getComponentType().isAnnotationPresent(Serializable.class)) {
                writer.name(field.getName());
                Serializer.serialize(field.get(object), writer, Serializer.getKeyName(parentId, field), meta);
                Serializer.writeMeta(field, order, writer, parentId, meta);
            }
            return;
        }
        if (Map.class.isAssignableFrom(field.getType())) {
            Class<?> valueType = Serializer.getMapValueType(field);
            if (valueType != null && valueType.isAnnotationPresent(Serializable.class)) {
                writer.name(field.getName());
                Serializer.serializeMap((Map<?, ?>)field.get(object), writer, parentId, meta, field.isAnnotationPresent(Sorted.class));
                Serializer.writeMeta(field, order, writer, parentId, meta);
            }
            return;
        }
        if (field.getType().isAnnotationPresent(Serializable.class)) {
            writer.name(field.getName());
            String parent = Serializer.getKeyName(parentId, field);
            Serializer.serialize(field.get(object), writer, parent, meta);
            Serializer.writeMeta(field, order, writer, parentId, meta);
        }
    }

    private static <K, V> void serializeMap(Map<K, V> map, Writer writer, String parentId, boolean meta, boolean sorted) throws IllegalAccessException {
        writer.beginObject();
        Collection<Map.Entry<K, V>> entries = map.entrySet();
        if (sorted) {
            entries = entries.stream().sorted(Comparator.comparing(e -> e.getKey().toString())).collect(Collectors.toList());
        }
        int order = 0;
        for (Map.Entry<K, V> entry : entries) {
            String name = entry.getKey().toString();
            writer.name(name);
            Serializer.serialize(entry.getValue(), writer, parentId, meta);
            Serializer.writeMapEntryMeta(name, order, writer, meta);
            ++order;
        }
        writer.endObject();
    }

    private static void writeMeta(Field field, int order, Writer writer, String parentId, boolean meta) throws IllegalAccessException {
        Restricted restricted;
        Limit limit;
        Comment comment;
        if (!meta) {
            return;
        }
        writer.name('#' + field.getName()).beginObject();
        writer.name(ORDER).value(order);
        writer.name(KEY).value(Serializer.getKeyName(parentId, field));
        writer.name(DISPLAY).value(Serializer.getDisplayName(field));
        Range range = field.getAnnotation(Range.class);
        if (range != null) {
            if (field.getType() == Integer.TYPE) {
                writer.name(BOUND_MIN).value((int)range.min());
                writer.name(BOUND_MAX).value((int)range.max());
            } else {
                writer.name(BOUND_MIN).value(range.min());
                writer.name(BOUND_MAX).value(range.max());
            }
        }
        if ((field.getAnnotation(Rand.class)) != null) {
            writer.name(RANDOM).value(1);
        }
        if ((comment = field.getAnnotation(Comment.class)) != null) {
            writer.name(COMMENT);
            writer.value(Serializer.getComment(comment));
        }
        if ((field.getAnnotation(NoName.class)) != null) {
            writer.name(NO_NAME);
            writer.value(true);
        }
        if ((limit = field.getAnnotation(Limit.class)) != null) {
            writer.name(LINK_LOWER);
            writer.value(limit.lower());
            writer.name(LINK_UPPER);
            writer.value(limit.upper());
            writer.name(LINK_PAD);
            writer.value(limit.pad());
        }
        if ((restricted = field.getAnnotation(Restricted.class)) != null) {
            writer.name(RESTRICTED);
            writer.beginObject();
            writer.name(RESTRICTED_NAME);
            writer.value(restricted.name());
            writer.name("options");
            writer.beginArray();
            for (String value : restricted.value()) {
                writer.value(value);
            }
            writer.endArray();
            writer.endObject();
        }
        if (field.getType() == Boolean.TYPE) {
            writer.name("options");
            writer.beginArray();
            writer.value(true);
            writer.value(false);
            writer.endArray();
        }
        if (field.getType().isEnum()) {
            writer.name("options");
            writer.beginArray();
            for (Enum<?> o : field.getType().asSubclass(Enum.class).getEnumConstants()) {
                if (!Serializer.isValidOption(o)) continue;
                writer.value(o.name());
            }
            writer.endArray();
        }
        writer.endObject();
    }

    private static void writeMapEntryMeta(String name, int order, Writer writer, boolean meta) {
        if (!meta) {
            return;
        }
        writer.name('#' + name);
        writer.beginObject();
        writer.name(ORDER).value(order);
        writer.name(KEY).value(name);
        writer.name(DISPLAY).value(name);
        writer.endObject();
    }

    private static String getDisplayName(Field field) {
        Name nameMeta = field.getAnnotation(Name.class);
        String name = nameMeta == null ? field.getName() : nameMeta.value();
        return NameUtil.toDisplayName(name);
    }

    private static String getKeyName(String parent, Field field) {
        Name nameMeta = field.getAnnotation(Name.class);
        String name = nameMeta == null ? field.getName() : nameMeta.value();
        return NameUtil.toTranslationKey(parent, name);
    }

    private static String getComment(Comment comment) {
        return String.join((CharSequence)"\n", comment.value());
    }

    private static boolean isValidOption(Enum<?> value) {
        if (Engine.ENFORCE_STABLE_OPTIONS) {
            try {
                Class<?> type = value.getDeclaringClass();
                Field field = type.getDeclaredField(value.name());
                return !field.isAnnotationPresent(Unstable.class);
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    protected static Class<?> getMapValueType(Field field) {
        ParameterizedType genericType = (ParameterizedType)field.getGenericType();
        Type[] types = genericType.getActualTypeArguments();
        if (types.length == 2) {
            return (Class<?>) types[1];
        }
        return null;
    }

    protected static boolean isSerializable(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers);
    }

    protected static boolean isHideMarker(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers) && Modifier.isTransient(modifiers) && field.getType() == Boolean.TYPE;
    }
}

