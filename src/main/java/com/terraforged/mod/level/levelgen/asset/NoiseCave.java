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

package com.terraforged.mod.level.levelgen.asset;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.codec.LazyCodec;
import com.terraforged.mod.level.levelgen.cave.CaveType;
import com.terraforged.mod.level.levelgen.noise.NoiseCodec;
import com.terraforged.noise.Module;
import com.terraforged.noise.util.NoiseUtil;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

public class NoiseCave {
    public static final Codec<NoiseCave> DIRECT_CODEC = LazyCodec.record(instance -> instance.group(
    	CaveType.CODEC.fieldOf("type").forGetter(c -> c.type),
    	NoiseCodec.CODEC.fieldOf("elevation").forGetter(c -> c.elevation),
    	NoiseCodec.CODEC.fieldOf("shape").forGetter(c -> c.shape),
    	NoiseCodec.CODEC.fieldOf("floor").forGetter(c -> c.floor),
    	Codec.INT.fieldOf("size").forGetter(c -> c.size),
    	Codec.INT.optionalFieldOf("min_y", -32).forGetter(c -> c.minY),
    	Codec.INT.fieldOf("max_y").forGetter(c -> c.maxY)
    ).apply(instance, NoiseCave::new));
    public static final Codec<Holder<NoiseCave>> CODEC = RegistryFileCodec.create(TerraForged.CAVE, DIRECT_CODEC);

    private final CaveType type;
    private final Module elevation;
    private final Module shape;
    private final Module floor;
    private final int size;
    private final int minY;
    private final int maxY;
    private final int rangeY;

    public NoiseCave(CaveType type, Module elevation, Module shape, Module floor, int size, int minY, int maxY) {
        this.type = type;
        this.elevation = elevation;
        this.shape = shape;
        this.floor = floor;
        this.size = size;
        this.minY = minY;
        this.maxY = maxY;
        this.rangeY = maxY - minY;
    }

    public CaveType getType() {
        return this.type;
    }

    public int getHeight(int x, int z) {
        return getScaleValue(x, z, 1F, this.minY, this.rangeY, this.elevation);
    }

    public int getCavernSize(int x, int z, float modifier) {
        return getScaleValue(x, z, modifier, 0, this.size, this.shape);
    }

    public int getFloorDepth(int x, int z, int size) {
        return getScaleValue(x, z, 1F, 0, size, this.floor);
    }

    @Override
    public String toString() {
        return "NoiseCave{" +
                "type=" + this.type +
                ", elevation=" + this.elevation +
                ", shape=" + this.shape +
                ", floor=" + this.floor +
                ", size=" + this.size +
                ", minY=" + this.minY +
                ", maxY=" + this.maxY +
                ", rangeY=" + this.rangeY +
                '}';
    }

    private static int getScaleValue(int x, int z, float modifier, int min, int range, Module noise) {
        if (range <= 0) return 0;

        return min + NoiseUtil.floor(noise.getValue(x, z) * range * modifier);
    }
}
