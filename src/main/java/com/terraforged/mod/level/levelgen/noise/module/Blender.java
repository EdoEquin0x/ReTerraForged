/*

 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.noise.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.level.levelgen.generator.terrain.Terrain;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.func.Interpolation;
import com.terraforged.mod.noise.util.NoiseUtil;

public class Blender extends Select implements Populator {
	public static final Codec<Blender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Module.CODEC.fieldOf("control").forGetter((p) -> p.control),
		Populator.CODEC.fieldOf("lower").forGetter((p) -> p.lower),
		Populator.CODEC.fieldOf("upper").forGetter((p) -> p.upper),
		Codec.FLOAT.fieldOf("min").forGetter((p) -> p.blendLower),
		Codec.FLOAT.fieldOf("max").forGetter((p) -> p.blendUpper),
		Codec.FLOAT.fieldOf("split").forGetter((p) -> p.split)
	).apply(instance, Blender::new));
	
    private final Populator lower;
    private final Populator upper;
    private final float blendLower;
    private final float blendUpper;
    private final float split;
    private final float blendRange;
    private final float midpoint;

    public Blender(Module control, Populator lower, Populator upper, float min, float max, float split) {
        super(control);
        this.lower = lower;
        this.upper = upper;
        this.blendLower = min;
        this.blendUpper = max;
        this.split = split;
        this.blendRange = this.blendUpper - this.blendLower;
        this.midpoint = this.blendLower + this.blendRange * split;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        float select = this.getSelect(cell, x, y);
        if (select < this.blendLower) {
            this.lower.apply(cell, x, y);
            return;
        }
        if (select > this.blendUpper) {
            this.upper.apply(cell, x, y);
            return;
        }
        float alpha = Interpolation.LINEAR.apply((select - this.blendLower) / this.blendRange);
        this.lower.apply(cell, x, y);
        float lowerVal = cell.value;
        Terrain lowerType = cell.terrain;
        this.upper.apply(cell, x, y);
        float upperVal = cell.value;
        cell.value = NoiseUtil.lerp(lowerVal, upperVal, alpha);
        if (select < this.midpoint) {
            cell.terrain = lowerType;
        }
    }

	@Override
	public Codec<Blender> codec() {
		return CODEC;
	}
}

