package com.terraforged.mod.level.levelgen.continent.river2;

import com.terraforged.mod.util.storage.LongCache;
import com.terraforged.mod.util.storage.LossyCache;
import com.terraforged.mod.util.storage.ObjectPool;

//TODO
public class RiverSampler {
    private static final int RIVER_CACHE_SIZE = 1024;
    private final ObjectPool<RiverTree> riverPool;
    // rivers per cell
    private final LongCache<RiverTree> riverCache;

	public RiverSampler() {
		this.riverPool = ObjectPool.forCacheSize(RIVER_CACHE_SIZE, RiverTree::new);
		this.riverCache = LossyCache.concurrent(RIVER_CACHE_SIZE, RiverTree[]::new, this.riverPool);
	}
}
