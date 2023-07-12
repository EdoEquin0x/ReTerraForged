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

package com.terraforged.mod.noise.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.func.Interpolation;

/**
 * @author dags <dags@dags.me>
 */
public class VariableBlend extends Selector {
	public static final Codec<VariableBlend> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Module.CODEC.fieldOf("control").forGetter((s) -> s.control),
		Module.CODEC.fieldOf("variator").forGetter((s) -> s.variator),
		Module.CODEC.fieldOf("source0").forGetter((s) -> s.source0),
		Module.CODEC.fieldOf("source1").forGetter((s) -> s.source1),
		Codec.FLOAT.optionalFieldOf("midpoint", 0.5F).forGetter((s) -> s.midpoint),
		Codec.FLOAT.optionalFieldOf("blend_min", 0.0F).forGetter((s) -> s.minBlend),
		Codec.FLOAT.optionalFieldOf("blend_max", 1.0F).forGetter((s) -> s.maxBlend),
		Interpolation.CODEC.optionalFieldOf("interp", Interpolation.LINEAR).forGetter((s) -> s.interpolation)
	).apply(instance, VariableBlend::new));
	
    private final Module source0;
    private final Module source1;
    private final Module variator;
    private final float midpoint;
    private final float maxBlend;
    private final float minBlend;

    public VariableBlend(Module control, Module variator, Module source0, Module source1, float midpoint, float minBlend, float maxBlend, Interpolation interpolation) {
        super(control, new Module[]{ source0, source1 }, interpolation);
        this.source0 = source0;
        this.source1 = source1;
        this.midpoint = midpoint;
        this.maxBlend = maxBlend;
        this.minBlend = minBlend;
        this.variator = variator;
    }
    
    @Override
    protected float selectValue(float x, float y, float selector) {
        float radius = minBlend + variator.getValue(x, y) * maxBlend;

        float min = Math.max(0, midpoint - radius);
        if (selector < min) {
            return source0.getValue(x, y);
        }

        float max = Math.min(1, midpoint + radius);
        if (selector > max) {
            return source1.getValue(x, y);
        }

        float alpha = (selector - min) / (max - min);
        return blendValues(source0.getValue(x, y), source1.getValue(x, y), alpha);
    }
    
    @Override
	public Codec<VariableBlend> codec() {
		return CODEC;
	}
}
