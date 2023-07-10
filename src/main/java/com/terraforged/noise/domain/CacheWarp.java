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

package com.terraforged.noise.domain;

import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.value.DataObject;

public class CacheWarp implements Domain {

    private final Domain domain;
    private boolean cached = false;
    private float cachedX;
    private float cachedY;
    private float x;
    private float y;

    public CacheWarp(Domain domain) {
        this.domain = domain;
    }

    @Override
    public String getSpecName() {
        return "CacheWarp";
    }

    @Override
    public float getOffsetX(float x, float y) {
        if (cached && x == this.x && y == this.y) {
            return cachedX;
        }
        this.x = x;
        this.y = y;
        this.cachedX = domain.getOffsetX(x, y);
        return cachedX;
    }

    @Override
    public float getOffsetY(float x, float y) {
        if (cached && x == this.x && y == this.y) {
            return cachedY;
        }
        this.x = x;
        this.y = y;
        this.cachedY = domain.getOffsetY(x, y);
        return cachedY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheWarp cacheWarp = (CacheWarp) o;

        if (cached != cacheWarp.cached) return false;
        if (Float.compare(cacheWarp.cachedX, cachedX) != 0) return false;
        if (Float.compare(cacheWarp.cachedY, cachedY) != 0) return false;
        if (Float.compare(cacheWarp.x, x) != 0) return false;
        if (Float.compare(cacheWarp.y, y) != 0) return false;
        return domain.equals(cacheWarp.domain);
    }

    @Override
    public int hashCode() {
        int result = domain.hashCode();
        result = 31 * result + (cached ? 1 : 0);
        result = 31 * result + (cachedX != +0.0f ? Float.floatToIntBits(cachedX) : 0);
        result = 31 * result + (cachedY != +0.0f ? Float.floatToIntBits(cachedY) : 0);
        result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    private static CacheWarp create(DataObject data, DataSpec<?> spec, Context context) {
        return new CacheWarp(spec.get("domain", data, Domain.class, context));
    }

    public static DataSpec<? extends Domain> spec() {
        return DataSpec.builder("CacheWarp", CacheWarp.class, CacheWarp::create)
                .addObj("domain", Domain.class, w -> w.domain)
                .build();
    }
}
