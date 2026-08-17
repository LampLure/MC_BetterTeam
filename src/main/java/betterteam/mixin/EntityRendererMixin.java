package betterteam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends net.minecraft.world.entity.Entity, S extends EntityRenderState> {
	@Inject(method = "submitNameDisplay", at = @At("HEAD"), cancellable = true)
	private void betterteam$submitNameDisplay(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		if (state.entityType != EntityType.PLAYER) {
			return;
		}
		BetterTeamConfig config = BetterTeamClient.getConfig();
		if (config == null) {
			return;
		}
		TeamConfig team = config.getActiveTeam();
		if (team == null) {
			return;
		}
		if (state.nameTag == null) {
			return;
		}
		String name = state.nameTag.getString();
		boolean isMember = team.isMember(name);
		boolean shouldColor;
		if (team.isWhitelist()) {
			shouldColor = isMember;
		} else {
			shouldColor = !isMember;
		}
		if (!shouldColor) {
			return;
		}

		// Apply custom text color with opacity (alpha) to the name tag component.
		int textColorRgb = team.getNameTextColorInt() & 0x00FFFFFF;
		float textOpacity = team.getNameTextOpacity();
		int alpha = (int) (textOpacity * 255.0F) & 0xFF;
		int argbColor = (alpha << 24) | textColorRgb;

		Component customNameTag = state.nameTag.copy().withStyle(style -> style.withColor(TextColor.fromRgb(argbColor)));

		// Position the name tag slightly higher than the default attachment point.
		Vec3 originalPos = state.nameTagAttachment;
		Vec3 adjustedPos = originalPos != null
				? originalPos.add(0.0D, 0.25D, 0.0D)
				: new Vec3(0.0D, 0.25D, 0.0D);

		submitNodeCollector.submitNameTag(
				poseStack,
				adjustedPos,
				0,
				customNameTag,
				!state.isDiscrete,
				state.lightCoords,
				state.distanceToCameraSq,
				camera
		);
		ci.cancel();
	}
}
