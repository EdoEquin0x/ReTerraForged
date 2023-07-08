/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.terrain.populator;

import com.terraforged.cereal.spec.DataFactory;
import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.spec.SpecName;
import com.terraforged.cereal.value.DataValue;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public class TerrainConfig implements SpecName {
    private static final String SPEC_NAME = "Terrain";
    private final Terrain type;
    private final Module noise;
    private final float weight;
    private static final DataFactory<TerrainConfig> FACTORY = (data, spec, context) -> new TerrainConfig(spec.get("type", data, v -> TerrainType.get(v.asString())), spec.get("noise", data, Module.class, context), spec.get("weight", data, DataValue::asFloat).floatValue());

    public TerrainConfig(Terrain type, Module noise, float weight) {
        this.type = type;
        this.weight = weight;
        this.noise = noise;
    }

    @Override
    public String getSpecName() {
        return SPEC_NAME;
    }

    public TerrainPopulator createPopulator(Module baseNoise) {
        return new TerrainPopulator(this.type, baseNoise, this.noise, this.weight);
    }

    public static DataSpec<TerrainConfig> spec() {
        return DataSpec.builder(SPEC_NAME, TerrainConfig.class, FACTORY).add("type", (Object)TerrainType.NONE.getName(), data -> data.type.getName()).add("weight", (Object)Float.valueOf(1.0f), data -> Float.valueOf(data.weight)).add("noise", (Object)Source.ZERO, data -> data.noise).build();
    }
}

