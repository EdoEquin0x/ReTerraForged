/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.rivermap.gen;

import com.terraforged.mod.level.levelgen.rivermap.river.Network;

public class GenRiver implements Comparable<GenRiver> {
	public final float dx;
	public final float dz;
	public final float angle;
	public final float length;
	public final Network.Builder builder;

	public GenRiver(Network.Builder branch, float angle, float dx, float dz, float length) {
		this.dx = dx;
		this.dz = dz;
		this.angle = angle;
		this.length = length;
		this.builder = branch;
	}

	@Override
	public int compareTo(GenRiver o) {
		return Float.compare(this.angle, o.angle);
	}
}
