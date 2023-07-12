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

package com.terraforged.mod.noise.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.util.NoiseUtil;

public class VariableCurve extends Modifier {
	public static final Codec<VariableCurve> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Module.CODEC.fieldOf("source").forGetter((m) -> m.source),
		Module.CODEC.fieldOf("midpoint").forGetter((m) -> m.midpoint),
		Module.CODEC.fieldOf("gradient").forGetter((m) -> m.gradient)
	).apply(instance, VariableCurve::new));

    private final Module midpoint;
    private final Module gradient;

    public VariableCurve(Module source, Module mid, Module gradient) {
        super(source);
        this.midpoint = mid;
        this.gradient = gradient;
    }

    @Override
    public float modify(float x, float y, float noiseValue) {
        float mid = midpoint.getValue(x, y);
        float curve = gradient.getValue(x, y);
        return NoiseUtil.curve(noiseValue, mid, curve);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        VariableCurve that = (VariableCurve) o;

        if (!midpoint.equals(that.midpoint)) return false;
        return gradient.equals(that.gradient);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + midpoint.hashCode();
        result = 31 * result + gradient.hashCode();
        return result;
    }
    
    @Override
	public Codec<VariableCurve> codec() {
		return CODEC;
	}
}