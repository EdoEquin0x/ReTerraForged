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
public @interface LegacyFloat {
    public static final Function<Field, Float> GETTER = field -> {
        LegacyFloat legacy = field.getAnnotation(LegacyFloat.class);
        return legacy == null ? null : Float.valueOf(legacy.value());
    };

    public float value();
}

