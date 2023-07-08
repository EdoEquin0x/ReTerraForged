/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Limit {
    public String lower() default "";

    public String upper() default "";

    public float pad() default -1.0f;
}

