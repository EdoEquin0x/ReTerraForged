/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.heightmap;

import com.terraforged.engine.settings.WorldSettings;

public class ControlPoints {
    public final float deepOcean;
    public final float shallowOcean;
    public final float beach;
    public final float coast;
    public final float coastMarker;
    public final float inland;

    public ControlPoints(WorldSettings.ControlPoints points) {
        if (!ControlPoints.validate(points)) {
            points = new WorldSettings.ControlPoints();
        }
        this.inland = points.inland;
        this.coast = points.coast;
        this.beach = points.beach;
        this.shallowOcean = points.shallowOcean;
        this.deepOcean = points.deepOcean;
        this.coastMarker = this.coast + (this.inland - this.coast) / 2.0f;
    }

    public static boolean validate(WorldSettings.ControlPoints points) {
        return points.inland <= 1.0f && points.inland > points.coast && points.coast > points.beach && points.beach > points.shallowOcean && points.shallowOcean > points.deepOcean && points.deepOcean >= 0.0f;
    }
}

