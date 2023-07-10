/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.geology;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.terraforged.engine.Seed;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;

public class Strata<T> {
    private final List<Stratum<T>> strata;

    private Strata(List<Stratum<T>> strata) {
        this.strata = strata;
    }

    public <Context> boolean downwards(int x, int y, int z, Context context, Stratum.Visitor<T, Context> visitor) {
        try (Resource<DepthBuffer> buffer = DepthBuffer.get();){
            this.initBuffer(buffer.get(), x, z);
            boolean bl = this.downwards(x, y, z, buffer.get(), context, visitor);
            return bl;
        }
    }

    public <Context> boolean downwards(int x, int y, int z, DepthBuffer depthBuffer, Context ctx, Stratum.Visitor<T, Context> visitor) {
        this.initBuffer(depthBuffer, x, z);
        int py = y;
        T last = null;
        for (int i = 0; i < this.strata.size(); ++i) {
            float depth = depthBuffer.getDepth(i);
            int height = NoiseUtil.round(depth * (float)y);
            T value = this.strata.get(i).getValue();
            last = value;
            for (int dy = 0; dy < height; ++dy) {
                if (py <= y && !visitor.visit(py, value, ctx)) {
                    return false;
                }
                if (--py >= 0) continue;
                return false;
            }
        }
        if (last != null) {
            while (py > 0) {
                visitor.visit(py, last, ctx);
                --py;
            }
        }
        return true;
    }

    private void initBuffer(DepthBuffer buffer, int x, int z) {
        buffer.init(this.strata.size());
        for (int i = 0; i < this.strata.size(); ++i) {
            float depth = this.strata.get(i).getDepth(x, z);
            buffer.set(i, depth);
        }
    }

    public static <T> Builder<T> builder(int seed, com.terraforged.noise.source.Builder noise) {
        return new Builder<>(seed, noise);
    }

    public static class Builder<T> {
        private final Seed seed;
        private final com.terraforged.noise.source.Builder noise;
        private final List<Stratum<T>> strata = new LinkedList<Stratum<T>>();

        public Builder(int seed, com.terraforged.noise.source.Builder noise) {
            this.seed = new Seed(seed);
            this.noise = noise;
        }

        public Builder<T> add(T material, double depth) {
            Module module = this.noise.seed(this.seed.next()).perlin().scale(depth);
            this.strata.add(Stratum.of(material, module));
            return this;
        }

        public Builder<T> add(Source type, T material, double depth) {
            Module module = this.noise.seed(this.seed.next()).build(type).scale(depth);
            this.strata.add(Stratum.of(material, module));
            return this;
        }

        public Strata<T> build() {
            return new Strata<>(new ArrayList<Stratum<T>>(this.strata));
        }
    }
}

