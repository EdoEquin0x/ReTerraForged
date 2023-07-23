package com.terraforged.mod.registry.data;

import static com.terraforged.mod.TerraForged.registryKey;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.noise.module.Valley;
import com.terraforged.mod.level.levelgen.util.Seed;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.domain.Domain;
import com.terraforged.mod.noise.func.EdgeFunc;
import com.terraforged.mod.noise.func.Interpolation;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;

public interface TFNoise {
	ResourceKey<Registry<Module>> REGISTRY = registryKey("worldgen/noise");
	
	ResourceKey<Module> STEPPE = resolve("steppe");
	ResourceKey<Module> PLAINS = resolve("plains");
	ResourceKey<Module> HILLS_1 = resolve("hills_1");
	ResourceKey<Module> HILLS_2 = resolve("hills_2");
	ResourceKey<Module> DALES = resolve("dales");
	ResourceKey<Module> PLATEAU = resolve("plateau");
	ResourceKey<Module> BADLANDS = resolve("badlands");
	ResourceKey<Module> TORRIDONIAN = resolve("torridonian");
	ResourceKey<Module> MOUNTAINS_1 = resolve("mountains_1");
	ResourceKey<Module> MOUNTAINS_2 = resolve("mountains_2");
	ResourceKey<Module> MOUNTAINS_3 = resolve("mountains_3");
	ResourceKey<Module> DOLOMITES = resolve("dolomites");
	ResourceKey<Module> MOUNTAINS_RIDGE_1 = resolve("mountains_ridge_1");
	ResourceKey<Module> MOUNTAINS_RIDGE_2 = resolve("mountains_ridge_2");
	ResourceKey<Module> TEST = resolve("test");
	
    static void register(BootstapContext<Module> ctx) {
        var seed = new Seed(0);
        ctx.register(STEPPE, Terrain.createSteppe(seed));
        ctx.register(PLAINS, Terrain.createPlains(seed));
        ctx.register(HILLS_1, Terrain.createHills1(seed));
        ctx.register(HILLS_2, Terrain.createHills2(seed));
        ctx.register(DALES, Terrain.createDales(seed));
        ctx.register(PLATEAU, Terrain.createPlateau(seed));
        ctx.register(BADLANDS, Terrain.createBadlands(seed));
        ctx.register(TORRIDONIAN, Terrain.createTorridonian(seed));
        ctx.register(MOUNTAINS_1, Terrain.createMountains1(seed));
        ctx.register(MOUNTAINS_2, Terrain.createMountains2(seed, true));
        ctx.register(MOUNTAINS_3, Terrain.createMountains3(seed, true));
        ctx.register(DOLOMITES, Terrain.createDolomite(seed));
        ctx.register(MOUNTAINS_RIDGE_1, Terrain.createMountains2(seed, false));
        ctx.register(MOUNTAINS_RIDGE_2, Terrain.createMountains3(seed, false));
//        ctx.register(TEST, Terrain.createTest(seed));
    }
    
    private static ResourceKey<Module> resolve(String path) {
		return TerraForged.resolve(REGISTRY, path);
	}

    class Terrain {
    	static final float STEPPE_HSCALE = 1.0F;
    	static final float STEPPE_VSCALE = 1.0F;
    	static final float PLAINS_HSCALE = 3.505F;
    	static final float PLAINS_VSCALE = 3.505F;
        static final int MOUNTAINS_H = 610;
        static final double MOUNTAINS_V = 1.3;
        static final int MOUNTAINS2_H = 600;
        static final double MOUNTAINS2_V = 1.145;
        static final float TERRAIN_VERTICAL_SCALE = 0.98F;

