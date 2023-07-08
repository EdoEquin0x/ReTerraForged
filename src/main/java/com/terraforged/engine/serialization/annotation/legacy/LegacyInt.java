/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.annotation.legacy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.function.Function;

@Target(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface LegacyInt {
    public static final Function<Field, Integer> GETTER = field -> {
        LegacyInt legacy = field.getAnnotation(LegacyInt.class);
        return legacy == null ? null : Integer.valueOf(legacy.value());
    };

    public int value();
}

