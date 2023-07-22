package com.terraforged.mod.registry.data;

import com.terraforged.mod.TerraForged;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

public interface TFDimensionTypes {
	public static final ResourceKey<DimensionType> VANILLA = resolve("vanilla");
	
	static void register(BootstapContext<DimensionType> ctx) {
//		final int worldHeight = 1024;
//		ctx.register(VANILLA, new DimensionType(OptionalLong.empty(), true, false, false, true, 1.0D, true, false, -64, worldHeight, worldHeight, BlockTags.INFINIBURN_OVERWORLD, BuiltinDimensionTypes.OVERWORLD_EFFECTS, 0.0F, new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0)));
    }

	private static ResourceKey<DimensionType> resolve(String key) {
		return TerraForged.resolve(Registries.DIMENSION_TYPE, key);
	}
}
