package com.terraforged.mod.noise.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.noise.Module;

public class Freq extends Modifier {
	public static final Codec<Freq> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Module.DIRECT_CODEC.fieldOf("source").forGetter((m) -> m.source),
		Module.DIRECT_CODEC.fieldOf("x").forGetter((m) -> m.x),
		Module.DIRECT_CODEC.fieldOf("y").forGetter((m) -> m.y)
	).apply(instance, Freq::new));
	
    private final Module x;
    private final Module y;

    public Freq(Module source, Module x, Module y) {
        super(source);
        this.x = x;
        this.y = y;
    }

    @Override
    public float getValue(float x, float y) {
    	float fx = this.x.getValue(x, y);
        float fy = this.y.getValue(x, y);
        return this.source.getValue(x * fx, y * fy);
    }

    @Override
    public float modify(float x, float y, float noiseValue) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Freq freq = (Freq) o;

        if (!x.equals(freq.x)) return false;
        return y.equals(freq.y);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + x.hashCode();
        result = 31 * result + y.hashCode();
        return result;
    }
    
    @Override
	public Codec<Freq> codec() {
		return CODEC;
	}
}
