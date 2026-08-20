package betterteam.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import betterteam.client.BetterTeamClient;
import betterteam.config.TeamConfig;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;

@Mixin(SubmitNodeStorage.NameTagSubmit.class)
public class NameTagSubmitMixin {
	@Shadow
	@Final
	private Component text;

	@Shadow
	@Final
	private int lightCoords;

	@Inject(method = "text", at = @At("HEAD"), cancellable = true)
	private void betterteam$text(CallbackInfoReturnable<Component> cir) {
		TeamConfig team = BetterTeamClient.getActiveTeamForName(text != null ? text.getString() : null);
		if (team == null || text == null) {
			return;
		}
		cir.setReturnValue(text.copy());
	}

	@Inject(method = "color", at = @At("HEAD"), cancellable = true)
	private void betterteam$color(CallbackInfoReturnable<Integer> cir) {
		TeamConfig team = BetterTeamClient.getActiveTeamForName(text != null ? text.getString() : null);
		if (team == null || text == null) {
			return;
		}
		int textColor = ((int) (team.getNameTextOpacity() * 255.0F) << 24) | (team.getNameTextColorInt() & 0x00FFFFFF);
		cir.setReturnValue(textColor);
	}

	@Inject(method = "backgroundColor", at = @At("HEAD"), cancellable = true)
	private void betterteam$backgroundColor(CallbackInfoReturnable<Integer> cir) {
		TeamConfig team = BetterTeamClient.getActiveTeamForName(text != null ? text.getString() : null);
		if (team == null || text == null) {
			return;
		}
		int backgroundColor = ((int) (team.getNameBackgroundOpacity() * 255.0F) << 24) | (team.getNameBackgroundColorInt() & 0x00FFFFFF);
		cir.setReturnValue(backgroundColor);
	}

	@Inject(method = "lightCoords", at = @At("HEAD"), cancellable = true)
	private void betterteam$lightCoords(CallbackInfoReturnable<Integer> cir) {
		TeamConfig team = BetterTeamClient.getActiveTeamForName(text != null ? text.getString() : null);
		if (team == null || text == null) {
			return;
		}
		cir.setReturnValue(LightCoordsUtil.lightCoordsWithEmission(lightCoords, 2));
	}
}
