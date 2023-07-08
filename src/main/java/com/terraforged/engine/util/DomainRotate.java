/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util;

import com.terraforged.noise.domain.Domain;

public class DomainRotate implements Domain {
    protected final float angle;
    protected final float cos;
    protected final float sin;

    public DomainRotate(float angle) {
        this.angle = angle;
        this.cos = (float)Math.cos(Math.toRadians(angle));
        this.sin = (float)Math.sin(Math.toRadians(angle));
    }

    @Override
    public String getSpecName() {
        return "DomainRotate";
    }

    @Override
    public float getX(int seed, float x, float y) {
        return this.getOffsetX(seed, x, y);
    }

    @Override
    public float getY(int seed, float x, float y) {
        return this.getOffsetY(seed, x, y);
    }

    @Override
    public float getOffsetX(int seed, float x, float y) {
        return x * this.cos - y * this.sin;
    }

    @Override
    public float getOffsetY(int seed, float x, float y) {
        return x * this.sin + y * this.cos;
    }
}

