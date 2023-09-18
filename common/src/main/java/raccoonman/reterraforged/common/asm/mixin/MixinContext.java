package raccoonman.reterraforged.common.asm.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import raccoonman.reterraforged.common.ReTerraForged;
import raccoonman.reterraforged.common.asm.extensions.ContextExtension;
import raccoonman.reterraforged.common.level.levelgen.surface.extension.ExtensionRuleSource;

@Mixin(Context.class)
@Implements(@Interface(iface = ContextExtension.class, prefix = ReTerraForged.MOD_ID + "$ContextExtension$"))
class MixinContext {
	private List<ExtensionRuleSource.Extension> extensions = new ArrayList<>();
	
	public List<ExtensionRuleSource.Extension> reterraforged$ContextExtension$extensions() {
		return this.extensions;
	}
}