/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.rivermap.gen;

import com.terraforged.mod.noise.domain.Domain;

public class GenRiver {
	public static final GenRiver EMPTY = new GenRiver();
	public final Domain lake;
	public final Domain river;

	private GenRiver() {
		this.lake = Domain.DIRECT;
		this.river = Domain.DIRECT;
	}

	public GenRiver(int seed, int continentScale) {
		this.lake = Domain.warp(++seed, 200, 1, 300.0).add(Domain.warp(++seed, 50, 2, 50.0));
		this.river = Domain.warp(++seed, 95, 1, 25.0).add(Domain.warp(++seed, 16, 1, 5.0));
	}
}
