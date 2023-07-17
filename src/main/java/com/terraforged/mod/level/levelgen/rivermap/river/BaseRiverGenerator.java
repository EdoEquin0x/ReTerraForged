/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.rivermap.river;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.terraforged.mod.level.levelgen.continent.Continent;
import com.terraforged.mod.level.levelgen.rivermap.RiverGenerator;
import com.terraforged.mod.level.levelgen.rivermap.Rivermap;
import com.terraforged.mod.level.levelgen.rivermap.gen.GenRiver;
import com.terraforged.mod.level.levelgen.rivermap.lake.Lake;
import com.terraforged.mod.level.levelgen.rivermap.lake.LakeConfig;
import com.terraforged.mod.level.levelgen.rivermap.wetland.Wetland;
import com.terraforged.mod.level.levelgen.rivermap.wetland.WetlandConfig;
import com.terraforged.mod.level.levelgen.settings.Levels;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.util.Seed;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.noise.util.Vec2f;
import com.terraforged.mod.util.Variance;
import com.terraforged.mod.util.pos.PosUtil;

public abstract class BaseRiverGenerator<T extends Continent> implements RiverGenerator {
    protected final int count;
    protected final int continentScale;
    protected final float minEdgeValue;
    protected final int seed;
    protected final LakeConfig lake;
    protected final RiverConfig main;
    protected final RiverConfig fork;
    protected final WetlandConfig wetland;
    protected final T continent;
    protected final Levels levels;

    public BaseRiverGenerator(T continent, Seed seed, Levels levels, Settings settings) {
        this.continent = continent;
        this.levels = levels;
        this.continentScale = settings.world().continent().scale();
        this.minEdgeValue = settings.world().controlPoints().inland();
        this.seed = seed.root() + settings.rivers().seedOffset();
        this.count = settings.rivers().riverCount();
        this.main = RiverConfig.builder(levels).bankHeight(settings.rivers().mainRivers().minBankHeight(), settings.rivers().mainRivers().maxBankHeight()).bankWidth(settings.rivers().mainRivers().bankWidth()).bedWidth(settings.rivers().mainRivers().bedWidth()).bedDepth(settings.rivers().mainRivers().bedDepth()).fade(settings.rivers().mainRivers().fade()).length(5000).main(true).order(0).build();
        this.fork = RiverConfig.builder(levels).bankHeight(settings.rivers().branchRivers().minBankHeight(), settings.rivers().branchRivers().maxBankHeight()).bankWidth(settings.rivers().branchRivers().bankWidth()).bedWidth(settings.rivers().branchRivers().bedWidth()).bedDepth(settings.rivers().branchRivers().bedDepth()).fade(settings.rivers().branchRivers().fade()).length(4500).order(1).build();
        this.wetland = new WetlandConfig(settings.rivers().wetlands());
        this.lake = LakeConfig.of(settings.rivers().lakes(), levels);
    }

    @Override
    public Rivermap generateRivers(int x, int z, long id) {
        Random random = new Random(id + (long)this.seed);
        GenRiver warp = new GenRiver((int)id, this.continentScale);
        List<Network.Builder> rivers = this.generateRoots(x, z, random, warp);
        Collections.shuffle(rivers, random);
        for (Network.Builder root : rivers) {
            this.generateForks(root, River.MAIN_SPACING, this.fork, random, warp, rivers, 0);
        }
        for (Network.Builder river : rivers) {
            this.generateWetlands(river, random);
        }
        Network[] networks = (Network[])rivers.stream().map(Network.Builder::build).toArray(Network[]::new);
        return new Rivermap(x, z, networks, warp);
    }

    public List<Network.Builder> generateRoots(int x, int z, Random random, GenRiver warp) {
        return Collections.emptyList();
    }

    public void generateForks(Network.Builder parent, Variance spacing, RiverConfig config, Random random, GenRiver warp, List<Network.Builder> rivers, int depth) {
        if (depth > 2) {
            return;
        }
        float length = 0.44f * parent.carver.river.length;
        if (length < 300.0f) {
            return;
        }
        int direction = random.nextBoolean() ? 1 : -1;
        for (float offset = 0.25f; offset < 0.9f; offset += spacing.next(random)) {
            boolean attempt = true;
            while (attempt) {
                float z2;
                float x2;
                float z1;
                direction = -direction;
                float parentAngle = parent.carver.river.getAngle();
                float forkAngle = (float)direction * ((float)Math.PI * 2) * River.FORK_ANGLE.next(random);
                float angle = parentAngle + forkAngle;
                float dx = NoiseUtil.sin(angle);
                float dz = NoiseUtil.cos(angle);
                long v1 = parent.carver.river.pos(offset);
                float x1 = PosUtil.unpackLeftf(v1);
                if (!(this.continent.getEdgeValue(x1, z1 = PosUtil.unpackRightf(v1)) < this.minEdgeValue) && !(this.continent.getEdgeValue(x2 = x1 - dx * length, z2 = z1 - dz * length) < this.minEdgeValue)) {
                    RiverConfig forkConfig = parent.carver.createForkConfig(offset, this.levels);
                    River river = new River(x2, z2, x1, z1);
                    if (!this.riverOverlaps(river, parent, rivers)) {
                        float valleyWidth = 275.0f * River.FORK_VALLEY.next(random);
                        RiverCarver.Settings settings = BaseRiverGenerator.creatSettings(random);
                        settings.fadeIn = config.fade;
                        settings.valleySize = valleyWidth;
                        RiverWarp forkWarp = parent.carver.warp.createChild(0.15f, 0.75f, 0.65f, random);
                        RiverCarver fork = new RiverCarver(river, forkWarp, forkConfig, settings, this.levels);
                        Network.Builder builder = Network.builder(fork);
                        parent.children.add(builder);
                        this.generateForks(builder, River.FORK_SPACING, config, random, warp, rivers, depth + 1);
                    }
                }
                attempt = false;
            }
        }
        this.addLake(parent, random, warp);
    }

