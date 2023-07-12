/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.rivermap.wetland;

import com.terraforged.mod.level.levelgen.settings.RiverSettings;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.util.Variance;

public class WetlandConfig {
    public final int skipSize;
    public final Variance length;
    public final Variance width;

    public WetlandConfig(RiverSettings.Wetland settings) {
        this.skipSize = Math.max(1, NoiseUtil.round((1.0f - settings.chance()) * 10.0f));
        this.length = Variance.of(settings.sizeMin(), settings.sizeMax());
        this.width = Variance.of(50.0, 150.0);
    }
}

