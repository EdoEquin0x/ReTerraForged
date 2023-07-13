/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain;

@Deprecated(forRemoval = true)
public interface ITerrain {
    default public float erosionModifier() {
        return 1.0f;
    }

    default public boolean isRiver() {
        return false;
    }

    default public boolean isCoast() {
        return false;
    }
 
    default public boolean isOverground() {
        return false;
    }

    default public boolean overridesRiver() {
        return this.isCoast();
    }

    default public boolean isLake() {
        return false;
    }

    public static interface Delegate extends ITerrain {
        public ITerrain getDelegate();

        @Override
        default public float erosionModifier() {
            return this.getDelegate().erosionModifier();
        }
        
        @Override
        default public boolean isRiver() {
            return this.getDelegate().isRiver();
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
        default public boolean isLake() {
            return this.getDelegate().isLake();
        }

        @Override
        default public boolean isOverground() {
            return this.getDelegate().isOverground();
        }
    }
}

