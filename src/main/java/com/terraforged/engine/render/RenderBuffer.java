/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.render;

public interface RenderBuffer {
    public void beginQuads();

    public void endQuads();

    public void vertex(float var1, float var2, float var3);

    public void color(float var1, float var2, float var3);

    public void draw();

    default public void dispose() {
    }

    default public void noFill() {
    }

    default public void noStroke() {
    }
}

