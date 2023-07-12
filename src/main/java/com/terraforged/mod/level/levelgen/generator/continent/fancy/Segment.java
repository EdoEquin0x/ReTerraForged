/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.continent.fancy;

import com.terraforged.mod.noise.util.Vec2f;

public class Segment {
    public final Vec2f a;
    public final Vec2f b;
    public final float dx;
    public final float dy;
    public final float length;
    public final float length2;
    public final float scaleA;
    public final float scale2A;
    public final float scaleB;
    public final float scale2B;

    public Segment(Vec2f a, Vec2f b, float scaleA, float scaleB) {
        this.a = a;
        this.b = b;
        this.scaleA = scaleA;
        this.scaleB = scaleB;
        this.scale2A = scaleA * scaleA;
        this.scale2B = scaleB * scaleB;
        this.dx = b.x - a.x;
        this.dy = b.y - a.y;
        this.length = (float)Math.sqrt(this.dx * this.dx + this.dy * this.dy);
        this.length2 = this.dx * this.dx + this.dy * this.dy;
    }

    public float minX() {
        return Math.min(this.a.x, this.b.x);
    }

    public float minY() {
        return Math.min(this.a.y, this.b.y);
    }

    public float maxX() {
        return Math.max(this.a.x, this.b.x);
    }

    public float maxY() {
        return Math.max(this.a.y, this.b.y);
    }

    public float maxScale() {
        return Math.max(this.scaleA, this.scaleB);
    }

    public Segment translate(Vec2f offset) {
        return new Segment(new Vec2f(this.a.x + offset.x, this.a.y + offset.y), new Vec2f(this.b.x + offset.x, this.b.y + offset.y), this.scaleA, this.scaleB);
    }
}

