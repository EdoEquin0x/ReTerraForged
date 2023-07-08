/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.render;

import java.awt.Color;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.pool.ObjectPool;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.noise.util.NoiseUtil;

public enum RenderMode {
    BIOME_TYPE{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            try (Resource<?> buf = hsbBufs.get();){
                float[] hsb = ((HSBBuf)buf.get()).hsb;
                if (cell.terrain == TerrainType.BEACH) {
                    hsb[0] = 0.15f;
                    hsb[1] = 0.55f;
                    hsb[2] = 1.0f;
                } else {
                    Color c = cell.biome.getColor();
                    Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
                }
                RenderMode.color(buffer, hsb[0] * 100.0f, hsb[1] * 100.0f, hsb[2] * 100.0f, height, 0.5f, context.levels);
            }
        }
    }
    ,
    ELEVATION{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float temp = cell.temperature;
            float moist = Math.min(temp, cell.moisture);
            float hue = 35.0f - temp * (1.0f - moist) * 25.0f;
            RenderMode.color(buffer, hue, 70.0f, 80.0f, height, 0.3f, context.levels);
        }
    }
    ,
    TEMPERATURE{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            if (cell.temperature < 0.0f || cell.temperature > 1.0f) {
                System.out.println(cell.temperature);
            }
            float hue = RenderMode.hue(1.0f - cell.temperature, 8, 70);
            RenderMode.color(buffer, hue, 70.0f, 80.0f, height, 0.35f, context.levels);
        }
    }
    ,
    MOISTURE{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = RenderMode.hue(cell.moisture, 64, 70);
            RenderMode.color(buffer, hue, 70.0f, 80.0f, height, 0.35f, context.levels);
        }
    }
    ,
    BIOME{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = cell.biomeRegionId * 70.0f;
            RenderMode.color(buffer, hue, 70.0f, 80.0f, height, 0.4f, context.levels);
        }
    }
    ,
    MACRO_NOISE{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = RenderMode.hue(1.0f - cell.macroBiomeId, 64, 70);
            RenderMode.color(buffer, hue, 70.0f, 70.0f, height, 0.4f, context.levels);
        }
    }
    ,
    STEEPNESS{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = RenderMode.hue(1.0f - cell.gradient, 64, 70);
            RenderMode.color(buffer, hue, 70.0f, 70.0f, height, 0.4f, context.levels);
        }
    }
    ,
    TERRAIN_TYPE{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = 20.0f + cell.terrain.getRenderHue() * 80.0f;
            if (cell.terrain == TerrainType.COAST) {
                hue = 15.0f;
            }
            if (cell.continentEdge < 0.01f) {
                hue = 70.0f;
            }
            RenderMode.color(buffer, hue, 70.0f, 70.0f, height, 0.4f, context.levels);
        }
    }
    ,
    CONTINENT{

        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = cell.continentId * 70.0f;
            RenderMode.color(buffer, hue, 70.0f, 70.0f, height, 0.4f, context.levels);
        }
    };

    private static final ObjectPool<HSBBuf> hsbBufs;

    public abstract void fill(Cell var1, float var2, RenderBuffer var3, RenderSettings var4);

    public void fillColor(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
        if (height <= (float)context.levels.waterLevel) {
            float temp = cell.temperature;
            float tempDelta = (double)temp > 0.5 ? temp - 0.5f : -(0.5f - temp);
            float tempAlpha = tempDelta / 0.5f;
            float hueMod = 4.0f * tempAlpha;
            float depth = ((float)context.levels.waterLevel - height) / 90.0f;
            float darkness = 1.0f - depth;
            float darknessMod = 0.5f + darkness * 0.5f;
            buffer.color(60.0f - hueMod, 65.0f, 90.0f * darknessMod);
        } else {
            this.fill(cell, height, buffer, context);
        }
    }

    private static float hue(float value, int steps, int max) {
        value = Math.round(value * (float)(steps - 1));
        return (value /= (float)(steps - 1)) * (float)max;
    }

    private static void color(RenderBuffer buffer, float hue, float saturation, float brightness, float height, float strength, Levels levels) {
        float value = NoiseUtil.clamp((height - (float)levels.waterLevel) / (float)(levels.worldHeight - levels.waterLevel), 0.0f, 1.0f);
        float shade = 1.0f - strength + value * strength;
        float sat = saturation * (1.0f - shade * 0.1f);
        float bri = brightness * shade;
        buffer.color(hue, sat, bri);
    }

    static {
        hsbBufs = new ObjectPool<>(5, HSBBuf::new);
    }
}