        static Module createSteppe(Seed seed) {
            int scaleH = Math.round(250.0F * STEPPE_HSCALE);
            double erosionAmount = 0.45D;
            Module erosion = Source.build(seed.next(), scaleH * 2, 3).lacunarity(3.75D).perlin().alpha(erosionAmount);
            Module warpX = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.0D).perlin();
            Module warpY = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.0D).perlin();
            Module module = Source.perlin(seed.next(), scaleH, 1).mul(erosion).warp(warpX, warpY, Source.constant((double)((float)scaleH / 4.0F))).warp(seed.next(), 256, 1, 200.0D);
            return module.scale(STEPPE_VSCALE).scale(0.08D).bias(-0.02D);
        }
        
        static Module createPlains(Seed seed) {
            int scaleH = Math.round(250.0F * PLAINS_HSCALE);
            double erosionAmount = 0.45D;
            Module erosion = Source.build(seed.next(), scaleH * 2, 3).lacunarity(3.75D).perlin().alpha(erosionAmount);
            Module warpX = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.5D).perlin();
            Module warpY = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.5D).perlin();
            Module module = Source.perlin(seed.next(), scaleH, 1).mul(erosion).warp(warpX, warpY, Source.constant((double)((float)scaleH / 4.0F))).warp(seed.next(), 256, 1, 256.0D);
            return module.scale(PLAINS_VSCALE).scale((double)(0.15F * TERRAIN_VERTICAL_SCALE)).bias(-0.02D);
        }
        
        static Module createHills1(Seed seed) {
        	return Source.perlin(seed.next(), 200, 3).mul(Source.billow(seed.next(), MOUNTAINS2_H, 3).alpha(0.5)).warp(seed.next(), 30, 3, 20.0).warp(seed.next(), MOUNTAINS2_H, 3, 200.0).scale(0.6f * TERRAIN_VERTICAL_SCALE);
        }

        static Module createHills2(Seed seed) {
            return Source.cubic(seed.next(), 128, 2).mul(Source.perlin(seed.next(), 32, 4).alpha(0.075)).warp(seed.next(), 30, 3, 20.0).warp(seed.next(), MOUNTAINS2_H, 3, 200.0).mul(Source.ridge(seed.next(), 512, 2).alpha(0.8)).scale(0.55f * TERRAIN_VERTICAL_SCALE);
        }
        
        static Module createPlateau(Seed seed) {
            Module valley = Source.ridge(seed.next(), 500, 1).invert().warp(seed.next(), 100, 1, 150.0D).warp(seed.next(), 20, 1, 15.0D);
            Module top = Source.build(seed.next(), 150, 3).lacunarity(2.45D).ridge().warp(seed.next(), 300, 1, 150.0D).warp(seed.next(), 40, 2, 20.0D).scale(0.15D).mul(valley.clamp(0.02D, 0.1D).map(0.0D, 1.0D));
            Module surface = Source.perlin(seed.next(), 20, 3).scale(0.05D).warp(seed.next(), 40, 2, 20.0D);
            Module module = valley.mul(Source.cubic(seed.next(), 500, 1).scale(0.6D).bias(0.3D)).add(top).terrace(Source.constant(0.9D), Source.constant(0.15D), Source.constant(0.35D), 4, 0.4D).add(surface);
            return module.scale(0.475D * (double) TERRAIN_VERTICAL_SCALE);
        }
        
        static Module createDales(Seed seed) {
            Module hills1 = Source.build(seed.next(), 300, 4).gain(0.8).lacunarity(4.0).billow().powCurve(0.5).scale(0.75);
            Module hills2 = Source.build(seed.next(), 350, 3).gain(0.8).lacunarity(4.0).billow().pow(1.25);
            Module combined = Source.perlin(seed.next(), MOUNTAINS2_H, 1).clamp(0.3, 0.6).map(0.0, 1.0).blend(hills1, hills2, 0.4, 0.75);
            Module module = combined.pow(1.125).warp(seed.next(), 300, 1, 100.0);
            return module.scale(0.4);
        }
        
        static Module createBadlands(Seed seed) {
            Module mask = Source.build(seed.next(), 270, 3).perlin().clamp(0.35, 0.65).map(0.0, 1.0);
            Module hills = Source.ridge(seed.next(), 275, 4).warp(seed.next(), MOUNTAINS2_H, 2, 100.0).warp(seed.next(), 18, 1, 20.0).mul(mask);
            double modulation = 0.4;
            double alpha = 1.0 - modulation;
            Module mod1 = hills.warp(seed.next(), 100, 1, 50.0).scale(modulation);
            Module lowFreq = hills.steps(4, 0.6, MOUNTAINS_V).scale(alpha).add(mod1);
            Module highFreq = hills.steps(10, 0.6, MOUNTAINS_V).scale(alpha).add(mod1);
            Module detail = lowFreq.add(highFreq);
            Module mod2 = hills.mul(Source.perlin(seed.next(), 200, 3).scale(modulation));
            Module shape = hills.steps(4, 0.65, 0.75, Interpolation.CURVE3).scale(alpha).add(mod2).scale(alpha);
            Module module = shape.mul(detail.alpha(0.5));
            return module.scale(0.55).bias(0.025);
        }
        
        static Module createTorridonian(Seed seed) {
            Module plains = Source.perlin(seed.next(), 100, 3).warp(seed.next(), 300, 1, 150.0).warp(seed.next(), 20, 1, 40.0).scale(0.15);
            Module hills = Source.perlin(seed.next(), 150, 4).warp(seed.next(), 300, 1, 200.0).warp(seed.next(), 20, 2, 20.0).boost();
            Module module = Source.perlin(seed.next(), 200, 3).blend(plains, hills, 0.6, 0.6).terrace(Source.perlin(seed.next(), 120, 1).scale(0.25), Source.perlin(seed.next(), 200, 1).scale(0.5).bias(0.5), Source.constant(0.5), 0.0, 0.3, 6, 1).boost();
            return module.scale(0.5);
        }
        
        static Module createMountains1(Seed seed) {
            int scaleH = Math.round(MOUNTAINS_H);
            Module module = Source.build(seed.next(), scaleH, 4).gain(1.15).lacunarity(2.35).ridge().mul(Source.perlin(seed.next(), 24, 4).alpha(0.075)).warp(seed.next(), 350, 1, 150.0);
            return makeFancy(seed, module).scale(MOUNTAINS_V * TERRAIN_VERTICAL_SCALE);
        }
        
        static Module createMountains2(Seed seed, boolean fancy) {
            Module cell = Source.cellEdge(seed.next(), 360, EdgeFunc.DISTANCE_2).scale(1.2).clamp(0.0, 1.0).warp(seed.next(), 200, 2, 100.0);
            Module blur = Source.perlin(seed.next(), 10, 1).alpha(0.025);
            Module surface = Source.ridge(seed.next(), 125, 4).alpha(0.37);
            Module module = cell.clamp(0.0, 1.0).mul(blur).mul(surface).pow(1.1);
            return (fancy ? makeFancy(seed, module) : module).scale(MOUNTAINS2_V * TERRAIN_VERTICAL_SCALE);
        }
        
        static Module createMountains3(Seed seed, boolean fancy) {
            Module cell = Source.cellEdge(seed.next(), MOUNTAINS2_H, EdgeFunc.DISTANCE_2).scale(1.2).clamp(0.0, 1.0).warp(seed.next(), 200, 2, 100.0);
            Module blur = Source.perlin(seed.next(), 10, 1).alpha(0.025);
            Module surface = Source.ridge(seed.next(), 125, 4).alpha(0.37);
            Module mountains = cell.clamp(0.0, 1.0).mul(blur).mul(surface).pow(1.1);
            Module module = mountains.terrace(Source.perlin(seed.next(), 50, 1).scale(0.5), Source.perlin(seed.next(), 100, 1).clamp(0.5, 0.95).map(0.0, 1.0), Source.constant(0.45), 0.2f, 0.45f, 24, 1);
            return (fancy ? makeFancy(seed, module) : module).scale(MOUNTAINS2_V * TERRAIN_VERTICAL_SCALE);
        }
        
        static Module makeFancy(Seed seed, Module module) {
        	Domain warp = Domain.direction(Source.perlin(seed.next(), 10, 1), Source.constant(2.0));
        	Valley erosion = new Valley(seed.next(), 5, 0.65f, 128.0f, 0.2f, 3.1f, 0.6f, Valley.Mode.CONSTANT);
        	return erosion.wrap(Holder.direct(module)).warp(warp);
        }
        
        static Module createDolomite(Seed seed) {
            // Valley floor terrain
            var base = Source.simplex(seed.next(), 80, 4).scale(0.1);

            // Controls where the ridges show up
            var shape = Source.simplex(seed.next(), 475, 4)
                    .clamp(0.3, 1.0).map(0, 1)
                    .warp(seed.next(), 10, 2, 8);

            // More gradual slopes up to the ridges
            var slopes = shape.pow(2.2).scale(0.65).add(base);

            // Sharp ridges
            var peaks = Source.build(seed.next(), 400, 5).lacunarity(2.7).gain(0.6).simplexRidge()
                    .clamp(0, 0.675).map(0, 1)
                    .warp(Domain.warp(Source.SIMPLEX, seed.next(), 40, 5, 30))
                    .alpha(0.875);

            return shape.mul(peaks).max(slopes)
                    .warp(seed.next(), 800, 3, 300)
                    .scale(0.75);
        }
        
        static Module createTest(Seed seed) {
        	Module base = Source.builder().frequency(0.0025D).build(Source.PERLIN2);
        	Module scale = Source.simplex(9, 2).freq(0.0025D, 0.0025D).alpha(0.8).invert();
            // Sharp ridges
        	Module peaks = Source.build(seed.next(), 400, 5).lacunarity(2.7).gain(0.6).simplexRidge()
                    .clamp(0, 0.675).map(0, 1)
                    .warp(Domain.warp(Source.SIMPLEX, seed.next(), 40, 5, 30))
                    .alpha(0.875)
                    .scale(0.4);
        	return base.mul(scale).max(peaks);
        }
    }
}
