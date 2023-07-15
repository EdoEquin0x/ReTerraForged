/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent.fancy;

import com.terraforged.mod.level.levelgen.settings.ControlPoints;
import com.terraforged.mod.noise.source.Line;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.noise.util.Vec2f;

public class Island {
    private final int id;
    private final Segment[] segments;
    private final float coastSq;
    private final float deepOceanSq;
    private final float shallowOcean;
    private final float shallowOceanSq;
    private final float radius;
    private final float deepMod;
    private final float shallowMod;
    private Vec2f center;
    private Vec2f min;
    private Vec2f max;

    public Island(int id, Segment[] segments, ControlPoints controlPoints, float deepOcean, float shallowOcean, float coast, float inland) {
        float x = 0.0f;
        float y = 0.0f;
        int points = segments.length + 1;
        float minX = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxZ = Float.MIN_VALUE;
        float maxRadius = coast;
        for (int i = 0; i < segments.length; ++i) {
            Segment segment = segments[i];
            minX = Math.min(minX, segment.minX());
            minZ = Math.min(minZ, segment.minY());
            maxX = Math.max(maxX, segment.maxX());
            maxZ = Math.max(maxZ, segment.maxY());
            maxRadius = Math.max(maxRadius, segment.maxScale() * coast);
            if (i == 0) {
                x += segment.a.x;
                y += segment.a.y;
            }
            x += segment.b.x;
            y += segment.b.y;
        }
        this.id = id;
        this.segments = segments;
        this.shallowOcean = shallowOcean;
        this.coastSq = coast * coast;
        this.deepOceanSq = deepOcean * deepOcean;
        this.shallowOceanSq = shallowOcean * shallowOcean;
        this.deepMod = 0.25f;
        this.shallowMod = 1.0f - this.deepMod;
        float maxDim = Math.max((maxX += coast) - (minX -= coast), (maxZ += coast) - (minZ -= coast));
        this.center = new Vec2f(x / (float)points, y / (float)points);
        this.min = new Vec2f(minX - maxRadius, minZ - maxRadius);
        this.max = new Vec2f(maxX + maxRadius, maxZ + maxRadius);
        this.radius = maxDim;
    }

    public int getId() {
        return this.id;
    }

    public Segment[] getSegments() {
        return this.segments;
    }

    public float radius() {
        return this.radius;
    }

    public float coast() {
        return this.shallowOcean;
    }

    public void translate(Vec2f offset) {
        this.center = new Vec2f(this.center.x + offset.x, this.center.y + offset.y);
        this.min = new Vec2f(this.min.x + offset.x, this.min.y + offset.y);
        this.max = new Vec2f(this.max.x + offset.x, this.max.y + offset.y);
        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i] = this.segments[i].translate(offset);
        }
    }

    public Vec2f getMin() {
        return this.min;
    }

    public Vec2f getMax() {
        return this.max;
    }

    public Vec2f getCenter() {
        return this.center;
    }

    public boolean overlaps(Island other) {
        return this.overlaps(other.min, other.max);
    }

    public boolean overlaps(Vec2f min, Vec2f max) {
        return this.min.x < max.x && this.max.x > min.x && this.min.y < max.y && this.max.y > min.y;
    }

    public boolean contains(Vec2f vec) {
        return this.contains(vec.x, vec.y);
    }

    public boolean contains(float x, float z) {
        return x > this.min.x && x < this.max.x && z > this.min.y && z < this.max.y;
    }

    public float getEdgeValue(float x, float y) {
        float value = this.getEdgeDist2(x, y, this.deepOceanSq);
        float deepValue = Math.min(this.deepOceanSq, value);
        float shallowValue = Math.min(this.shallowOceanSq, value);
        return this.process(deepValue, shallowValue);
    }

    public float getLandValue(float x, float y) {
        float value = this.getEdgeDist2(x, y, this.shallowOceanSq);
        if (value < this.shallowOceanSq) {
            value = (this.shallowOceanSq - value) / this.shallowOceanSq;
            return NoiseUtil.curve(value, 0.75f, 4.0f);
        }
        return 0.0f;
    }

    private float getEdgeDist2(float x, float y, float minDist2) {
        float value = minDist2;
        for (Segment segment : this.segments) {
            float scale;
            float py;
            float px;
            float dx = segment.dx;
            float dy = segment.dy;
            float t = (x - segment.a.x) * dx + (y - segment.a.y) * dy;
            if ((t /= segment.length2) < 0.0f) {
                px = segment.a.x;
                py = segment.a.y;
                scale = segment.scale2A;
            } else if (t > 1.0f) {
                px = segment.b.x;
                py = segment.b.y;
                scale = segment.scale2B;
            } else {
                px = segment.a.x + t * dx;
                py = segment.a.y + t * dy;
                scale = NoiseUtil.lerp(segment.scale2A, segment.scale2B, t);
            }
            float v = Line.dist2(x, y, px, py) / scale;
            value = Math.min(v, value);
        }
        return value;
    }

    private float process(float deepValue, float shallowValue) {
        if (deepValue == this.deepOceanSq) {
            return 0.0f;
        }
        if (deepValue > this.shallowOceanSq) {
            deepValue = (deepValue - this.shallowOceanSq) / (this.deepOceanSq - this.shallowOceanSq);
            deepValue = 1.0f - deepValue;
            deepValue *= deepValue;
            return deepValue * this.deepMod;
        }
        if (shallowValue == this.shallowOceanSq) {
            return this.deepMod;
        }
        if (shallowValue > this.coastSq) {
            shallowValue = (shallowValue - this.coastSq) / (this.shallowOceanSq - this.coastSq);
            shallowValue = 1.0f - shallowValue;
            return this.deepMod + shallowValue * this.shallowMod;
        }
        return 1.0f;
    }
}

