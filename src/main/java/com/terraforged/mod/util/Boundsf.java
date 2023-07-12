/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.util;

public class Boundsf {
    public static final Boundsf NONE = new Boundsf(1.0f, 1.0f, -1.0f, -1.0f);
    public final float minX;
    public final float minY;
    public final float maxX;
    public final float maxY;

    public Boundsf(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public boolean contains(float x, float y) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float minX = Float.MAX_VALUE;
        private float minY = Float.MAX_VALUE;
        private float maxX = Float.MIN_VALUE;
        private float maxY = Float.MIN_VALUE;

        public void record(float x, float y) {
            this.minX = Math.min(this.minX, x);
            this.minY = Math.min(this.minY, y);
            this.maxX = Math.max(this.maxX, x);
            this.maxY = Math.max(this.maxY, y);
        }

        public Boundsf build() {
            return new Boundsf(this.minX, this.minY, this.maxX, this.maxY);
        }
    }
}

