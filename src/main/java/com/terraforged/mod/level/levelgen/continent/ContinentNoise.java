/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.level.levelgen.continent;

import com.terraforged.mod.level.levelgen.continent.config.ContinentConfig;
import com.terraforged.mod.level.levelgen.noise.NoiseLevels;
import com.terraforged.mod.level.levelgen.noise.NoiseSample;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.level.levelgen.settings.ControlPoints;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.terrain.generation.TerrainLevels;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.domain.Domain;

public class ContinentNoise {
    protected final TerrainLevels levels;
    protected final ControlPoints controlPoints;
    protected final ContinentGenerator generator;

    protected final Seed seed;
    protected final Settings settings;
    protected final Domain warp;
    protected final float frequency;

    public ContinentNoise(Seed seed, TerrainLevels levels, Settings settings) {
    	this.seed = seed.split();
    	this.settings = settings;
    	this.levels = levels;
        this.controlPoints = new ControlPoints(settings.world().controlPoints());
        this.generator = createContinent(seed, settings, controlPoints, levels.noiseLevels);

        this.frequency = 1F / settings.world().continent().scale();

        double strength = 0.2;

        var builder = Source.builder()
                .octaves(3)
                .lacunarity(2.2)
                .frequency(3)
                .gain(0.3);

        this.warp = Domain.warp(
        	builder.seed(seed.next()).perlin2(),
        	builder.seed(seed.next()).perlin2(),
        	Source.constant(strength)
        );
    }

    public void sampleContinent(float x, float y, NoiseSample sample) {
        x *= frequency;
        y *= frequency;

        float px = warp.getX(x, y);
        float py = warp.getY(x, y);

        var offset = generator.getWorldOffset();
        px += offset.x;
        py += offset.y;

        generator.shapeNoise.sample(px, py, sample);
    }

    public void sampleRiver(float x, float y, NoiseSample sample) {
        x *= frequency;
        y *= frequency;

        float px = warp.getX(x, y);
        float py = warp.getY(x, y);

        var offset = generator.getWorldOffset();
        px += offset.x;
        py += offset.y;

        generator.riverNoise.sample(px, py, sample);
    }

    public Seed getSeed() {
    	return this.seed;
    }
    
    public ControlPoints getControlPoints() {
        return controlPoints;
    }
    
    public Settings getSettings() {
    	return this.settings;
    }

    protected static ContinentGenerator createContinent(Seed seed, Settings settings, ControlPoints controlPoints, NoiseLevels levels) {
        var config = new ContinentConfig();
        config.shape.scale = settings.world().continent().scale();
        config.shape.seed0 = seed.next();
        config.shape.seed1 = seed.next();
        return new ContinentGenerator(seed.get(), config, levels, controlPoints);
    }
}
