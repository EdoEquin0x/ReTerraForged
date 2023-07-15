package com.terraforged.mod.client.gui.preview;

import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforged.mod.level.levelgen.climate.ClimateSample;
import com.terraforged.mod.level.levelgen.generator.TFChunkGenerator;
import com.terraforged.mod.util.ColorUtil;
import com.terraforged.mod.util.MathUtil;

import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biome;

//TODO finish this
public class WorldPreviewScreen extends Screen {
	private static final int MAX_SCALE = 1000;
	
	private final CreateWorldScreen parent;
	private DynamicTexture framebuffer;
//	private final Registry<Biome> biomes;
	private final TFChunkGenerator generator;
	private Layer layer;
	private int scale;
	
	public WorldPreviewScreen(CreateWorldScreen parent, Registry<Biome> biomes, TFChunkGenerator generator) {
		super(Component.translatable("createWorld.customize.terraforged.title"));
		this.parent = parent;
		this.framebuffer = new DynamicTexture(256, 256, false);
//		this.biomes = biomes;
		this.generator = generator;
		this.scale = 15;
		
		this.setLayer(Layer.CONTINENT);
	}

	@Override
	public void onClose() {
		this.framebuffer.close();
		
		this.minecraft.setScreen(this.parent);
	}
	
	public void setLayer(Layer layer) {
		this.layer = layer;
		
		this.rebuildFramebuffer();
	}
	
	private void rebuildFramebuffer() {
		final int cellCount = 16;
		NativeImage pixels = this.framebuffer.getPixels();
		int cellWidth = pixels.getWidth() / cellCount;
		int cellHeight = pixels.getHeight() / cellCount;

		CompletableFuture<?>[] futures = new CompletableFuture[cellCount * cellCount];
		for(int x = 0; x < cellCount; x++) {
			for(int y = 0; y < cellCount; y++) {
				final int cx = x * cellWidth;
				final int cy = y * cellHeight;
				futures[x + y * cellCount] = CompletableFuture.runAsync(() -> {
					for(int lx = 0; lx < cellWidth; lx++) {
						for(int ly = 0; ly < cellHeight; ly++) {
							int tx = cx + lx;
							int ty = cy + ly;
							
							ClimateSample sample = this.generator.getClimateSampler().getSample(tx * this.scale, ty * this.scale);
				            pixels.setPixelRGBA(tx, ty, this.layer.getColor(this.layer.getValue(sample)));
						}
					}
				}, Util.backgroundExecutor());
			}
		}
		
		CompletableFuture.allOf(futures).join();

		this.framebuffer.upload();		
	}
	
