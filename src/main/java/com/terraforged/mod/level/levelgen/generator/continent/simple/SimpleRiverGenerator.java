/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.continent.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.generator.continent.SimpleContinent;
import com.terraforged.mod.level.levelgen.rivermap.gen.GenWarp;
import com.terraforged.mod.level.levelgen.rivermap.river.BaseRiverGenerator;
import com.terraforged.mod.level.levelgen.rivermap.river.Network;
import com.terraforged.mod.level.levelgen.rivermap.river.River;
import com.terraforged.mod.level.levelgen.rivermap.river.RiverCarver;
import com.terraforged.mod.level.levelgen.rivermap.river.RiverWarp;
import com.terraforged.mod.noise.util.NoiseUtil;

public class SimpleRiverGenerator extends BaseRiverGenerator<SimpleContinent> {
    public SimpleRiverGenerator(SimpleContinent continent, GeneratorContext context) {
        super(continent, context);
    }

    @Override
    public List<Network.Builder> generateRoots(int x, int z, Random random, GenWarp warp) {
        float start = random.nextFloat();
        float spacing = (float)Math.PI * 2 / (float)this.count;
        float spaceVar = spacing * 0.75f;
        float spaceBias = -spaceVar / 2.0f;
        ArrayList<Network.Builder> roots = new ArrayList<Network.Builder>(this.count);
        for (int i = 0; i < this.count; ++i) {
            float variance = random.nextFloat() * spaceVar + spaceBias;
            float angle = start + spacing * (float)i + variance;
            float dx = NoiseUtil.sin(angle);
            float dz = NoiseUtil.cos(angle);
            float startMod = 0.05f + random.nextFloat() * 0.45f;
            float length = ((SimpleContinent)this.continent).getDistanceToOcean(x, z, dx, dz);
            float startDist = Math.max(400.0f, startMod * length);
            float x1 = (float)x + dx * startDist;
            float z1 = (float)z + dz * startDist;
            float x2 = (float)x + dx * length;
            float z2 = (float)z + dz * length;
            float valleyWidth = 275.0f * River.MAIN_VALLEY.next(random);
            River river = new River((int)x1, (int)z1, (int)x2, (int)z2);
            RiverCarver.Settings settings = SimpleRiverGenerator.creatSettings(random);
            settings.fadeIn = this.main.fade;
            settings.valleySize = valleyWidth;
            RiverWarp riverWarp = RiverWarp.create(0.1f, 0.85f, random);
            RiverCarver carver = new RiverCarver(river, riverWarp, this.main, settings, this.levels);
            Network.Builder branch = Network.builder(carver);
            roots.add(branch);
            this.addLake(branch, random, warp);
        }
        return roots;
    }
}

