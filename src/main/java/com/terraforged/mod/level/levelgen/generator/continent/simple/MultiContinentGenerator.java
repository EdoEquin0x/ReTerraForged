/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.continent.simple;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.seed.Seed;

public class MultiContinentGenerator extends ContinentGenerator {
	public static final Codec<MultiContinentGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Seed.CODEC.fieldOf("seed").forGetter((c) -> new Seed(c.seed)), // TODO i think theres a way to do this without converting it back to a Seed
		GeneratorContext.CODEC.fieldOf("context").forGetter((c) -> c.context)
	).apply(instance, MultiContinentGenerator::new));
	
    public MultiContinentGenerator(Seed seed, GeneratorContext context) {
        super(seed, context);
    }

	@Override
	public Codec<MultiContinentGenerator> codec() {
		return CODEC;
	}
}

