/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent.fancy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.rivermap.Rivermap;
import com.terraforged.mod.level.levelgen.rivermap.gen.GenRiver;
import com.terraforged.mod.level.levelgen.rivermap.river.BaseRiverGenerator;
import com.terraforged.mod.level.levelgen.rivermap.river.Network;
import com.terraforged.mod.level.levelgen.rivermap.river.River;
import com.terraforged.mod.level.levelgen.rivermap.river.RiverCarver;
import com.terraforged.mod.level.levelgen.rivermap.river.RiverConfig;
import com.terraforged.mod.level.levelgen.rivermap.river.RiverWarp;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.noise.util.Vec2f;
import com.terraforged.mod.util.Variance;
import com.terraforged.mod.util.pos.PosUtil;

public class FancyRiverGenerator extends BaseRiverGenerator<FancyContinentGenerator> {
    private static final float END_VALUE = 0.1f;
    private static final Variance MAIN_PADDING = Variance.of(0.05, END_VALUE);
    private static final Variance MAIN_JITTER = Variance.of(-0.2, 0.4);
    private final float freq;

    public FancyRiverGenerator(FancyContinentGenerator continent, GeneratorContext context) {
        super(continent, context);
        this.freq = 1.0f / (float)context.settings.world().continent().scale();
    }

    @Override
    public Rivermap generateRivers(int x, int z, long id) {
        Random random = new Random(id + (long)this.seed);
        GenRiver warp = new GenRiver((int)id, this.continentScale);
        ArrayList<Network> networks = new ArrayList<>(32);
        ArrayList<Network.Builder> roots = new ArrayList<Network.Builder>(16);
        for (Island island : ((FancyContinentGenerator)this.continent).getSource().getIslands()) {
            this.generateRoots(((FancyContinentGenerator)this.continent).getSource(), island, random, warp, roots);
            for (Network.Builder river : roots) {
                networks.add(river.build());
            }
            roots.clear();
        }
        return new Rivermap(x, z, networks.toArray(Network.NETWORKS), warp);
    }

    private void generateRoots(FancyContinent continent, Island island, Random random, GenRiver warp, List<Network.Builder> roots) {
        Segment[] segments = island.getSegments();
        int lineCount = Math.max(1, 8 - island.getId());
        int endCount = Math.max(4, 12 - island.getId());
        for (int i = 0; i < segments.length; ++i) {
            boolean end = i == 0 || i == segments.length - 1;
            Segment segment = segments[i];
            int riverCount = end ? lineCount - 1 : lineCount;
            this.collectSegmentRoots(continent, island, segment, riverCount, random, warp, roots);
        }
        Segment first = segments[0];
        this.collectPointRoots(continent, island, first.a, first.scaleA, endCount, random, warp, roots);
        Segment last = segments[segments.length - 1];
        this.collectPointRoots(continent, island, last.b, last.scaleB, endCount, random, warp, roots);
    }

    private void collectSegmentRoots(FancyContinent continent, Island island, Segment segment, int count, Random random, GenRiver warp, List<Network.Builder> roots) {
        float dx = segment.dx;
        float dy = segment.dy;
        float nx = dy / segment.length;
        float ny = -dx / segment.length;
        float stepSize = 1.0f / (float)(count + 2);
        for (int i = 0; i < count; ++i) {
            float progress = stepSize + stepSize * (float)i;
            if (progress > 1.0f) {
                return;
            }
            float startX = segment.a.x + dx * progress;
            float startZ = segment.a.y + dy * progress;
            float radiusScale = NoiseUtil.lerp(segment.scaleA, segment.scaleB, progress);
            float radius = island.coast() * radiusScale;
            int dir = random.nextBoolean() ? -1 : 1;
            float dirX = nx * (float)dir + MAIN_JITTER.next(random);
            float dirZ = ny * (float)dir + MAIN_JITTER.next(random);
            float scale = FancyRiverGenerator.getExtendScale(island.getId(), startX, startZ, dirX, dirZ, radius, continent);
            if (scale == 0.0f) continue;
            float startPad = MAIN_PADDING.next(random);
            float x1 = startX + (float)dir * dirX * radius * startPad;
            float y1 = startZ + (float)dir * dirZ * radius * startPad;
            float x2 = startX + dirX * radius * scale;
            float y2 = startZ + dirZ * radius * scale;
            this.addRoot(x1, y1, x2, y2, this.main, random, warp, roots);
        }
    }

    private void collectPointRoots(FancyContinent continent, Island island, Vec2f vec, float radiusScale, int count, Random random, GenRiver warp, List<Network.Builder> roots) {
        float yawStep = (float)Math.PI * 2 / (float)count;
        float radius = island.coast() * radiusScale;
        for (int i = 0; i < count; ++i) {
            float yaw = yawStep * (float)i;
            float dx = NoiseUtil.cos(yaw);
            float dz = NoiseUtil.sin(yaw);
            float scale = FancyRiverGenerator.getExtendScale(island.getId(), vec.x, vec.y, dx, dz, radius, continent);
            if (scale == 0.0f) continue;
            float startPad = MAIN_PADDING.next(random);
            float startX = vec.x + dx * startPad * radius;
            float startZ = vec.y + dz * startPad * radius;
            float endX = vec.x + dx * radius * scale;
            float endZ = vec.y + dz * radius * scale;
            if (continent.getValue(endX, endZ) > 0.1f) continue;
            this.addRoot(startX, startZ, endX, endZ, this.main, random, warp, roots);
        }
    }

    private void addRoot(float x1, float z1, float x2, float z2, RiverConfig config, Random random, GenRiver warp, List<Network.Builder> roots) {
        River river = new River(x1 / this.freq, z1 / this.freq, x2 / this.freq, z2 / this.freq);
        if (this.riverOverlaps(river, null, roots)) {
            return;
        }
        RiverCarver.Settings settings = FancyRiverGenerator.creatSettings(random);
        settings.fadeIn = config.fade;
        settings.valleySize = 275.0f * River.FORK_VALLEY.next(random);
        RiverWarp riverWarp = RiverWarp.create(0.1f, 0.85f, random);
        RiverCarver carver = new RiverCarver(river, riverWarp, config, settings, this.levels);
        Network.Builder network = Network.builder(carver);
        roots.add(network);
        this.generateForks(network, River.FORK_SPACING, this.fork, random, warp, roots, 0);
        this.generateWetlands(network, random);
    }

    private static float getExtendScale(int islandId, float startX, float startZ, float dx, float dz, float radius, FancyContinent continent) {
        float scale = 1.0f;
        for (int i = 0; i < 25; ++i) {
            float x = startX + dx * radius * scale;
            float z = startZ + dz * radius * scale;
            long packed = continent.getValueId(x, z);
            if (PosUtil.unpackLeft(packed) != islandId) {
                return 0.0f;
            }
            if (PosUtil.unpackRightf(packed) < 0.1f) {
                return scale;
            }
            scale += 0.075f;
        }
        return 0.0f;
    }
}

