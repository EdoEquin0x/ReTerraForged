package com.terraforged.mod.registry;

import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.noise.func.CurveFunc;
import com.terraforged.mod.noise.func.Interpolation;
import com.terraforged.mod.noise.func.MidPointCurve;
import com.terraforged.mod.noise.func.SCurve;

import net.minecraft.resources.ResourceKey;

public interface TFCurves {
	ResourceKey<Codec<? extends CurveFunc>> INTERPOLATION = resolve("interpolation");
	ResourceKey<Codec<? extends CurveFunc>> MID_POINT_CURVE = resolve("mid_point_curve");
	ResourceKey<Codec<? extends CurveFunc>> SCURVE = resolve("scurve");
	
	static void register(BiConsumer<ResourceKey<Codec<? extends CurveFunc>>, Codec<? extends CurveFunc>> register) {
		register.accept(INTERPOLATION, Interpolation.CODEC);
		register.accept(MID_POINT_CURVE, MidPointCurve.CODEC);
		register.accept(SCURVE, SCurve.CODEC);
	}
	
	private static ResourceKey<Codec<? extends CurveFunc>> resolve(String path) {
		return TerraForged.resolve(TFRegistries.CURVE_TYPE, path);
	}
}
