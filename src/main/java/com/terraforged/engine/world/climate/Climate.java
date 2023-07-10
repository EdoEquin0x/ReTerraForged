/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.climate;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.continent.Continent;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.source.Rand;
import com.terraforged.noise.util.NoiseUtil;

public class Climate {
    private final float lowerHeight;
    private final float midHeight = 0.45f;
    private final float upperHeight = 0.75f;
    private final float temperatureModifier = 0.05f;
    private final Rand rand;
    private final Module offsetX;
    private final Module offsetY;
    private final int offsetDistance;
    private final Levels levels;
    private final ClimateModule biomeNoise;

    public Climate(Continent continent, GeneratorContext context) {
        this.biomeNoise = new ClimateModule(continent, context);
        this.levels = context.levels;
        this.offsetDistance = context.settings.climate.biomeEdgeShape.strength;
        this.rand = new Rand(Source.builder());
        this.offsetX = context.settings.climate.biomeEdgeShape.build(context.seed.next());
        this.offsetY = context.settings.climate.biomeEdgeShape.build(context.seed.next());
        this.lowerHeight = context.levels.ground;
    }

    public Rand getRand() {
        return this.rand;
    }

    public float getOffsetX(float x, float z, float distance) {
        return this.offsetX.getValue(x, z) * distance;
    }

    public float getOffsetZ(float x, float z, float distance) {
        return this.offsetY.getValue(x, z) * distance;
    }

    public void apply(Cell cell, float x, float z) {
        this.biomeNoise.apply(cell, x, z, true);
        float edgeBlend = 0.4f;
        if (cell.value <= this.levels.water) {
            if (cell.terrain == TerrainType.COAST) {
                cell.terrain = TerrainType.SHALLOW_OCEAN;
            }
        } else if (cell.biomeRegionEdge < edgeBlend || cell.terrain == TerrainType.MOUNTAIN_CHAIN) {
            float modifier = 1.0f - NoiseUtil.map(cell.biomeRegionEdge, 0.0f, edgeBlend, edgeBlend);
            float distance = (float)this.offsetDistance * modifier;
            float dx = this.getOffsetX(x, z, distance);
            float dz = this.getOffsetZ(x, z, distance);
            this.biomeNoise.apply(cell, x += dx, z += dz, false);
        }
        this.modifyTemp(cell, x, z);
    }

    private void modifyTemp(Cell cell, float x, float z) {
        float height = cell.value;
        if (height > this.upperHeight) {
            cell.temperature = Math.max(0.0f, cell.temperature - this.temperatureModifier);
            return;
        }
        if (height > this.midHeight) {
            float delta = (height - this.midHeight) / 0.3f;
            cell.temperature = Math.max(0.0f, cell.temperature - delta * this.temperatureModifier);
            return;
        }
        if ((height = Math.max(this.lowerHeight, height)) >= this.lowerHeight) {
            float delta = 1.0f - (height - this.lowerHeight) / (this.midHeight - this.lowerHeight);
            cell.temperature = Math.min(1.0f, cell.temperature + delta * this.temperatureModifier);
        }
    }
}

