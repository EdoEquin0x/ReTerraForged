/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine;

import com.terraforged.cereal.spec.DataSpecs;
import com.terraforged.engine.module.Ridge;

public class Engine {
    public static final boolean ENFORCE_STABLE_OPTIONS = System.getProperty("unstable") == null;

    public static void init() {
    }

    static {
        DataSpecs.register(Ridge.spec());
    }
}

