/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.noise.Module;

public class Compressor implements Module {
	public static final Codec<Compressor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Module.CODEC.fieldOf("module").forGetter((m) -> m.module),
		Codec.FLOAT.fieldOf("lower_start").forGetter((m) -> m.lowerStart),
		Codec.FLOAT.fieldOf("lower_end").forGetter((m) -> m.lowerEnd),
		Codec.FLOAT.fieldOf("upper_start").forGetter((m) -> m.upperStart),
		Codec.FLOAT.fieldOf("upper_end").forGetter((m) -> m.upperEnd)
	).apply(instance, Compressor::new));
	
    private final float lowerStart;
    private final float lowerEnd;
    private final float lowerRange;
    private final float lowerExpandRange;
    private final float upperStart;
    private final float upperEnd;
    private final float upperRange;
    private final float upperExpandedRange;
    private final float compression;
    private final float compressionRange;
    private final Module module;

    public Compressor(Module module, float inset, float amount) {
        this(module, inset, inset + amount, 1.0f - inset - amount, 1.0f - inset);
    }

    public Compressor(Module module, float lowerStart, float lowerEnd, float upperStart, float upperEnd) {
        this.module = module;
        this.lowerStart = lowerStart;
        this.lowerEnd = lowerEnd;
        this.lowerRange = lowerStart;
        this.lowerExpandRange = lowerEnd;
        this.upperStart = upperStart;
        this.upperEnd = upperEnd;
        this.upperRange = 1.0f - upperEnd;
        this.upperExpandedRange = 1.0f - upperStart;
        this.compression = upperStart - lowerEnd;
        this.compressionRange = upperEnd - lowerStart;
    }

    @Override
    public float getValue(float x, float y) {
        float value = this.module.getValue(x, y);
        if (value <= this.lowerStart) {
            float alpha = value / this.lowerRange;
            return alpha * this.lowerExpandRange;
        }
        if (value >= this.upperEnd) {
            float delta = value - this.upperEnd;
            float alpha = delta / this.upperRange;
            return this.upperStart + alpha * this.upperExpandedRange;
        }
        float delta = value - this.lowerStart;
        float alpha = delta / this.compressionRange;
        return this.lowerEnd + alpha * this.compression;
    }

	@Override
	public Codec<Compressor> codec() {
		return CODEC;
	}
}

