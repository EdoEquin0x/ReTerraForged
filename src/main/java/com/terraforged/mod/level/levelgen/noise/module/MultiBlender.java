/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.noise.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.func.Interpolation;
import com.terraforged.mod.noise.util.NoiseUtil;

public class MultiBlender extends Select implements Populator {
	public static final Codec<MultiBlender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Module.CODEC.fieldOf("control").forGetter((p) -> p.control),
		Populator.CODEC.fieldOf("lower").forGetter((p) -> p.lower),
		Populator.CODEC.fieldOf("middle").forGetter((p) -> p.middle),
		Populator.CODEC.fieldOf("upper").forGetter((p) -> p.upper),
		Codec.FLOAT.fieldOf("min").forGetter((p) -> p.blendLower),
		Codec.FLOAT.fieldOf("mid").forGetter((p) -> p.midpoint),
		Codec.FLOAT.fieldOf("max").forGetter((p) -> p.blendUpper)
	).apply(instance, MultiBlender::new));
	
    private final Populator lower;
    private final Populator middle;
    private final Populator upper;
    private final float midpoint;
    private final float blendLower;
    private final float blendUpper;
    private final float lowerRange;
    private final float upperRange;

    public MultiBlender(Module control, Populator lower, Populator middle, Populator upper, float min, float mid, float max) {
        super(control);
        this.lower = lower;
        this.upper = upper;
        this.middle = middle;
        this.midpoint = mid;
        this.blendLower = min;
        this.blendUpper = max;
        this.lowerRange = this.midpoint - this.blendLower;
        this.upperRange = this.blendUpper - this.midpoint;
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
        if (select < this.midpoint) {
            float alpha = Interpolation.CURVE3.apply((select - this.blendLower) / this.lowerRange);
            this.lower.apply(cell, x, y);
            float lowerVal = cell.value;
            this.middle.apply(cell, x, y);
            float upperVal = cell.value;
            cell.value = NoiseUtil.lerp(lowerVal, upperVal, alpha);
        } else {
            float alpha = Interpolation.CURVE3.apply((select - this.midpoint) / this.upperRange);
            this.middle.apply(cell, x, y);
            float lowerVal = cell.value;
            this.upper.apply(cell, x, y);
            cell.value = NoiseUtil.lerp(lowerVal, cell.value, alpha);
        }
    }

	@Override
	public Codec<? extends Populator> codec() {
		return null;
	}
}
