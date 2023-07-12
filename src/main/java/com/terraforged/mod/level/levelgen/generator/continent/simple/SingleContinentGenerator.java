/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.generator.continent.simple;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.cell.Cell;
import com.terraforged.mod.level.levelgen.generator.GeneratorContext;
import com.terraforged.mod.level.levelgen.seed.Seed;
import com.terraforged.mod.noise.util.Vec2i;
import com.terraforged.mod.util.pos.PosUtil;

public class SingleContinentGenerator extends ContinentGenerator {
	public static final Codec<SingleContinentGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Seed.CODEC.fieldOf("seed").forGetter((c) -> new Seed(c.seed)), // TODO i think theres a way to do this without converting it back to a Seed
		GeneratorContext.CODEC.fieldOf("context").forGetter((c) -> c.context)
	).apply(instance, SingleContinentGenerator::new));
    private final Vec2i center;

    public SingleContinentGenerator(Seed seed, GeneratorContext context) {
        super(seed, context);
        long center = this.getNearestCenter(0.0f, 0.0f);
        int cx = PosUtil.unpackLeft(center);
        int cz = PosUtil.unpackRight(center);
        this.center = new Vec2i(cx, cz);
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        super.apply(cell, x, y);
        if (cell.continentX != this.center.x || cell.continentZ != this.center.y) {
            cell.continentId = 0.0f;
            cell.continentEdge = 0.0f;
            cell.continentX = 0;
            cell.continentZ = 0;
        }
    }

	@Override
	public Codec<SingleContinentGenerator> codec() {
		return CODEC;
	}
}

