package betterteam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import betterteam.client.BetterTeamClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
	private void betterteam$shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (BetterTeamClient.shouldGlow(entity)) {
			cir.setReturnValue(true);
		}
	}
}
