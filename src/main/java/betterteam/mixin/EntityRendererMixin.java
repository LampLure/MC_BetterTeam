package betterteam.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import betterteam.client.BetterTeamClient;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void betterteam$extractRenderState(T entity, S state, float partialTick, CallbackInfo ci) {
		if (state.entityType != EntityType.PLAYER) {
			return;
		}

		state.nameTag = entity.getDisplayName().copy();
		state.nameTagAttachment = entity.getAttachments()
			.getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTick));
	}

	@Inject(method = "finalizeRenderState", at = @At("TAIL"))
	private void betterteam$finalizeRenderState(T entity, S state, CallbackInfo ci) {
		if (BetterTeamClient.shouldGlow(entity)) {
			state.outlineColor = 0xFF000000 | (BetterTeamClient.getGlowColor(entity) & 0x00FFFFFF);
		}

		if (state.entityType != EntityType.PLAYER || state.nameTag == null) {
			return;
		}

		state.nameTag = state.nameTag.copy();
		Vec3 attachment = state.nameTagAttachment;
		if (attachment != null) {
			state.nameTagAttachment = attachment.add(0.0D, 0.25D, 0.0D);
		}
	}

	@Inject(
		method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void betterteam$submitPlayerNameDisplay(
		EntityRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState cameraRenderState,
		int yOffset,
		CallbackInfo ci
	) {
		if (state.entityType != EntityType.PLAYER || state.nameTag == null || state.nameTagAttachment == null) {
			return;
		}

		poseStack.pushPose();
		if (state.scoreText != null) {
			submitNodeCollector.submitNameTag(
				poseStack,
				state.nameTagAttachment,
				yOffset,
				state.scoreText,
				true,
				state.lightCoords,
				state.distanceToCameraSq,
				cameraRenderState
			);
			poseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
		}

		submitNodeCollector.submitNameTag(
			poseStack,
			state.nameTagAttachment,
			yOffset,
			state.nameTag,
			true,
			state.lightCoords,
			state.distanceToCameraSq,
			cameraRenderState
		);
		poseStack.popPose();
		ci.cancel();
	}
}
