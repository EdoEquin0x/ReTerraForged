/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.rivermap.wetland;

import com.terraforged.engine.settings.RiverSettings;
import com.terraforged.engine.util.Variance;
import com.terraforged.noise.util.NoiseUtil;

public class WetlandConfig {
    public final int skipSize;
    public final Variance length;
    public final Variance width;

    public WetlandConfig(RiverSettings.Wetland settings) {
        this.skipSize = Math.max(1, NoiseUtil.round((1.0f - settings.chance) * 10.0f));
        this.length = Variance.of(settings.sizeMin, settings.sizeMax);
        this.width = Variance.of(50.0, 150.0);
    }
}

