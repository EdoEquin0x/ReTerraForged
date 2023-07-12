/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.continent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.cell.Populator;
import com.terraforged.mod.noise.func.Interpolation;
import com.terraforged.mod.noise.util.NoiseUtil;

public class ContinentLerper2 implements Populator {
	public static final Codec<ContinentLerper2> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Populator.CODEC.fieldOf("lower").forGetter((p) -> p.lower),
		Populator.CODEC.fieldOf("upper").forGetter((p) -> p.upper),
		Codec.FLOAT.fieldOf("min").forGetter((p) -> p.blendLower),
		Codec.FLOAT.fieldOf("max").forGetter((p) -> p.blendUpper),
		Interpolation.CODEC.optionalFieldOf("interpolation", Interpolation.LINEAR).forGetter((p) -> p.interpolation)
	).apply(instance, ContinentLerper2::new));
	
    private final Populator lower;
    private final Populator upper;
    private final Interpolation interpolation;
    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;

    public ContinentLerper2(Populator lower, Populator upper, float min, float max) {
        this(lower, upper, min, max, Interpolation.LINEAR);
    }

    public ContinentLerper2(Populator lower, Populator upper, float min, float max, Interpolation interpolation) {
        this.lower = lower;
        this.upper = upper;
        this.interpolation = interpolation;
        this.blendLower = min;
        this.blendUpper = max;
        this.blendRange = this.blendUpper - this.blendLower;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        if (cell.continentEdge < this.blendLower) {
            this.lower.apply(cell, x, y);
            return;
        }
        if (cell.continentEdge > this.blendUpper) {
            this.upper.apply(cell, x, y);
            return;
        }
        float alpha = this.interpolation.apply((cell.continentEdge - this.blendLower) / this.blendRange);
        this.lower.apply(cell, x, y);
        float lowerVal = cell.value;
        this.upper.apply(cell, x, y);
        float upperVal = cell.value;
        cell.value = NoiseUtil.lerp(lowerVal, upperVal, alpha);
    }

	@Override
	public Codec<ContinentLerper2> codec() {
		return CODEC;
	}
}
