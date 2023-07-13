/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent;

import com.mojang.serialization.Codec;
import com.terraforged.mod.codec.TFCodecs;

public enum SpawnType {
    CONTINENT_CENTER,
    WORLD_ORIGIN;
	
	public static final Codec<SpawnType> CODEC = TFCodecs.forEnum(SpawnType::valueOf);
}