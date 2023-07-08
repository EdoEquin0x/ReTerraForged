/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.settings;

import com.terraforged.engine.serialization.annotation.Comment;
import com.terraforged.engine.serialization.annotation.Rand;
import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Serializable;

@Serializable
public class RiverSettings {
    @Rand
    @Comment(value={"A seed offset used to randomise river distribution"})
    public int seedOffset = 0;
    @Range(min=0.0f, max=30.0f)
    @Comment(value={"Controls the number of main rivers per continent."})
    public int riverCount = 8;
    public River mainRivers = new River(5, 2, 6, 20, 8, 0.75f);
    public River branchRivers = new River(4, 1, 4, 14, 5, 0.975f);
    public Lake lakes = new Lake();
    public Wetland wetlands = new Wetland();

    @Serializable
    public static class Wetland {
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"Controls how common wetlands are"})
        public float chance = 0.6f;
        @Range(min=50.0f, max=500.0f)
        @Comment(value={"The minimum size of the wetlands"})
        public int sizeMin = 175;
        @Range(min=50.0f, max=500.0f)
        @Comment(value={"The maximum size of the wetlands"})
        public int sizeMax = 225;
    }

    @Serializable
    public static class Lake {
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"Controls the chance of a lake spawning"})
        public float chance = 0.3f;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"The minimum distance along a river that a lake will spawn"})
        public float minStartDistance = 0.0f;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"The maximum distance along a river that a lake will spawn"})
        public float maxStartDistance = 0.03f;
        @Range(min=1.0f, max=20.0f)
        @Comment(value={"The max depth of the lake"})
        public int depth = 10;
        @Range(min=10.0f, max=100.0f)
        @Comment(value={"The minimum size of the lake"})
        public int sizeMin = 75;
        @Range(min=50.0f, max=500.0f)
        @Comment(value={"The maximum size of the lake"})
        public int sizeMax = 150;
        @Range(min=1.0f, max=10.0f)
        @Comment(value={"The minimum bank height"})
        public int minBankHeight = 2;
        @Range(min=1.0f, max=10.0f)
        @Comment(value={"The maximum bank height"})
        public int maxBankHeight = 10;
    }

    @Serializable
    public static class River {
        @Range(min=1.0f, max=10.0f)
        @Comment(value={"Controls the depth of the river"})
        public int bedDepth;
        @Range(min=0.0f, max=10.0f)
        @Comment(value={"Controls the height of river banks"})
        public int minBankHeight;
        @Range(min=1.0f, max=10.0f)
        @Comment(value={"Controls the height of river banks"})
        public int maxBankHeight;
        @Range(min=1.0f, max=20.0f)
        @Comment(value={"Controls the river-bed width"})
        public int bedWidth;
        @Range(min=1.0f, max=50.0f)
        @Comment(value={"Controls the river-banks width"})
        public int bankWidth;
        @Range(min=0.0f, max=1.0f)
        @Comment(value={"Controls how much rivers taper"})
        public float fade;

        public River() {
        }

        public River(int depth, int minBank, int maxBank, int outer, int inner, float fade) {
            this.minBankHeight = minBank;
            this.maxBankHeight = maxBank;
            this.bankWidth = outer;
            this.bedWidth = inner;
            this.bedDepth = depth;
            this.fade = fade;
        }
    }
}

