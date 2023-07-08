/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.biome.modifier;

import com.terraforged.engine.cell.Cell;

public interface BiomeModifier extends Comparable<BiomeModifier> {
    public int priority();

    public boolean test(int seed, int var1, Cell var2);

    public int modify(int seed, int var1, Cell var2, int var3, int var4);

    default public boolean exitEarly() {
        return false;
    }

    @Override
    default public int compareTo(BiomeModifier other) {
        return Integer.compare(other.priority(), this.priority());
    }
}

