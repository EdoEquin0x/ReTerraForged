/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.rivermap.lake;

import com.terraforged.mod.level.levelgen.settings.Levels;
import com.terraforged.mod.level.levelgen.settings.RiverSettings;

public class LakeConfig {
	public final float depth;
	public final float chance;
	public final float sizeMin;
	public final float sizeMax;
	public final float sizeRange;
	public final float bankMin;
	public final float bankMax;
	public final float distanceMin;
	public final float distanceMax;

	private LakeConfig(Builder builder) {
		this.depth = builder.depth;
		this.chance = builder.chance;
		this.sizeMin = builder.sizeMin;
		this.sizeMax = builder.sizeMax;
		this.sizeRange = this.sizeMax - this.sizeMin;
		this.bankMin = builder.bankMin;
		this.bankMax = builder.bankMax;
		this.distanceMin = builder.distanceMin;
		this.distanceMax = builder.distanceMax;
	}

	public static LakeConfig of(RiverSettings.Lake settings, Levels levels) {
		Builder builder = new Builder();
		builder.chance = settings.chance();
		builder.sizeMin = settings.sizeMin();
		builder.sizeMax = settings.sizeMax();
		builder.depth = levels.water(-settings.depth());
		builder.distanceMin = settings.minStartDistance();
		builder.distanceMax = settings.maxStartDistance();
		builder.bankMin = levels.water(settings.minBankHeight());
		builder.bankMax = levels.water(settings.maxBankHeight());
		return new LakeConfig(builder);
	}

	public static class Builder {
		public float chance;
		public float depth = 10.0f;
		public float sizeMin = 30.0f;
		public float sizeMax = 100.0f;
		public float bankMin = 1.0f;
		public float bankMax = 8.0f;
		public float distanceMin = 0.025f;
		public float distanceMax = 0.05f;
	}
}