	@Override
	protected void init() {		
		this.addRenderableWidget(new FramebufferWidget(this.framebuffer, this.width / 2 - 128, this.height - 512 + 64, 256, 256, Component.empty()));
		this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 128 + 16, this.height - 256 + 64 + 7, 100, 20, CommonComponents.EMPTY, (float) this.scale / MAX_SCALE) {
	         {
	            this.updateMessage();
	         }

	         @Override
	         protected void updateMessage() {
	            this.setMessage(Component.translatable("createWorld.customize.terraforged.world_preview.zoom", (float) (MAX_SCALE - WorldPreviewScreen.this.scale) / MAX_SCALE));
	         }

	         @Override
	         protected void applyValue() {
	        	 WorldPreviewScreen.this.scale = (int) ((1 - this.value) * MAX_SCALE);
	        	 
	        	 WorldPreviewScreen.this.rebuildFramebuffer();
	         }
		});

		this.addRenderableWidget(
			CycleButton.builder(Layer::getName)
				.withValues(Layer.TEMPERATURE, Layer.MOISTURE, Layer.CONTINENT, Layer.WATER)
				.withInitialValue(this.layer)
				.create(this.width / 2 + 12, this.height - 256 + 64 + 7, 100, 20, Component.translatable("createWorld.customize.terraforged.world_preview.layer"), (button, layer) -> {
					this.setLayer(layer);
				})
		);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
			this.minecraft.setScreen(this.parent);
		}).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
			this.minecraft.setScreen(this.parent);
		}).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTick);
		drawCenteredString(stack, this.font, this.title, this.width / 2, 8, 16777215);
	}
	
	public interface Layer {
		static final Layer TEMPERATURE = new Layer() {

			@Override
			public float getValue(ClimateSample sample) {
				return sample.temperature;
			}

			@Override
			public int getColor(float value) {
	            float saturation = 0.7F;
	            float brightness = 0.8F;
				return ColorUtil.rgba(MathUtil.step(1 - value, 8) * 0.65F, saturation, brightness);
			}

			@Override
			public Component getName() {
				return Component.translatable("createWorld.customize.terraforged.world_preview.layer.temperature");
			}
		};
		
		static final Layer MOISTURE = new Layer() {

			@Override
			public float getValue(ClimateSample sample) {
				return sample.moisture;
			}

			@Override
			public int getColor(float value) {
	            float saturation = 0.7F;
	            float brightness = 0.8F;
				return ColorUtil.rgba(MathUtil.step(value, 8) * 0.65F, saturation, brightness);
			}

			@Override
			public Component getName() {
				return Component.translatable("createWorld.customize.terraforged.world_preview.layer.moisture");
			}
		};
		
		static final Layer CONTINENT = new Layer() {

			@Override
			public float getValue(ClimateSample sample) {
				return sample.continentNoise;
			}

			@Override
			public int getColor(float value) {
	            float saturation = 0.7F;
	            float brightness = 0.8F;
				return ColorUtil.rgba(MathUtil.step(value, 8) * 0.65F, saturation, brightness);
			}

			@Override
			public Component getName() {
				return Component.translatable("createWorld.customize.terraforged.world_preview.layer.continent");
			}
		};
		
		static final Layer WATER = new Layer() {

			@Override
			public float getValue(ClimateSample sample) {
				return Math.max(1 - sample.continentNoise, 1 - sample.riverNoise);
			}

			@Override
			public int getColor(float value) {
				return ColorUtil.rgba((int) (40 * value), (int) (140 * value), (int) (200 * value));
			}

			@Override
			public Component getName() {
				return Component.translatable("createWorld.customize.terraforged.world_preview.layer.water");
			}
		};
		
		float getValue(ClimateSample sample);
		
		int getColor(float value);
		
		Component getName();
	}
	
	private class FramebufferWidget extends AbstractWidget {
		
		public FramebufferWidget(AbstractTexture framebuffer, int x, int y, int w, int h, Component message) {
			super(x, y, w, h, message);
		}

		@Override
		public void renderWidget(PoseStack stack, int mouseX, int mouseY, float partialTick) {
			int x = this.getX();
			int y = this.getY();
			
			RenderSystem.setShaderTexture(0, WorldPreviewScreen.this.framebuffer.getId());
			blit(stack, x, y, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
			
			if(this.isMouseOver(mouseX, mouseY)) {
//				int relativeMouseX = (mouseX - this.getX()) * WorldPreviewScreen.this.scale;
//				int relativeMouseY = (mouseY - this.getY()) * WorldPreviewScreen.this.scale;
//				
//				IClimateSampler sampler = WorldPreviewScreen.this.generator.getClimateSampler();
//				ClimateSample sample = sampler.getSample(relativeMouseX, relativeMouseY);
//				Biome biome = sampler.sampleBiome(sample).value();
//				String biomeKey = WorldPreviewScreen.this.biomes.getKey(biome).toString();
//				drawString(stack, WorldPreviewScreen.this.font, "biome: " + biomeKey, x + 8, y + this.getHeight() - 16, 16777215);
//				
//				if(Screen.hasAltDown()) {
//					drawString(stack, WorldPreviewScreen.this.font, "noise: " + WorldPreviewScreen.this.layer.getValue(sample), x + 8, y + this.getWidth() - 26, 16777215);	
//				}
			}
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narration) {
			// TODO
		}
	}
}