    public void generateAdditionalLakes(int x, int z, Random random, List<Network.Builder> roots, List<RiverCarver> rivers, List<Lake> lakes) {
        float size = 150.0f;
        Variance sizeVariance = Variance.of(1.0, 0.25);
        Variance distanceVariance = Variance.of(0.6f, 0.3f);
        for (int i = 1; i < roots.size(); ++i) {
            Network.Builder a = roots.get(i - 1);
            float angle = 0.0f;
            float dx = NoiseUtil.sin(angle);
            float dz = NoiseUtil.cos(angle);
            float distance = distanceVariance.next(random);
            float lx = (float)x + dx * a.carver.river.length * distance;
            float lz = (float)z + dz * a.carver.river.length * distance;
            float variance = sizeVariance.next(random);
            Vec2f center = new Vec2f(lx, lz);
            if (this.lakeOverlaps(center, size, rivers)) continue;
            lakes.add(new Lake(center, size, variance, this.lake));
        }
    }

    public void generateWetlands(Network.Builder builder, Random random) {
        int skip = random.nextInt(this.wetland.skipSize);
        if (skip == 0) {
            float width = this.wetland.width.next(random);
            float length = this.wetland.length.next(random);
            float riverLength = builder.carver.river.length();
            float startPos = random.nextFloat() * 0.75f;
            float endPos = startPos + random.nextFloat() * (length / riverLength);
            long start = builder.carver.river.pos(startPos);
            long end = builder.carver.river.pos(endPos);
            float x1 = PosUtil.unpackLeftf(start);
            float z1 = PosUtil.unpackRightf(start);
            float x2 = PosUtil.unpackLeftf(end);
            float z2 = PosUtil.unpackRightf(end);
            builder.wetlands.add(new Wetland(random.nextInt(), new Vec2f(x1, z1), new Vec2f(x2, z2), width, this.levels));
        }
        for (Network.Builder child : builder.children) {
            this.generateWetlands(child, random);
        }
    }

    public void addLake(Network.Builder branch, Random random, GenRiver warp) {
        if (random.nextFloat() <= this.lake.chance) {
            float cx = branch.carver.river.x1;
            float cz = branch.carver.river.z1;
            float lakeSize = this.lake.sizeMin + random.nextFloat() * this.lake.sizeRange;
            if (this.lakeOverlapsOther(cx, cz, lakeSize, branch.lakes)) {
                return;
            }
            branch.lakes.add(new Lake(new Vec2f(cx, cz), lakeSize, 1.0f, this.lake));
        }
    }

    public boolean riverOverlaps(River river, Network.Builder parent, List<Network.Builder> rivers) {
        for (Network.Builder other : rivers) {
            if (!other.overlaps(river, parent, 250.0f)) continue;
            return true;
        }
        return false;
    }

    public boolean lakeOverlaps(Vec2f lake, float size, List<RiverCarver> rivers) {
        for (RiverCarver other : rivers) {
            if (other.main || !other.river.overlaps(lake, size)) continue;
            return true;
        }
        return false;
    }

    public boolean lakeOverlapsOther(float x, float z, float size, List<Lake> lakes) {
        float dist2 = size * size;
        for (Lake other : lakes) {
            if (!other.overlaps(x, z, dist2)) continue;
            return true;
        }
        return false;
    }

    public static RiverCarver create(float x1, float z1, float x2, float z2, RiverConfig config, Levels levels, Random random) {
        River river = new River(x1, z1, x2, z2);
        RiverWarp warp = RiverWarp.create(0.35f, random);
        float valleyWidth = 275.0f * River.MAIN_VALLEY.next(random);
        RiverCarver.Settings settings = BaseRiverGenerator.creatSettings(random);
        settings.fadeIn = config.fade;
        settings.valleySize = valleyWidth;
        return new RiverCarver(river, warp, config, settings, levels);
    }

    public static RiverCarver createFork(float x1, float z1, float x2, float z2, float valleyWidth, RiverConfig config, Levels levels, Random random) {
        River river = new River(x1, z1, x2, z2);
        RiverWarp warp = RiverWarp.create(0.4f, random);
        RiverCarver.Settings settings = BaseRiverGenerator.creatSettings(random);
        settings.fadeIn = config.fade;
        settings.valleySize = valleyWidth;
        return new RiverCarver(river, warp, config, settings, levels);
    }

    public static RiverCarver.Settings creatSettings(Random random) {
        RiverCarver.Settings settings = new RiverCarver.Settings();
        settings.valleyCurve = RiverCarver.getValleyType(random);
        return settings;
    }
}

