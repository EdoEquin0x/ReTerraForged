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

public class Modulate extends Modifier {
	public static final Codec<Modulate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Module.DIRECT_CODEC.fieldOf("source").forGetter((m) -> m.source),
		Module.DIRECT_CODEC.fieldOf("direction").forGetter((m) -> m.direction),
		Module.DIRECT_CODEC.fieldOf("strength").forGetter((m) -> m.strength)
	).apply(instance, Modulate::new));
	
    private final Module direction;
    private final Module strength;

    public Modulate(Module source, Module direction, Module strength) {
        super(source);
        this.direction = direction;
        this.strength = strength;
    }

    @Override
    public float getValue(float x, float y) {
        float angle = direction.getValue(x, y) * NoiseUtil.PI2;
        float strength = this.strength.getValue(x, y);
        float dx = NoiseUtil.sin(angle) * strength;
        float dy = NoiseUtil.cos(angle) * strength;
        return source.getValue(x + dx, y + dy);
    }

    @Override
    public float modify(float x, float y, float noiseValue) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Modulate modulate = (Modulate) o;

        if (!direction.equals(modulate.direction)) return false;
        return strength.equals(modulate.strength);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + direction.hashCode();
        result = 31 * result + strength.hashCode();
        return result;
    }
    
    @Override
	public Codec<Modulate> codec() {
		return CODEC;
	}
}
