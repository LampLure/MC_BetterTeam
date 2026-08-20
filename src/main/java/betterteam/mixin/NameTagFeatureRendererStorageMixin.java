package betterteam.mixin;

import java.util.List;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import betterteam.client.BetterTeamClient;
import betterteam.config.TeamConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

@Mixin(NameTagFeatureRenderer.Storage.class)
public class NameTagFeatureRendererStorageMixin {
	@Shadow
	@Final
	private List<SubmitNodeStorage.NameTagSubmit> nameTagSubmitsSeethrough;

	@Shadow
	@Final
	private List<SubmitNodeStorage.NameTagSubmit> nameTagSubmitsNormal;

	@Inject(
		method = "add(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void betterteam$forceHighlightSeeThrough(
		PoseStack poseStack,
		Vec3 attachment,
		int yOffset,
		Component text,
		boolean seeThrough,
		int lightCoords,
		double distanceToCameraSq,
		CameraRenderState cameraRenderState,
		CallbackInfo ci
	) {
		if (attachment == null || text == null) {
			return;
		}
		TeamConfig team = BetterTeamClient.getActiveTeamForName(text.getString());
		if (team == null) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		poseStack.pushPose();
		poseStack.translate(attachment.x, attachment.y + 0.5D, attachment.z);
		poseStack.mulPose(cameraRenderState.orientation);
		poseStack.scale(0.025F, -0.025F, 0.025F);

		Matrix4f pose = new Matrix4f(poseStack.last().pose());
		float textX = -minecraft.font.width(text) / 2.0F;
		int defaultBackgroundOpacity = (int) (minecraft.gameRenderer.getGameRenderState().optionsRenderState.getBackgroundOpacity(0.25F) * 255.0F) << 24;

		nameTagSubmitsNormal.add(new SubmitNodeStorage.NameTagSubmit(
			pose,
			textX,
			(float) yOffset,
			text,
			LightCoordsUtil.lightCoordsWithEmission(lightCoords, 2),
			-1,
			0,
			distanceToCameraSq
		));
		nameTagSubmitsSeethrough.add(new SubmitNodeStorage.NameTagSubmit(
			pose,
			textX,
			(float) yOffset,
			text,
			lightCoords,
			-2130706433,
			defaultBackgroundOpacity,
			distanceToCameraSq
		));

		poseStack.popPose();
		ci.cancel();
	}
}
