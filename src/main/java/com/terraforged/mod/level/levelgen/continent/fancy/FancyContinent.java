/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.continent.fancy;

import java.util.Random;

import com.terraforged.mod.level.levelgen.rivermap.RiverGenerator;
import com.terraforged.mod.level.levelgen.rivermap.Rivermap;
import com.terraforged.mod.level.levelgen.settings.ControlPoints;
import com.terraforged.mod.level.levelgen.settings.Levels;
import com.terraforged.mod.level.levelgen.settings.Settings;
import com.terraforged.mod.level.levelgen.util.Seed;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.noise.util.Vec2f;
import com.terraforged.mod.util.pos.PosUtil;

public class FancyContinent implements RiverGenerator {
    private final Island[] islands;
    private final FancyRiverGenerator riverGenerator;

    public FancyContinent(int nodes, float radius, Seed seed, Levels levels, Settings settings, FancyContinentGenerator continent) {
        ControlPoints controlPoints = new ControlPoints(settings.world().controlPoints());
        this.islands = FancyContinent.generateIslands(controlPoints, 3, nodes, radius, new Random(seed.next()));
        this.riverGenerator = new FancyRiverGenerator(continent, seed, levels, settings);
    }

    public float getValue(float x, float y) {
        float value = 0.0f;
        for (Island island : this.islands) {
            float v = island.getEdgeValue(x, y);
            value = Math.max(v, value);
        }
        return FancyContinent.process(value);
    }

    public Island getMain() {
        return this.islands[0];
    }

    public Island[] getIslands() {
        return this.islands;
    }

    public long getMin() {
        float x = Float.MAX_VALUE;
        float z = Float.MAX_VALUE;
        for (Island island : this.islands) {
            x = Math.min(x, island.getMin().x);
            z = Math.min(z, island.getMin().y);
        }
        return PosUtil.packf(x, z);
    }

    public long getMax() {
        float x = Float.MIN_VALUE;
        float z = Float.MIN_VALUE;
        for (Island island : this.islands) {
            x = Math.max(x, island.getMin().x);
            z = Math.max(z, island.getMin().y);
        }
        return PosUtil.packf(x, z);
    }

    public float getLandValue(float x, float y) {
        float value = 0.0f;
        for (Island island : this.islands) {
            float v = island.getLandValue(x, y);
            value = Math.max(v, value);
        }
        return value;
    }

    public long getValueId(float x, float y) {
        int id = -1;
        float value = 0.0f;
        for (Island island : this.islands) {
            float v = island.getEdgeValue(x, y);
            if (v > value) {
                value = v;
                id = island.getId();
            }
            value = Math.max(v, value);
        }
        return PosUtil.packMix(id, value);
    }

    @Override
    public Rivermap generateRivers(int x, int z, long id) {
        return this.riverGenerator.generateRivers(x, z, id);
    }

    private static float process(float value) {
        return value;
    }

    private static Island[] generateIslands(ControlPoints controlPoints, int islandCount, int nodeCount, float radius, Random random) {
        int dirs = 4;
        Island main = FancyContinent.generate(0, controlPoints, nodeCount, radius, random);
        Island[] islands = new Island[1 + islandCount * dirs];
        islands[0] = main;
        int i = 1;
        float yawStep = 1.0f / (float)dirs * ((float)Math.PI * 2);
        for (int dir = 0; dir < dirs; ++dir) {
            Island previous = main;
            int nCount = Math.max(2, nodeCount - 1);
            float r = radius * 0.5f;
            float yaw = yawStep * (float)dir + random.nextFloat() * yawStep;
            for (int island = 0; island < islandCount; ++island) {
                Island next = FancyContinent.generate(i, controlPoints, nCount, r, random);
                float y = yaw + FancyContinent.nextFloat(random, -0.2f, 0.2f);
                float distance = previous.radius();
                float dx = NoiseUtil.sin(y * ((float)Math.PI * 2)) * distance;
                float dz = NoiseUtil.cos(y * ((float)Math.PI * 2)) * distance;
                float ox = previous.getCenter().x + dx;
                float oy = previous.getCenter().y + dz;
                next.translate(new Vec2f(ox, oy));
                nCount = Math.max(2, nCount - 1);
                r *= 0.8f;
                islands[i++] = next;
                previous = next;
            }
        }
        return islands;
    }

    private static Island generate(int id, ControlPoints controlPoints, int nodes, float radius, Random random) {
        float minScale = 0.75f;
        float maxScale = 2.5f;
        float minLen = radius * 1.5f;
        float maxLen = radius * 3.5f;
        float maxYaw = 1.5707964f;
        float minYaw = -maxYaw;
        Segment[] segments = new Segment[nodes - 1];
        Vec2f pointA = new Vec2f(0.0f, 0.0f);
        float scaleA = FancyContinent.nextFloat(random, minScale, maxScale);
        float previousYaw = FancyContinent.nextFloat(random, 0.0f, (float)Math.PI * 2);
        for (int i = 0; i < segments.length; ++i) {
            float length = FancyContinent.nextFloat(random, minLen, maxLen);
            float yaw = previousYaw + FancyContinent.nextFloat(random, minYaw, maxYaw);
            float dx = NoiseUtil.sin(yaw) * length;
            float dz = NoiseUtil.cos(yaw) * length;
            Vec2f pointB = new Vec2f(pointA.x + dx, pointA.y + dz);
            float scaleB = FancyContinent.nextFloat(random, minScale, maxScale);
            segments[i] = new Segment(pointA, pointB, scaleA, scaleB);
            previousYaw = yaw;
            pointA = pointB;
            scaleA = scaleB;
        }
        return new Island(id, segments, controlPoints, radius * 3.0f, radius * 1.25f, radius, radius * 0.975f);
    }

    public static float nextFloat(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}

