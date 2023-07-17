/*
 *
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.mod.noise.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.func.CellFunc;
import com.terraforged.mod.noise.func.DistanceFunc;
import com.terraforged.mod.noise.util.Noise;

public class CellNoise extends NoiseSource {
	public static final Codec<CellNoise> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("seed", Builder.DEFAULT_SEED).forGetter((c) -> c.seed),
		Codec.FLOAT.optionalFieldOf("frequency", Builder.DEFAULT_FREQUENCY).forGetter((c) -> c.frequency),
		Module.DIRECT_CODEC.fieldOf("source").forGetter((c) -> c.lookup),
		CellFunc.CODEC.optionalFieldOf("cell_func", Builder.DEFAULT_CELL_FUNC).forGetter((c) -> c.cellFunc),
		DistanceFunc.CODEC.optionalFieldOf("dist_func", Builder.DEFAULT_DIST_FUNC).forGetter((c) -> c.distFunc),
		Codec.FLOAT.optionalFieldOf("distance", Builder.DEFAULT_DISTANCE).forGetter((c) -> c.distance)
	).apply(instance, CellNoise::new));
	
    private final Module lookup;
    private final CellFunc cellFunc;
    private final DistanceFunc distFunc;
    private final float distance;
    private final float min;
    private final float max;
    private final float range;

    public CellNoise(int seed, float frequency, Module lookup, CellFunc cellFunc, DistanceFunc distFunc, float distance) {
    	super(seed, frequency);
    	this.lookup = lookup;
        this.cellFunc = cellFunc;
        this.distFunc = distFunc;
        this.distance = distance;
        this.min = min(this.cellFunc, this.lookup);
        this.max = max(this.cellFunc, this.lookup);
        this.range = this.max - this.min;
    }

    @Override
    public float getValue(float x, float y, int seed) {
        x *= frequency;
        y *= frequency;
        float value = Noise.cell(x, y, seed, distance, cellFunc, distFunc, lookup);
        return cellFunc.mapValue(value, min, max, range);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CellNoise cellNoise = (CellNoise) o;

        if (Float.compare(cellNoise.min, min) != 0) return false;
        if (Float.compare(cellNoise.max, max) != 0) return false;
        if (Float.compare(cellNoise.range, range) != 0) return false;
        if (Float.compare(cellNoise.distance, distance) != 0) return false;
        if (!lookup.equals(cellNoise.lookup)) return false;
        if (cellFunc != cellNoise.cellFunc) return false;
        return distFunc == cellNoise.distFunc;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + lookup.hashCode();
        result = 31 * result + cellFunc.hashCode();
        result = 31 * result + distFunc.hashCode();
        result = 31 * result + (min != +0.0f ? Float.floatToIntBits(min) : 0);
        result = 31 * result + (max != +0.0f ? Float.floatToIntBits(max) : 0);
        result = 31 * result + (range != +0.0f ? Float.floatToIntBits(range) : 0);
        result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
        return result;
    }
    
    @Override
	public Codec<CellNoise> codec() {
		return CODEC;
	}

    static float min(CellFunc func, Module lookup) {
        if (func == CellFunc.NOISE_LOOKUP) {
            return lookup.minValue();
        }
        if (func == CellFunc.DISTANCE) {
            return -1;
        }
        return -1;
    }

    static float max(CellFunc func, Module lookup) {
        if (func == CellFunc.NOISE_LOOKUP) {
            return lookup.maxValue();
        }
        if (func == CellFunc.DISTANCE) {
            return 0.25F;
        }
        return 1;
    }
}
