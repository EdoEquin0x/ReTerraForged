/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.worldgen.biome.vegetation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.terraforged.mod.worldgen.asset.VegetationConfig;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class VegetationFeatures {
    public static VegetationFeatures NONE = new VegetationFeatures();
    public static final int STAGE = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();

    private final PlacedFeature[] trees;
    private final PlacedFeature[] grass;
    private final PlacedFeature[] other;

    private VegetationFeatures() {
        trees = new PlacedFeature[0];
        grass = new PlacedFeature[0];
        other = new PlacedFeature[0];
    }

    public VegetationFeatures(List<PlacedFeature> trees, List<PlacedFeature> grass, List<PlacedFeature> other) {
        this.trees = trees.toArray(PlacedFeature[]::new);
        this.grass = grass.toArray(PlacedFeature[]::new);
        this.other = other.toArray(PlacedFeature[]::new);
    }

    public PlacedFeature[] trees() {
        return trees;
    }

    public PlacedFeature[] grass() {
        return grass;
    }

    public PlacedFeature[] other() {
        return other;
    }

    public static VegetationFeatures create(Biome biome, VegetationConfig config) {
        var trees = new ArrayList<PlacedFeature>();
        var grass = new ArrayList<PlacedFeature>();
        var other = new ArrayList<PlacedFeature>();

        var features = biome.getGenerationSettings().features();
        if (features.size() > STAGE) {
            var vegetation = features.get(STAGE);

            for (var feature : vegetation) {
//TODO
//            	var path = featureKey.toString();
//            	if (matches(path, TREE_KEYWORDS)) {
//            		trees.add(unwrap(feature, TREE_EXCLUSIONS, custom));
//            	} else if (matches(path, GRASS_KEYWORDS)) {
//            		grass.add(feature.value());
//                } else {
                	other.add(feature.value());
//                }
            }
        }

        return new VegetationFeatures(trees, grass, other);
    }

    protected static boolean matches(String name, String[] keywords) {
        for (var keyword : keywords) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static PlacedFeature unwrap(Holder<PlacedFeature> supplier, Set<PlacementModifierType<?>> exclusions, boolean custom) {
        if (!custom) return supplier.value();

        try {
            PlacedFeature placed = supplier.value();
            var placements = new ArrayList<>(placed.placement());
            placements.removeIf(placement -> exclusions.contains(placement.type()));
            return new PlacedFeature(placed.feature(), placements);
        } catch (Throwable t) {
            t.printStackTrace();
            return supplier.value();
        }
    }
}
