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
public @interface LegacyBool {
    public static final Function<Field, Boolean> GETTER = field -> {
        LegacyBool legacy = field.getAnnotation(LegacyBool.class);
        return legacy == null ? null : Boolean.valueOf(legacy.value());
    };

    public boolean value();
}

