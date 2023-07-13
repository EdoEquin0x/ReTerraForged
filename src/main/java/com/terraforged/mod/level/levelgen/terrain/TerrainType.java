/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.terraforged.mod.TerraForged;

import net.minecraft.resources.ResourceLocation;

@Deprecated(forRemoval = true)
public class TerrainType {
    private static final Object lock = new Object();
    private static final List<Terrain> REGISTRY = new CopyOnWriteArrayList<Terrain>();
    public static final Terrain NONE = TerrainType.register(TerraForged.location("none"), TerrainCategory.NONE);
    public static final Terrain DEEP_OCEAN = TerrainType.register(TerraForged.location("deep_ocean"), TerrainCategory.DEEP_OCEAN);
    public static final Terrain SHALLOW_OCEAN = TerrainType.register(TerraForged.location("ocean"), TerrainCategory.SHALLOW_OCEAN);
    public static final Terrain COAST = TerrainType.register(TerraForged.location("coast"), TerrainCategory.COAST);
    public static final Terrain BEACH = TerrainType.register(TerraForged.location("beach"), TerrainCategory.BEACH);
    public static final Terrain RIVER = TerrainType.register(TerraForged.location("river"), TerrainCategory.RIVER);
    public static final Terrain LAKE = TerrainType.register(TerraForged.location("lake"), TerrainCategory.LAKE);
    public static final Terrain WETLAND = TerrainType.registerWetlands(TerraForged.location("wetland"), TerrainCategory.WETLAND);
    public static final Terrain FLATS = TerrainType.register(TerraForged.location("flats"), TerrainCategory.FLATLAND);
    public static final Terrain BADLANDS = TerrainType.registerBadlands(TerraForged.location("badlands"), TerrainCategory.FLATLAND);
    public static final Terrain PLATEAU = TerrainType.register(TerraForged.location("plateau"), TerrainCategory.LOWLAND);
    public static final Terrain HILLS = TerrainType.register(TerraForged.location("hills"), TerrainCategory.LOWLAND);
    public static final Terrain MOUNTAINS = TerrainType.registerMountain(TerraForged.location("mountains"), TerrainCategory.HIGHLAND);
    public static final Terrain MOUNTAIN_CHAIN = TerrainType.registerMountain(TerraForged.location("mountain_chain"), TerrainCategory.HIGHLAND);
    
    public static final Codec<Terrain> CODEC = ResourceLocation.CODEC.flatXmap((loc) -> {
    	return Optional.ofNullable(get(loc)).map(DataResult::success).orElseGet(() -> {
    		return DataResult.error(() -> {
    			return "Unknown registry key: " + loc;
    		});
    	});
    }, (terrain) -> {
    	return DataResult.success(terrain.getName());
    });
      
    
    public static void forEach(Consumer<Terrain> action) {
        REGISTRY.forEach(action);
    }

    public static Optional<Terrain> find(Predicate<Terrain> filter) {
        return REGISTRY.stream().filter(filter).findFirst();
    }

    public static Terrain get(ResourceLocation name) {
        for (Terrain terrain : REGISTRY) {
            if (!terrain.getName().equals(name)) continue;
            return terrain;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Terrain get(int id) {
        Object object = lock;
        synchronized (object) {
            if (id >= 0 && id < REGISTRY.size()) {
                return REGISTRY.get(id);
            }
            return NONE;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Terrain register(Terrain instance) {
        Object object = lock;
        synchronized (object) {
            Terrain current = TerrainType.get(instance.getName());
            if (current != null) {
                return current;
            }
            Terrain terrain = instance.withId(REGISTRY.size());
            REGISTRY.add(terrain);
            return terrain;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Terrain registerComposite(Terrain a, Terrain b) {
        if (a == b) {
            return a;
        }
        Object object = lock;
        synchronized (object) {
            Terrain min = a.getId() < b.getId() ? a : b;
            Terrain max = a.getId() > b.getId() ? a : b;
            Terrain current = TerrainType.get(new ResourceLocation(TerraForged.MODID, min.getName() + "-" + max.getName()));
            if (current != null) {
                return current;
            }
            CompositeTerrain mix = new CompositeTerrain(REGISTRY.size(), min, max);
            REGISTRY.add(mix);
            return mix;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Terrain register(ResourceLocation name, TerrainCategory type) {
        Object object = lock;
        synchronized (object) {
            Terrain terrain = new Terrain(REGISTRY.size(), name, type);
            REGISTRY.add(terrain);
            return terrain;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Terrain registerWetlands(ResourceLocation name, TerrainCategory type) {
        Object object = lock;
        synchronized (object) {
            ConfiguredTerrain terrain = new ConfiguredTerrain(REGISTRY.size(), name, type, true);
            REGISTRY.add(terrain);
            return terrain;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Terrain registerBadlands(ResourceLocation name, TerrainCategory type) {
        Object object = lock;
        synchronized (object) {
            ConfiguredTerrain terrain = new ConfiguredTerrain(REGISTRY.size(), name, type, 0.3f);
            REGISTRY.add(terrain);
            return terrain;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Terrain registerMountain(ResourceLocation name, TerrainCategory type) {
        Object object = lock;
        synchronized (object) {
            ConfiguredTerrain terrain = new ConfiguredTerrain(REGISTRY.size(), name, type, true);
            REGISTRY.add(terrain);
            return terrain;
        }
    }
}

