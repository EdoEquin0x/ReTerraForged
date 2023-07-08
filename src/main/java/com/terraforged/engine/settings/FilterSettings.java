/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.settings;

import com.terraforged.engine.serialization.annotation.Comment;
import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Serializable;

@Serializable
public class FilterSettings {
    public Erosion erosion = new Erosion();
    public Smoothing smoothing = new Smoothing();

    @Serializable
    public static class Smoothing {
        @Range(min=0.0f, max=5.0f)
        @Comment(value={"Controls the number of smoothing iterations"})
        public int iterations = 1;
        @Range(min=0.0f, max=5.0f)
        @Comment(value={"Controls the smoothing radius"})
        public float smoothingRadius = 1.8f;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"Controls how strongly smoothing is applied"})
        public float smoothingRate = 0.9f;
    }

    @Serializable
    public static class Erosion {
        @Range(min=10.0f, max=250.0f)
        @Comment(value={"The average number of water droplets to simulate per chunk"})
        public int dropletsPerChunk = 135;
        @Range(min=1.0f, max=32.0f)
        @Comment(value={"Controls the number of iterations that a single water droplet is simulated for"})
        public int dropletLifetime = 12;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"Controls the starting volume of water that a simulated water droplet carries"})
        public float dropletVolume = 0.7f;
        @Range(min=0.1f, max=1.0f)
        @Comment(value={"Controls the starting velocity of the simulated water droplet"})
        public float dropletVelocity = 0.7f;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"Controls how quickly material dissolves (during erosion)"})
        public float erosionRate = 0.5f;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"Controls how quickly material is deposited (during erosion)"})
        public float depositeRate = 0.5f;

        public Erosion copy() {
            Erosion erosion = new Erosion();
            erosion.dropletsPerChunk = this.dropletsPerChunk;
            erosion.erosionRate = this.erosionRate;
            erosion.depositeRate = this.depositeRate;
            erosion.dropletLifetime = this.dropletLifetime;
            erosion.dropletVolume = this.dropletVolume;
            erosion.dropletVelocity = this.dropletVelocity;
            return erosion;
        }
    }
}

