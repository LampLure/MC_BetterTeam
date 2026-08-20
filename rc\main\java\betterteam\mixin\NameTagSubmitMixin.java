package betterteam.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
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
		TeamConfig team = getActiveTeam();
		if (team == null || text == null) {
			return;
		}
		String name = text.getString();
		if (!shouldHighlight(team, name)) {
			return;
		}
		cir.setReturnValue(Component.literal(name));
	}

	@Inject(method = "color", at = @At("HEAD"), cancellable = true)
	private void betterteam$color(CallbackInfoReturnable<Integer> cir) {
		TeamConfig team = getActiveTeam();
		if (team == null || text == null) {
			return;
		}
		String name = text.getString();
		if (!shouldHighlight(team, name)) {
			return;
		}
		int textColor = ((int) (team.getNameTextOpacity() * 255.0F) << 24) | (team.getNameTextColorInt() & 0x00FFFFFF);
		cir.setReturnValue(textColor);
	}

	@Inject(method = "backgroundColor", at = @At("HEAD"), cancellable = true)
	private void betterteam$backgroundColor(CallbackInfoReturnable<Integer> cir) {
		TeamConfig team = getActiveTeam();
		if (team == null || text == null) {
			return;
		}
		String name = text.getString();
		if (!shouldHighlight(team, name)) {
			return;
		}
		int backgroundColor = ((int) (team.getNameBackgroundOpacity() * 255.0F) << 24) | (team.getNameBackgroundColorInt() & 0x00FFFFFF);
		cir.setReturnValue(backgroundColor);
	}

	@Inject(method = "lightCoords", at = @At("HEAD"), cancellable = true)
	private void betterteam$lightCoords(CallbackInfoReturnable<Integer> cir) {
		TeamConfig team = getActiveTeam();
		if (team == null || text == null) {
			return;
		}
		String name = text.getString();
		if (!shouldHighlight(team, name)) {
			return;
		}
		cir.setReturnValue(LightCoordsUtil.lightCoordsWithEmission(lightCoords, 2));
	}

	private TeamConfig getActiveTeam() {
		BetterTeamConfig config = BetterTeamClient.getConfig();
		if (config == null) {
			return null;
		}
		return config.getActiveTeam();
	}

	private boolean shouldHighlight(TeamConfig team, String name) {
		if (name == null || name.isBlank()) {
			return false;
		}
		boolean isMember = team.isMember(name);
		return team.isWhitelist() ? isMember : !isMember;
	}
}
