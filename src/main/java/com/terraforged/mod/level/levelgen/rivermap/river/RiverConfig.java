/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.rivermap.river;

import com.terraforged.mod.level.levelgen.heightmap.Levels;
import com.terraforged.mod.noise.util.NoiseUtil;

public class RiverConfig {
    public final int order;
    public final boolean main;
    public final int bedWidth;
    public final int bankWidth;
    public final float bedHeight;
    public final float minBankHeight;
    public final float maxBankHeight;
    public final int length;
    public final int length2;
    public final float fade;

    private RiverConfig(Builder builder) {
        this.main = builder.main;
        this.order = builder.order;
        this.bedWidth = builder.bedWidth;
        this.bankWidth = builder.bankWidth;
        this.bedHeight = builder.levels.water(-builder.bedDepth);
        this.minBankHeight = builder.levels.water(builder.minBankHeight);
        this.maxBankHeight = builder.levels.water(builder.maxBankHeight);
        this.length = builder.length;
        this.length2 = builder.length * builder.length;
        this.fade = (float)builder.fade;
    }

    public RiverConfig(boolean main, int order, int bedWidth, int bankWidth, float bedHeight, float minBankHeight, float maxBankHeight, int length, int length2, double fade) {
        this.main = main;
        this.order = order;
        this.bedWidth = bedWidth;
        this.bankWidth = bankWidth;
        this.bedHeight = bedHeight;
        this.minBankHeight = minBankHeight;
        this.maxBankHeight = maxBankHeight;
        this.length = length;
        this.length2 = length2;
        this.fade = (float)fade;
    }

    public RiverConfig createFork(float connectWidth, Levels levels) {
        if ((float)this.bankWidth < connectWidth) {
            return this;
        }
        float scale = (float)this.bankWidth / connectWidth;
        return this.createFork(levels.scale(this.bedHeight), NoiseUtil.round((float)this.bedWidth / scale), NoiseUtil.round((float)this.bankWidth / scale), levels);
    }

    public RiverConfig createFork(int bedHeight, int bedWidth, int bankWidth, Levels levels) {
        int minBankHeight = Math.max(levels.groundLevel, levels.scale(this.minBankHeight) - 1);
        int maxBankHeight = Math.max(minBankHeight, levels.scale(this.maxBankHeight) - 1);
        return new RiverConfig(false, this.order + 1, bedWidth, bankWidth, levels.scale(bedHeight), levels.scale(minBankHeight), levels.scale(maxBankHeight), this.length, this.length2, this.fade);
    }

    public static Builder builder(Levels levels) {
        return new Builder(levels);
    }

    public static class Builder {
        private boolean main = false;
        private int order = 0;
        private int bedWidth = 4;
        private int bankWidth = 15;
        private int bedDepth = 5;
        private int maxBankHeight = 1;
        private int minBankHeight = 1;
        private int length = 1000;
        private double fade = 0.2;
        private final Levels levels;

        private Builder(Levels levels) {
            this.levels = levels;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder main(boolean value) {
            this.main = value;
            return this;
        }

        public Builder bedWidth(int value) {
            this.bedWidth = value;
            return this;
        }

        public Builder bankWidth(int value) {
            this.bankWidth = value;
            return this;
        }

        public Builder bedDepth(int depth) {
            this.bedDepth = depth;
            return this;
        }

        public Builder bankHeight(int min, int max) {
            this.minBankHeight = Math.min(min, max);
            this.maxBankHeight = Math.max(min, max);
            return this;
        }

        public Builder length(int value) {
            this.length = value;
            return this;
        }

        public Builder fade(double value) {
            this.fade = value;
            return this;
        }

        public RiverConfig build() {
            return new RiverConfig(this);
        }
    }
}

