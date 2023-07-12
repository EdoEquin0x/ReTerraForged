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

package com.terraforged.mod.noise.domain;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.codec.TFCodecs;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;

public interface Domain {
	public static final Codec<Domain> CODEC = TFCodecs.registryCodec(TerraForged.DOMAIN, Domain::codec);

    Domain DIRECT = new Domain() {

        @Override
        public float getOffsetX(float x, float y) {
            return 0;
        }

        @Override
        public float getOffsetY(float x, float y) {
            return 0;
        }
        
        @Override
        public Codec<? extends Domain> codec() {
        	return Codec.unit(this);
        }
    };

    float getOffsetX(float x, float y);

    float getOffsetY(float x, float y);

    Codec<? extends Domain> codec();
    
    default float getX(float x, float y) {
        return x + getOffsetX(x, y);
    }

    default float getY(float x, float y) {
        return y + getOffsetY(x, y);
    }

    default Domain cache() {
        return new CacheWarp(this);
    }

    default Domain add(Domain next) {
        return new AddWarp(this, next);
    }

    default Domain warp(Domain next) {
        return new CompoundWarp(this, next);
    }

    default Domain then(Domain next) {
        return new CumulativeWarp(this, next);
    }

    static Domain warp(Module x, Module y, Module distance) {
        return new DomainWarp(x, y, distance);
    }

    static Domain warp(int seed, int scale, int octaves, double strength) {
        return warp(Source.PERLIN, seed, scale, octaves, strength);
    }

    static Domain warp(Source type, int seed, int scale, int octaves, double strength) {
        return warp(
                Source.build(seed, scale, octaves).build(type),
                Source.build(seed + 1, scale, octaves).build(type),
                Source.constant(strength)
        );
    }

    static Domain direction(Module direction, Module distance) {
        return new DirectionWarp(direction, distance);
    }

    static Domain direction(int seed, int scale, int octaves, double strength) {
        return direction(Source.PERLIN, seed, scale, octaves, strength);
    }

    static Domain direction(Source type, int seed, int scale, int octaves, double strength) {
        return direction(
                Source.build(seed, scale, octaves).build(type),
                Source.constant(strength)
        );
    }
}
