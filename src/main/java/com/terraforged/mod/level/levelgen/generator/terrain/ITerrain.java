/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.terrain;

public interface ITerrain {
    default public float erosionModifier() {
        return 1.0f;
    }

    default public boolean isFlat() {
        return false;
    }

    default public boolean isRiver() {
        return false;
    }

    default public boolean isShallowOcean() {
        return false;
    }

    default public boolean isDeepOcean() {
        return false;
    }

    default public boolean isCoast() {
        return false;
    }

    default public boolean isSubmerged() {
        return this.isDeepOcean() || this.isShallowOcean() || this.isRiver() || this.isLake();
    }

    default public boolean isOverground() {
        return false;
    }

    default public boolean overridesRiver() {
        return this.isDeepOcean() || this.isShallowOcean() || this.isCoast();
    }

    default public boolean overridesCoast() {
        return this.isVolcano();
    }

    default public boolean isLake() {
        return false;
    }

    default public boolean isWetland() {
        return false;
    }

    default public boolean isMountain() {
        return false;
    }

    default public boolean isVolcano() {
        return false;
    }

    public static interface Delegate extends ITerrain {
        public ITerrain getDelegate();

        @Override
        default public float erosionModifier() {
            return this.getDelegate().erosionModifier();
        }

        @Override
        default public boolean isFlat() {
            return this.getDelegate().isFlat();
        }

        @Override
        default public boolean isRiver() {
            return this.getDelegate().isRiver();
        }

        @Override
        default public boolean isShallowOcean() {
            return this.getDelegate().isShallowOcean();
        }

        @Override
        default public boolean isDeepOcean() {
            return this.getDelegate().isDeepOcean();
        }

        @Override
        default public boolean isCoast() {
            return this.getDelegate().isCoast();
        }

        @Override
        default public boolean overridesRiver() {
            return this.getDelegate().overridesRiver();
        }

        @Override
        default public boolean overridesCoast() {
            return this.getDelegate().overridesCoast();
        }

        @Override
        default public boolean isLake() {
            return this.getDelegate().isLake();
        }

        @Override
        default public boolean isWetland() {
            return this.getDelegate().isWetland();
        }

        @Override
        default public boolean isOverground() {
            return this.getDelegate().isOverground();
        }

        @Override
        default public boolean isSubmerged() {
            return this.getDelegate().isSubmerged();
        }

        @Override
        default public boolean isMountain() {
            return this.getDelegate().isMountain();
        }

        @Override
        default public boolean isVolcano() {
            return this.getDelegate().isVolcano();
        }
    }
}

