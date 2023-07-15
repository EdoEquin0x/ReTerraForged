/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.level.levelgen.settings.Levels;
import com.terraforged.mod.level.levelgen.settings.Settings;

public class GeneratorContext {
	public static final Codec<GeneratorContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Seed.CODEC.fieldOf("seed").forGetter((c) -> c.seed),
		Settings.CODEC.fieldOf("settings").forGetter((c) -> c.settings)
	).apply(instance, GeneratorContext::new));
	
    public final Seed seed;
    public final Levels levels;
    public final Settings settings;

    public GeneratorContext(Seed seed, Settings settings) {
        this.settings = settings;
        this.seed = seed;
        this.levels = new Levels(settings.world());
    }

    protected GeneratorContext(GeneratorContext src) {
        this(src, 0);
    }

    protected GeneratorContext(GeneratorContext src, int seedOffset) {
        this.seed = src.seed.offset(seedOffset);
        this.levels = src.levels;
        this.settings = src.settings;
    }

    public Seed seed() {
        return this.seed.split();
    }

    public Seed seed(int offset) {
        return this.seed.offset(offset);
    }
}

