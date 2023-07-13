/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.terrain;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Deprecated(forRemoval = true)
public class TerrainType {
    private static final Object lock = new Object();
    private static final List<Terrain> REGISTRY = new CopyOnWriteArrayList<Terrain>();
    public static final Terrain NONE = TerrainType.register("none", TerrainCategory.NONE);
    public static final Terrain DEEP_OCEAN = TerrainType.register("deep_ocean", TerrainCategory.DEEP_OCEAN);
    public static final Terrain SHALLOW_OCEAN = TerrainType.register("ocean", TerrainCategory.SHALLOW_OCEAN);
    public static final Terrain COAST = TerrainType.register("coast", TerrainCategory.COAST);
    public static final Terrain BEACH = TerrainType.register("beach", TerrainCategory.BEACH);
    public static final Terrain RIVER = TerrainType.register("river", TerrainCategory.RIVER);
    public static final Terrain LAKE = TerrainType.register("lake", TerrainCategory.LAKE);
    public static final Terrain WETLAND = TerrainType.registerWetlands("wetland", TerrainCategory.WETLAND);
    public static final Terrain FLATS = TerrainType.register("flats", TerrainCategory.FLATLAND);
    public static final Terrain BADLANDS = TerrainType.registerBadlands("badlands", TerrainCategory.FLATLAND);
    public static final Terrain PLATEAU = TerrainType.register("plateau", TerrainCategory.LOWLAND);
    public static final Terrain HILLS = TerrainType.register("hills", TerrainCategory.LOWLAND);
    public static final Terrain MOUNTAINS = TerrainType.registerMountain("mountains", TerrainCategory.HIGHLAND);
    public static final Terrain MOUNTAIN_CHAIN = TerrainType.registerMountain("mountain_chain", TerrainCategory.HIGHLAND);
    public static final Terrain VOLCANO = TerrainType.registerVolcano("volcano", TerrainCategory.HIGHLAND);
    public static final Terrain VOLCANO_PIPE = TerrainType.registerVolcano("volcano_pipe", TerrainCategory.HIGHLAND);

    public static void forEach(Consumer<Terrain> action) {
        REGISTRY.forEach(action);
    }

    public static Optional<Terrain> find(Predicate<Terrain> filter) {
        return REGISTRY.stream().filter(filter).findFirst();
    }

    public static Terrain get(String name) {
        for (Terrain terrain : REGISTRY) {
            if (!terrain.getName().equalsIgnoreCase(name)) continue;
            return terrain;
        }
        return null;
    }
    
    public static Terrain getOrCreate(String name, Terrain terrain) {
    	Terrain present = get(name);
    	if(present != null) {
    		return present;
    	} else {
    		return register(terrain);
    	}
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
            Terrain current = TerrainType.get(min.getName() + "-" + max.getName());
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
    private static Terrain register(String name, TerrainCategory type) {
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
    private static Terrain registerWetlands(String name, TerrainCategory type) {
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
    private static Terrain registerBadlands(String name, TerrainCategory type) {
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
    private static Terrain registerMountain(String name, TerrainCategory type) {
        Object object = lock;
        synchronized (object) {
            ConfiguredTerrain terrain = new ConfiguredTerrain(REGISTRY.size(), name, type, true, true);
            REGISTRY.add(terrain);
            return terrain;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Terrain registerVolcano(String name, TerrainCategory type) {
        Object object = lock;
        synchronized (object) {
            ConfiguredTerrain terrain = new ConfiguredTerrain(REGISTRY.size(), name, type, true, true){

                @Override
                public boolean isVolcano() {
                    return true;
                }

                @Override
                public boolean overridesCoast() {
                    return true;
                }
            };
            REGISTRY.add(terrain);
            return terrain;
        }
    }
}

