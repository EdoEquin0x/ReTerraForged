package com.terraforged.mod.util;

import java.util.function.IntFunction;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;

public class RegistryUtil {

	public static <T> T[] values(HolderLookup<T> registry, IntFunction<T[]> newArray) {
		return registry.listElements().map(Holder::get).toArray(newArray);
	}
}
